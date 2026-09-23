package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import co.wethinkcode.healthsafe.mq.MqConfig;

public class EquipmentAlertServiceApp {

    public static void main(String[] args) throws Exception {
        EquipmentAlertStore store = new EquipmentAlertStore();
        EquipmentFailureConsumer consumer = new EquipmentFailureConsumer(
                MqConfig.BROKER_URL, MqConfig.QUEUE, store);
        consumer.start();

        Javalin app = Javalin.create();
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/equipment-alerts", ctx -> ctx.json(store.alerts()));
        app.start(7034);
    }
}
