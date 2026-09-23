package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

public final class EquipmentFailureConsumer {

    private final String brokerUrl;
    private final String queueName;
    private final EquipmentAlertStore store;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Connection connection;

    public EquipmentFailureConsumer(String brokerUrl, String queueName, EquipmentAlertStore store) {
        this.brokerUrl = brokerUrl;
        this.queueName = queueName;
        this.store = store;
    }

    public void start() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        connection = factory.createConnection();
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(session.createQueue(queueName));
        consumer.setMessageListener(this::receive);
        connection.start();
    }

    private void receive(Message message) {
        if (!(message instanceof TextMessage textMessage)) {
            return;
        }
        try {
            EquipmentFailureEvent event = objectMapper.readValue(
                    textMessage.getText(), EquipmentFailureEvent.class);
            store.record(event);
            message.acknowledge();
        } catch (Exception exception) {
            System.err.println("Equipment failure was not acknowledged: " + exception.getMessage());
        }
    }
}
