package self.project.practicum.s2.service.grpc

import io.quarkus.grpc.GrpcService
import io.smallrye.mutiny.Uni
import java.time.Duration
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.slf4j.LoggerFactory
import self.project.practicum.s2.hotelio.booking.BookingListRequest
import self.project.practicum.s2.hotelio.booking.BookingListResponse
import self.project.practicum.s2.hotelio.booking.BookingRequest
import self.project.practicum.s2.hotelio.booking.BookingResponse
import self.project.practicum.s2.hotelio.booking.BookingService
import self.project.practicum.s2.entity.toBookingResponse
import self.project.practicum.s2.repository.BookingRepository
import self.project.practicum.s2.entity.BookingEntity
import self.project.practicum.s2.client.HotelioApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import com.google.protobuf.StringValue
import com.google.protobuf.DoubleValue
import jakarta.transaction.Transactional

@GrpcService
class BookingGrpcService(
        val repository: BookingRepository,
        @RestClient val httpClient: HotelioApiClient
) : BookingService {

    var log = LoggerFactory.getLogger(BookingGrpcService::class.java)
    val fiveSeconds: Duration = Duration.ofSeconds(5)
    val executor = Dispatchers.IO.asExecutor()

    override fun listBookings(request: BookingListRequest): Uni<BookingListResponse> {
        return Uni.createFrom()
                .item { request }
                .emitOn(executor)
                .map { rq -> 
                    log.info("Got request to list bookings with parameters $rq")
                    val bookings =
                    if (rq.userId != null) {
                        repository.findByUserId(rq.userId)
                    } else {
                        repository.listAll()
                    }
                    log.info("Found ${bookings.size} bookings. 'bout to parse 'em")
                    val rsBuilder = BookingListResponse.newBuilder()
                    bookings.forEach { elem ->
                        rsBuilder.addBookings(elem.toBookingResponse())
                    }
                    log.info("Found ${bookings.size} booking records. Returning them...")
                    rsBuilder.build()
                }
    }

    override fun createBooking(request: BookingRequest): Uni<BookingResponse> {
        return Uni.createFrom()
                .item { request }
                .emitOn(executor)
                .map { rq -> 
                    log.info(
                            "Creating booking: userId={}, hotelId={}, promoCode={}",
                            rq.userId,
                            rq.hotelId,
                            rq.promoCode
                    )
                    validateUser(rq.userId)
                    validateHotel(rq.hotelId)
                    log.info("Validation passed successfully")

                    val basePrice = resolveBasePrice(rq.userId)
                    val discount = resolvePromoDiscount(rq.promoCode, rq.userId)

                    val finalPrice = basePrice - discount
                    log.info(
                            "Final price calculated: base={}, discount={}, final={}",
                            basePrice,
                            discount,
                            finalPrice
                    )

                    var booking =
                            BookingEntity().apply {
                                this.userId = rq.userId
                                this.hotelId = rq.hotelId
                                this.promoCode = rq.promoCode
                                this.discountPercent = discount
                                this.price = finalPrice
                            }

                    val rs = save(booking).toBookingResponse()
                    log.info("Returning $rs")
                    rs
                }
    }

    @Transactional
    internal fun save(booking: BookingEntity): BookingEntity {
        repository.persistAndFlush(booking)
        return repository.findById(booking.id!!)!!
    }

    private fun validateUser(userId: String) {
        httpClient
                .isUserActive(userId)
                .invoke { active ->
                    if (active == false) {
                        log.warn("User {} is inactive", userId)
                        throw IllegalArgumentException("User is inactive")
                    }
                }
                .flatMap { httpClient.isUserBlacklisted(userId) }
                .invoke { blacklisted ->
                    if (blacklisted == true) {
                        log.warn("User {} is blacklisted", userId)
                        throw IllegalArgumentException("User is blacklisted")
                    }
                }
                .await()
                .atMost(fiveSeconds)
    }

    private fun validateHotel(hotelId: String) {
        httpClient
                .isHotelOperational(hotelId)
                .invoke { operational ->
                    if (operational == false) {
                        log.warn("Hotel {} is not operational", hotelId)
                        throw IllegalArgumentException("Hotel is not operational")
                    }
                }
                .flatMap { httpClient.isHotelTrusted(hotelId) }
                .invoke { trusted ->
                    if (trusted == false) {
                        log.warn("Hotel {} is not trusted", hotelId)
                        throw IllegalArgumentException("Hotel is not trusted based on reviews")
                    }
                }
                .flatMap { httpClient.isHotelFullyBooked(hotelId) }
                .invoke { booked ->
                    if (booked == true) {
                        log.warn("Hotel {} is fully booked", hotelId)
                        throw IllegalArgumentException("Hotel is fully booked")
                    }
                }
                .await()
                .atMost(fiveSeconds)
    }

    private fun resolveBasePrice(userId: String): Double {
        return httpClient
                .getUserStatus(userId)
                .map { status ->
                    if (status != null) {
                        log.debug("User {} has status '{}'", userId, status)
                        if (status.equals("VIP", ignoreCase = true)) return@map 80.0
                    } else {
                        log.debug("User {} has unknown status, default base price 100.0", userId)
                    }
                    return@map 100.0
                }
                .await()
                .atMost(fiveSeconds)
    }

    private fun resolvePromoDiscount(promoCode: String?, userId: String): Double {
        if (promoCode == null) return 0.0

        val response = httpClient.validatePromoCode(promoCode, userId)
        if (response.statusInfo.statusCode == 200 && response.hasEntity()) {
            val promoCodeDto = response.entity!!
            log.debug(
                    "Promo code '{}' applied with discount {}",
                    promoCodeDto,
                    promoCodeDto.discount
            )
            return promoCodeDto.discount
        }

        log.debug("Promo code '{}' is invalid or not applicable for user {}", promoCode, userId)
        return 0.0
    }
}
