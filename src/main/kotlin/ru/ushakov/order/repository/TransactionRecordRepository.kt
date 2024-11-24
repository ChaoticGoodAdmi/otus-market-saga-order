package ru.ushakov.order.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.ushakov.order.domain.TransactionRecord

interface TransactionRecordRepository : JpaRepository<TransactionRecord, String> {
    fun findByTransactionKey(transactionKey: String): TransactionRecord?
}