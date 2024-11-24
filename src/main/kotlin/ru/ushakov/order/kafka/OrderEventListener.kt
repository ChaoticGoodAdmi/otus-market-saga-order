package ru.ushakov.order.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import ru.ushakov.order.domain.OrderStatus
import ru.ushakov.order.repository.OrderRepository
import java.math.BigDecimal
import java.time.LocalDate

@Service
class OrderEventListener @Autowired constructor(
    private val orderRepository: OrderRepository
) {

    @KafkaListener(topics = ["BillingReserved"], groupId = "order-service-group")
    fun handleBillingReservedEvent(message: String) {
        val event = parseBillingReservedEvent(message)
        val order = orderRepository.findById(event.orderId)
        if (order.isPresent) {
            val existingOrder = order.get()
            existingOrder.status = OrderStatus.PAID
            orderRepository.save(existingOrder)
            println("Order with ID: ${event.orderId} has been updated to PAID.")
        } else {
            println("Order with ID: ${event.orderId} not found.")
        }
    }

    @KafkaListener(topics = ["BillingFailed"], groupId = "order-service-group")
    fun handleBillingFailedEvent(message: String) {
        val event = parseBillingFailedEvent(message)
        val order = orderRepository.findById(event.orderId)
        if (order.isPresent) {
            val existingOrder = order.get()
            existingOrder.status = OrderStatus.CANCELLED_BY_BILLING
            orderRepository.save(existingOrder)
            println("Order with ID: ${event.orderId} has been updated to CANCELLED status due to BillingFailed event.")
        } else {
            println("Order with ID: ${event.orderId} not found for cancellation.")
        }
    }

    @KafkaListener(topics = ["ItemReserved"], groupId = "order-service-group")
    fun handleItemReservedEvent(message: String) {
        val event = parseItemReservedEvent(message)
        val order = orderRepository.findById(event.orderId)
        if (order.isPresent) {
            val existingOrder = order.get()
            existingOrder.status = OrderStatus.RESERVED
            orderRepository.save(existingOrder)
            println("Order with ID: ${event.orderId} has been updated to PENDING_DELIVERY.")
        } else {
            println("Order with ID: ${event.orderId} not found.")
        }
    }

    @KafkaListener(topics = ["ItemReserveFailed"], groupId = "order-service-group")
    fun handleItemReserveFailedEvent(message: String) {
        val event = parseItemReserveFailedEvent(message)
        val order = orderRepository.findById(event.orderId)
        if (order.isPresent) {
            val existingOrder = order.get()
            existingOrder.status = OrderStatus.CANCELLED_BY_WAREHOUSE
            orderRepository.save(existingOrder)
            println("Order with ID: ${event.orderId} has been updated to CANCELLED status due to ItemReserveFailed event.")
        } else {
            println("Order with ID: ${event.orderId} not found for cancellation.")
        }
    }

    @KafkaListener(topics = ["DeliveryReserved"], groupId = "order-service-group")
    fun handleDeliveryReservedEvent(message: String) {
        val event = parseDeliveryReservedEvent(message)
        val order = orderRepository.findById(event.orderId)
        if (order.isPresent) {
            val existingOrder = order.get()
            existingOrder.status = OrderStatus.READY_TO_DELIVER
            orderRepository.save(existingOrder)
            println("Order with ID: ${event.orderId} has been updated to READY_TO_DELIVER.")
        } else {
            println("Order with ID: ${event.orderId} not found.")
        }
    }

    @KafkaListener(topics = ["DeliveryReserveFailed"], groupId = "order-service-group")
    fun handleDeliveryReserveFailedEvent(message: String) {
        val event = parseDeliveryReservedEvent(message)
        val order = orderRepository.findById(event.orderId)
        if (order.isPresent) {
            val existingOrder = order.get()
            existingOrder.status = OrderStatus.PENDING_DELIVERY
            orderRepository.save(existingOrder)
            println("Order with ID: ${event.orderId} has been updated to PENDING_DELIVERY.")
        } else {
            println("Order with ID: ${event.orderId} not found.")
        }
    }

    private fun parseBillingReservedEvent(message: String): BillingReservedEvent {
        val mapper = jacksonObjectMapper().registerKotlinModule()
        return mapper.readValue(message, BillingReservedEvent::class.java)
    }

    private fun parseBillingFailedEvent(message: String): BillingFailedEvent {
        val mapper = jacksonObjectMapper().registerKotlinModule()
        return mapper.readValue(message, BillingFailedEvent::class.java)
    }

    private fun parseItemReservedEvent(message: String): ItemReservedEvent {
        val mapper = jacksonObjectMapper().registerKotlinModule()
        return mapper.readValue(message, ItemReservedEvent::class.java)
    }

    private fun parseItemReserveFailedEvent(message: String): ItemReserveFailedEvent {
        val mapper = jacksonObjectMapper().registerKotlinModule()
        return mapper.readValue(message, ItemReserveFailedEvent::class.java)
    }

    private fun parseDeliveryReservedEvent(message: String): DeliveryReservedEvent {
        val mapper = jacksonObjectMapper().registerKotlinModule()
        return mapper.readValue(message, DeliveryReservedEvent::class.java)
    }
}

data class BillingReservedEvent(
    val orderId: Long,
    val items: List<Item>,
    val accountNumber: String,
    val totalPrice: BigDecimal,
    val deliveryAddress: String,
    val deliveryDate: LocalDate
)

data class Item(
    val name: String,
    val price: BigDecimal,
    val quantity: Int
)

data class BillingFailedEvent(
    val orderId: Long,
    val reason: String
)

data class ItemReservedEvent(
    val orderId: Long,
    val deliveryDate: LocalDate,
    val deliveryAddress: String
)

data class ItemReserveFailedEvent(
    val orderId: Long,
    val accountNumber: String,
    val totalPrice: BigDecimal
)

data class DeliveryReservedEvent(
    val orderId: Long
)