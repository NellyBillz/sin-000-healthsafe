package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

import java.net.URI;

public class WardServiceApp {

    private static final String DEFAULT_INGESTION_URL = "http://localhost:7030";

    public static void main(String[] args) throws Exception {
        String ingestionUrl = System.getenv().getOrDefault(
                "INGESTION_SERVICE_URL", DEFAULT_INGESTION_URL);
        WardDirectory directory = new WardDirectory(
                new IngestionClient(URI.create(ingestionUrl)).fetchWards());

        createApp(directory).start(7031);
    }

    static Javalin createApp(WardDirectory directory) {
        Javalin app = Javalin.create();
        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/wards", ctx -> ctx.json(directory.wards()));

        app.get("/wards/{id}", ctx -> directory.find(ctx.pathParam("id"))
                .ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(404).json(new ErrorResponse("Unknown ward"))));

        app.get("/departments", ctx -> ctx.json(directory.departments()));

        return app;
    }

    record ErrorResponse(String error) {
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.healthsafe.mq.MqConfig)
// MQ TODO: publishes to ActiveMQ queue MqConfig.QUEUE when it detects an equipment failure on one of its wards.
