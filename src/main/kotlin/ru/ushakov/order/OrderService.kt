package ru.ushakov.order

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.ushakov.order.controller.CreateOrderRequest
import ru.ushakov.order.domain.Order
import ru.ushakov.order.domain.OrderStatus
import ru.ushakov.order.domain.TransactionRecord
import ru.ushakov.order.kafka.OrderCreatedEvent
import ru.ushakov.order.kafka.OrderEventProducer
import ru.ushakov.order.repository.OrderRepository
import ru.ushakov.order.repository.TransactionRecordRepository
import java.math.BigDecimal

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val transactionRecordRepository: TransactionRecordRepository,
    private val orderEventProducer: OrderEventProducer
) {

    @Transactional
    fun createOrder(request: CreateOrderRequest, transactionKey: String): Order {
        val existingRecord = transactionRecordRepository.findByTransactionKey(transactionKey)
        if (existingRecord != null) {
            return orderRepository.findById(existingRecord.orderId)
                .orElseThrow { IllegalStateException("Order not found for transactionKey: $transactionKey") }
        }

        val order = Order(
            items = request.items,
            totalPrice = request.items.sumOf { it.price.multiply(BigDecimal(it.quantity)) },
            accountNumber = request.accountNumber,
            deliveryAddress = request.deliveryAddress,
            deliveryDate = request.deliveryDate,
            status = OrderStatus.CREATED
        )
        orderRepository.save(order)

        val transactionRecord = TransactionRecord(
            transactionKey = transactionKey,
            orderId = order.id
        )
        transactionRecordRepository.save(transactionRecord)

        val event = OrderCreatedEvent(
            orderId = order.id,
            items = request.items,
            totalPrice = order.totalPrice,
            accountNumber = order.accountNumber,
            deliveryAddress = order.deliveryAddress,
            deliveryDate = order.deliveryDate
        )
        orderEventProducer.sendOrderCreatedEvent(event)

        return order
    }
}