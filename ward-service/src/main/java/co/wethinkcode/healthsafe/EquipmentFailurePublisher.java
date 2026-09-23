package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public final class EquipmentFailurePublisher {

    private final String brokerUrl;
    private final String queueName;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EquipmentFailurePublisher(String brokerUrl, String queueName) {
        this.brokerUrl = brokerUrl;
        this.queueName = queueName;
    }

    public void publish(EquipmentFailureEvent event) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        try (Connection connection = factory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
             MessageProducer producer = session.createProducer(session.createQueue(queueName))) {
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            TextMessage message = session.createTextMessage(objectMapper.writeValueAsString(event));
            producer.send(message);
        } catch (JMSException | JsonProcessingException exception) {
            throw new EquipmentFailurePublishException("Unable to publish equipment failure", exception);
        }
    }
}
