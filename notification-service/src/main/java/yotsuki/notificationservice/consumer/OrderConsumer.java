package yotsuki.notificationservice.consumer;

import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class OrderConsumer {

    @KafkaListener(topics = "order-topic", groupId = "notification-group")
    public void consume(String message) {
        log.info("Received order:{} ", message);
    }
}
