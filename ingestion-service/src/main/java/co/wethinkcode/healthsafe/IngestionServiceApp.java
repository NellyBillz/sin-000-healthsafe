package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class IngestionServiceApp {

    private static final String WARD_DATA = "/wards-outdated.csv";

    public static void main(String[] args) throws IOException {
        createApp(loadWards()).start(7030);
    }

    static Javalin createApp(List<WardRecord> wards) {
        Javalin app = Javalin.create();
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/wards", ctx -> ctx.json(wards));
        return app;
    }

    static List<WardRecord> loadWards() throws IOException {
        InputStream source = IngestionServiceApp.class.getResourceAsStream(WARD_DATA);
        if (source == null) {
            throw new IOException("Missing classpath resource " + WARD_DATA);
        }

        try (InputStreamReader reader = new InputStreamReader(source, StandardCharsets.UTF_8)) {
            return new WardCsvCleaner().clean(reader);
        }
    }
}
