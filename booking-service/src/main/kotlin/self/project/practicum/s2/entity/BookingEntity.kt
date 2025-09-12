package self.project.practicum.s2.entity

import jakarta.persistence.*
import java.time.Instant
import self.project.practicum.s2.hotelio.booking.BookingResponse
import com.google.protobuf.StringValue
import com.google.protobuf.DoubleValue
import org.hibernate.annotations.CreationTimestamp

@Entity
@Table(name = "booking")
class BookingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "user_id")
    var userId: String? = null
    @Column(name = "hotel_id")
    var hotelId: String? = null

    @Column(name = "promo_code")
    var promoCode: String? = null
    @Column(name = "discount_percent")
    var discountPercent: Double? = null
    @Column(nullable = false)
    var price: Double? = null

    @Column(name = "created_at")
    @CreationTimestamp
    var createdAt: Instant? = null
}

fun BookingEntity.toBookingResponse(): BookingResponse = 
    BookingResponse.newBuilder()
        .setId(this.id?.toString())
        .setUserId(this.userId)
        .setHotelId(this.hotelId)
        .setPromoCode(this.promoCode ?: "")
        .setDiscountPercent(this.discountPercent ?: 0.0)
        .setPrice(this.price ?: 0.0)
        .setCreatedAt(this.createdAt.toString())
        .build()
