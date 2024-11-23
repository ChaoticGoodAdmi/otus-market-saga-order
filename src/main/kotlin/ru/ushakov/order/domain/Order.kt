package ru.ushakov.order.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ElementCollection
    val items: List<Item>,
    val totalPrice: BigDecimal,
    val accountNumber: String,
    val deliveryAddress: String,
    val deliveryDate: LocalDate,
    var status: OrderStatus
) {
    constructor() : this(0, emptyList(), BigDecimal.ZERO, "", "", LocalDate.now(), OrderStatus.CREATED)
}

enum class OrderStatus {
    CREATED, PAID, RESERVED, PENDING_DELIVERY, READY_TO_DELIVER, CANCELLED_BY_BILLING, CANCELLED_BY_WAREHOUSE
}

@Embeddable
data class Item(
    val name: String,
    val price: BigDecimal,
    val quantity: Int
) {
    constructor() : this("", BigDecimal.ZERO, 0)


}