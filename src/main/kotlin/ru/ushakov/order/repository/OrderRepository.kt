package ru.ushakov.order.repository
import org.springframework.data.jpa.repository.JpaRepository
import ru.ushakov.order.domain.Order

interface OrderRepository : JpaRepository<Order, Long>