package ru.ushakov.order.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.ushakov.order.domain.Item
import ru.ushakov.order.domain.Order
import ru.ushakov.order.domain.OrderStatus
import ru.ushakov.order.kafka.OrderEventProducer
import ru.ushakov.order.kafka.OrderCreatedEvent
import ru.ushakov.order.repository.OrderRepository
import java.math.BigDecimal
import java.time.LocalDate

@RestController
@RequestMapping("/orders")
class OrderController @Autowired constructor(
    private val orderRepository: OrderRepository,
    private val orderEventProducer: OrderEventProducer
) {

    @PostMapping
    fun createOrder(
        @RequestBody request: CreateOrderRequest
    ): ResponseEntity<Any> {
        val order = Order(
            items = request.items,
            totalPrice = request.items.sumOf { it.price.multiply(BigDecimal(it.quantity)) },
            accountNumber = request.accountNumber,
            deliveryAddress = request.deliveryAddress,
            deliveryDate = request.deliveryDate,
            status = OrderStatus.CREATED
        )

        orderRepository.save(order)

        val event = OrderCreatedEvent(
            orderId = order.id,
            items = request.items,
            totalPrice = order.totalPrice,
            accountNumber = order.accountNumber,
            deliveryAddress = order.deliveryAddress,
            deliveryDate = order.deliveryDate
        )
        orderEventProducer.sendOrderCreatedEvent(event)
        return ResponseEntity.ok(order)
    }

    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: Long): ResponseEntity<Any> {
        val order = orderRepository.findById(id)

        return if (order.isPresent) {
            ResponseEntity.ok(order.get())
        } else {
            ResponseEntity.status(404).body(mapOf("message" to "Order with ID: $id not found."))
        }
    }
}

data class CreateOrderRequest(
    val items: List<Item>,
    val accountNumber: String,
    val deliveryAddress: String,
    val deliveryDate: LocalDate
)