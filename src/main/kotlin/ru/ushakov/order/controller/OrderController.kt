package ru.ushakov.order.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.ushakov.order.OrderService
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
    private val orderService: OrderService,
    private val orderRepository: OrderRepository,
    private val orderEventProducer: OrderEventProducer
) {

    @PostMapping
    fun createOrder(
        @RequestHeader("X-Request-ID") transactionKey: String,
        @RequestBody request: CreateOrderRequest
    ): ResponseEntity<Any> {
        val order = orderService.createOrder(request, transactionKey)
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