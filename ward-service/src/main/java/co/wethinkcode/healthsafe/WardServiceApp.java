package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

import co.wethinkcode.healthsafe.mq.MqConfig;

import java.net.URI;

public class WardServiceApp {

    private static final String DEFAULT_INGESTION_URL = "http://localhost:7030";

    public static void main(String[] args) throws Exception {
        String ingestionUrl = System.getenv().getOrDefault(
                "INGESTION_SERVICE_URL", DEFAULT_INGESTION_URL);
        WardDirectory directory = new WardDirectory(
                new IngestionClient(URI.create(ingestionUrl)).fetchWards());
        StaffingEventSubscriber subscriber = new StaffingEventSubscriber(
                MqConfig.BROKER_URL, MqConfig.TOPIC);
        subscriber.start();
        EquipmentFailurePublisher equipmentPublisher = new EquipmentFailurePublisher(
                MqConfig.BROKER_URL, MqConfig.QUEUE);

        createApp(directory, subscriber, equipmentPublisher).start(7031);
    }

    static Javalin createApp(WardDirectory directory, StaffingEventSubscriber subscriber,
                             EquipmentFailurePublisher equipmentPublisher) {
        Javalin app = Javalin.create();
        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/wards", ctx -> ctx.json(directory.wards()));

        app.get("/wards/{id}", ctx -> directory.find(ctx.pathParam("id"))
                .ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(404).json(new ErrorResponse("Unknown ward"))));

        app.get("/departments", ctx -> ctx.json(directory.departments()));
        app.get("/staffing-events", ctx -> ctx.json(subscriber.receivedEvents()));
        app.post("/equipment-failures", ctx -> {
            try {
                EquipmentFailureRequest request = ctx.bodyAsClass(EquipmentFailureRequest.class);
                if (request.wardId() == null || request.wardId().isBlank()
                        || request.equipment() == null || request.equipment().isBlank()) {
                    ctx.status(400).json(new ErrorResponse("wardId and equipment are required"));
                    return;
                }
                if (directory.find(request.wardId()).isEmpty()) {
                    ctx.status(404).json(new ErrorResponse("Unknown ward"));
                    return;
                }
                EquipmentFailureEvent event = EquipmentFailureEvent.create(
                        request.wardId(), request.equipment(), request.details());
                equipmentPublisher.publish(event);
                ctx.status(202).json(event);
            } catch (EquipmentFailurePublishException exception) {
                ctx.status(503).json(new ErrorResponse("Equipment alert queue unavailable"));
            } catch (Exception exception) {
                ctx.status(400).json(new ErrorResponse("Invalid equipment failure request"));
            }
        });

        return app;
    }

    record ErrorResponse(String error) {
    }

    record EquipmentFailureRequest(String wardId, String equipment, String details) {
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.healthsafe.mq.MqConfig)
