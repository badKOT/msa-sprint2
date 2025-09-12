package self.project.practicum.s2.service.kafka

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Outgoing
import org.eclipse.microprofile.reactive.messaging.Message
import io.quarkus.runtime.StartupEvent
import java.util.stream.Stream 

@ApplicationScoped
class MessagingApplication {

    @Inject
    @Channel("words-out")
    lateinit var emitter: Emitter<String>

    /**
     * Sends message to the "words-out" channel, can be used from a JAX-RS resource or any bean of your application.
     * Messages are sent to the broker.
     *
    fun onStart(@Observes ev: StartupEvent) {
        println(ev)
        Stream.of("Hello", "with", "Quarkus", "Messaging", "message").forEach { str -> emitter.send(str) }
    }*/

    /**
     * Consume the message from the "words-in" channel, uppercase it and send it to the uppercase channel.
     * Messages come from the broker.
     **/
    @Incoming("words-in")
    @Outgoing("uppercase")
    fun toUpperCase(message: Message<String>): Message<String> {
        return message.withPayload(message.getPayload().uppercase())
    }

    /**
     * Consume the uppercase channel (in-memory) and print the messages.
     **/
    fun sink(word: String) {
        println(">> $word")
    }
}