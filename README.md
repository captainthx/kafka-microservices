# ระบบการทำงาน Kafka: order-service และ notification-service

## 1. การใช้ Kafka ในแต่ละ Service

### ✅ order-service

* **บทบาท:** Kafka Producer
* **หน้าที่:** ส่งข้อความไปยัง Kafka topic โดยใช้ `KafkaTemplate`
* **คำสั่ง:** `kafkaTemplate.send("order-topic", message)`

### ✅ notification-service

* **บทบาท:** Kafka Consumer
* **หน้าที่:** รับข้อความจาก Kafka topic โดยใช้ `@KafkaListener`
* **คำสั่ง:** `@KafkaListener(topics = "order-topic", groupId = "notification-group")`

---

## 2. ชื่อ Topic

* **ชื่อ Kafka Topic ที่ใช้:** `order-topic`
* **ถูกสร้างโดย:** Bean `TopicBuilder` ใน `KafkaConfig.java` ของ `order-service`

```java
@Bean
public NewTopic orderTopic() {
    return TopicBuilder.name("order-topic").partitions(1).replicas(1).build();
}
```

---

## 3. การตั้งค่า Kafka Client

### 🔹 order-service (`resources/application.yml`)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

* **bootstrap-servers:** ที่อยู่ของ Kafka broker
* **key/value-serializer:** แปลงข้อมูลเป็น String ก่อนส่ง

### 🔹 notification-service (`resources/application.yml`)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: notification-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

* **group-id:** กำหนดกลุ่ม consumer
* **auto-offset-reset:** ถ้าไม่มี offset เดิม ให้อ่านจากต้นหัว
* **deserializer:** แปลง byte เป็น String

---

## 4. หลักการทำงานโดยรวม

### Flow Diagram

```text
Client --> OrderController (HTTP) --> OrderService --> Kafka (order-topic)
                                                 ↓
                                        NotificationService <-- KafkaListener
```

### คำอธิบาย:

1. Client เรียก POST ที่ `order-service`
2. `OrderService` ส่งข้อความไปที่ Kafka topic `order-topic`
3. Kafka ส่งต่อข้อความไปยัง consumer ที่อยู่ใน topic นั้น
4. `notification-service` ฟังด้วย `@KafkaListener`
5. รับข้อความมา log หรือใช้งานต่อ

---

## 5. ไฟล์และคลาสสำคัญ

### 📂 order-service

* `controller/OrderController.java` – รับ HTTP Request
* `service/OrderService.java` – ส่ง Kafka message
* `config/KafkaConfig.java` – สร้าง topic
* `resources/application.yml` – config Kafka producer

### 📂 notification-service

* `notification/consumer/OrderConsumer.java` – รับข้อความจาก Kafka
* `resources/application.yml` – config Kafka consumer

---

## 6. Docker & Zookeeper

### docker-compose.yml

```yaml
services:
  zookeeper:
    image: confluentinc/cp-zookeeper
    ports: ["2181:2181"]

  kafka:
    image: confluentinc/cp-kafka
    ports: ["9092:9092"]
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
```

### Zookeeper มีหน้าที่

* จัดการ metadata ของ Kafka
* ช่วยประสาน broker แต่ละตัว
* ใช้ในการเลือก leader partition

---

## 7. สรุปภาพรวมเข้าใจง่าย

| Service              | บทบาท    | Kafka Component | หมายเหตุ                     |
| -------------------- | -------- | --------------- | ---------------------------- |
| order-service        | Producer | KafkaTemplate   | ส่งข้อมูลไปที่ `order-topic` |
| notification-service | Consumer | @KafkaListener  | รับข้อมูลจาก `order-topic`   |

Kafka ช่วยให้แต่ละ service แยกจากกันได้อย่างอิสระ (Decoupled)
หาก service หนึ่งล่มชั่วคราว ข้อมูลยังอยู่ใน Kafka รอการดึงไปใช้งาน

---

