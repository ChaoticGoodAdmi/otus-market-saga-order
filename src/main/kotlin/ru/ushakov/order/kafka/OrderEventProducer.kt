package ru.ushakov.order.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import ru.ushakov.order.domain.Item
import java.math.BigDecimal
import java.time.LocalDate

@Service
class OrderEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    val objectMapper: ObjectMapper
) {

    fun sendOrderCreatedEvent(event: OrderCreatedEvent) {
        kafkaTemplate.send("OrderCreated", objectMapper.writeValueAsString(event))
        println("OrderCreatedEvent sent to Kafka: $event")
    }
}

data class OrderCreatedEvent(
    val orderId: Long,
    val items: List<Item>,
    val totalPrice: BigDecimal,
    val accountNumber: String,
    val deliveryAddress: String,
    val deliveryDate: LocalDate
)

@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
    }
}