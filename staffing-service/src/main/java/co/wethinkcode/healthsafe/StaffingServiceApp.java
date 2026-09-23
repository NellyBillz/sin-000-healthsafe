package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

import co.wethinkcode.healthsafe.mq.MqConfig;

import java.net.URI;
import java.util.List;

public class StaffingServiceApp {

    private static final String DEFAULT_WARD_URL = "http://localhost:7031";
    private static final String DEFAULT_ALERT_URL = "http://localhost:7032";

    public static void main(String[] args) {
        HospitalServiceClient client = new HospitalServiceClient(
                URI.create(System.getenv().getOrDefault("WARD_SERVICE_URL", DEFAULT_WARD_URL)),
                URI.create(System.getenv().getOrDefault("ALERT_LEVEL_SERVICE_URL", DEFAULT_ALERT_URL)));
        StaffingEventPublisher publisher = new StaffingEventPublisher(
                MqConfig.BROKER_URL, MqConfig.TOPIC);

        createApp(client, new StaffingPlanner(), publisher).start(7033);
    }

    static Javalin createApp(
            HospitalServiceClient client,
            StaffingPlanner planner,
            StaffingEventPublisher publisher) {
        Javalin app = Javalin.create();
        app.get("/health", ctx -> ctx.result("OK"));

        app.post("/schedules", ctx -> {
            ScheduleRequest request;
            try {
                request = ctx.bodyAsClass(ScheduleRequest.class);
                validate(request);
            } catch (Exception exception) {
                ctx.status(400).json(new ErrorResponse(
                        "Request must contain a wardId and a doctors list"));
                return;
            }

            try {
                if (!client.wardExists(request.wardId())) {
                    ctx.status(404).json(new ErrorResponse("Unknown ward"));
                    return;
                }

                int alertLevel = client.fetchAlertLevel();
                StaffingPlanner.Plan plan = planner.plan(request.doctors(), alertLevel);
                StaffingEvent event = new StaffingEvent(
                        request.wardId().trim().toUpperCase(),
                        alertLevel,
                        plan.requiredDoctors(),
                        plan.assignedDoctors(),
                        plan.understaffed());
                publisher.publish(event);
                ctx.json(new ScheduleResponse(
                        event.wardId(),
                        event.alertLevel(),
                        event.requiredDoctors(),
                        event.assignedDoctors(),
                        event.understaffed()));
            } catch (DownstreamServiceException exception) {
                ctx.status(503).json(new ErrorResponse(exception.getMessage()));
            } catch (StaffingEventException exception) {
                ctx.status(503).json(new ErrorResponse(exception.getMessage()));
            }
        });

        return app;
    }

    private static void validate(ScheduleRequest request) {
        if (request == null || request.wardId() == null || request.wardId().isBlank()
                || request.doctors() == null) {
            throw new IllegalArgumentException("Invalid schedule request");
        }
    }

    record ScheduleRequest(String wardId, List<String> doctors) {
    }

    record ScheduleResponse(
            String wardId,
            int alertLevel,
            int requiredDoctors,
            List<String> assignedDoctors,
            boolean understaffed) {
    }

    record ErrorResponse(String error) {
    }
}

// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.healthsafe.mq.MqConfig)
