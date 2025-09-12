package self.project.practicum.s2.client

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response
import io.smallrye.mutiny.Uni
import io.quarkus.rest.client.reactive.ClientExceptionMapper
import org.jboss.resteasy.reactive.RestResponse
import self.project.practicum.s2.dto.PromoCodeRsDto

@Path("/api")
@RegisterRestClient(configKey = "hotelio-api")
interface HotelioApiClient {

    @GET
    @Path("/users/{userId}/active")
    fun isUserActive(@PathParam("userId") userId: String): Uni<Boolean>

    @GET
    @Path("/users/{userId}/blacklisted")
    fun isUserBlacklisted(@PathParam("userId") userId: String): Uni<Boolean>

    @GET
    @Path("/hotels/{hotelId}/operational")
    fun isHotelOperational(@PathParam("hotelId") hotelId: String): Uni<Boolean>

    @GET
    @Path("/reviews/hotel/{hotelId}/trusted")
    fun isHotelTrusted(@PathParam("hotelId") hotelId: String): Uni<Boolean>

    @GET
    @Path("/hotels/{hotelId}/fully-booked")
    fun isHotelFullyBooked(@PathParam("hotelId") hotelId: String): Uni<Boolean>

    @GET
    @Path("/users/{userId}/status")
    fun getUserStatus(@PathParam("userId") userId: String): Uni<String?>

    @POST
    @Path("/promos/validate")
    fun validatePromoCode(
        @QueryParam("code") promoCode: String,
        @QueryParam("userId") userId: String
    ): RestResponse<PromoCodeRsDto?>

    companion object {
        @ClientExceptionMapper
        @JvmStatic
        fun toException(response: Response): RuntimeException? {
            if (response.getStatus() == 500) {
                return RuntimeException("The remote service responded with HTTP 500");
            }
            return null
        }
    }
}
