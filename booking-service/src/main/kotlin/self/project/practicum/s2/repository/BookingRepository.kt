package self.project.practicum.s2.repository

import jakarta.enterprise.context.ApplicationScoped
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import self.project.practicum.s2.entity.BookingEntity

@ApplicationScoped
class BookingRepository: PanacheRepository<BookingEntity> {
    fun findByUserId(userId: String) = list("userId", userId)
}