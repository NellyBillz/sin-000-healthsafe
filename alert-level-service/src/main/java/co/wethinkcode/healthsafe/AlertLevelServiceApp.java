package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

public class AlertLevelServiceApp {

    public static void main(String[] args) {
        createApp(new AlertLevelStore()).start(7032);
    }

    static Javalin createApp(AlertLevelStore store) {
        Javalin app = Javalin.create();
        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/alert-level", ctx -> ctx.json(new AlertLevelResponse(store.get())));

        app.put("/alert-level", ctx -> {
            try {
                AlertLevelRequest request = ctx.bodyAsClass(AlertLevelRequest.class);
                store.set(request.level());
                ctx.json(new AlertLevelResponse(store.get()));
            } catch (IllegalArgumentException exception) {
                ctx.status(400).json(new ErrorResponse(exception.getMessage()));
            } catch (Exception exception) {
                ctx.status(400).json(new ErrorResponse(
                        "Request body must contain a numeric level"));
            }
        });

        return app;
    }

    record AlertLevelRequest(int level) {
    }

    record AlertLevelResponse(int level) {
    }

    record ErrorResponse(String error) {
    }
}
