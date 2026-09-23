package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

public final class StaffingEventPublisher {

    private final String brokerUrl;
    private final String topicName;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StaffingEventPublisher(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
    }

    public void publish(StaffingEvent event) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        try (Connection connection = factory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
             MessageProducer producer = session.createProducer(session.createTopic(topicName))) {
            TextMessage message = session.createTextMessage(objectMapper.writeValueAsString(event));
            producer.send(message);
        } catch (JMSException | JsonProcessingException exception) {
            throw new StaffingEventException("Unable to publish staffing event", exception);
        }
    }
}
