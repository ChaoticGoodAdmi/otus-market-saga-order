package ru.ushakov.order.domain

import jakarta.persistence.*

@Entity
@Table(name = "transaction_records")
data class TransactionRecord(
    @Id
    @Column(nullable = false, unique = true)
    val transactionKey: String,

    @Column(nullable = false)
    val orderId: Long
) {
    constructor() : this("", 0)
}