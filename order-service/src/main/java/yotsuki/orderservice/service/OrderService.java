package yotsuki.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaTemplate<String, String> kafkaTemplate;


    public void SendOrder(String message) {
        kafkaTemplate.send("order-topic", message);
    }
}
