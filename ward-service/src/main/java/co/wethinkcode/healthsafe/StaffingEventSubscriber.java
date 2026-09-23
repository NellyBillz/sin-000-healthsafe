package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class StaffingEventSubscriber {

    private final String brokerUrl;
    private final String topicName;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<StaffingEvent> receivedEvents = new CopyOnWriteArrayList<>();
    private Connection connection;

    public StaffingEventSubscriber(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
    }

    public void start() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        connection = factory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicName);
        MessageConsumer consumer = session.createConsumer(topic);
        consumer.setMessageListener(this::receive);
        connection.start();
    }

    public List<StaffingEvent> receivedEvents() {
        return List.copyOf(receivedEvents);
    }

    private void receive(Message message) {
        if (!(message instanceof TextMessage textMessage)) {
            return;
        }
        try {
            receivedEvents.add(objectMapper.readValue(textMessage.getText(), StaffingEvent.class));
        } catch (Exception exception) {
            System.err.println("Ignored invalid staffing event: " + exception.getMessage());
        }
    }
}
