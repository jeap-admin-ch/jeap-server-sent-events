package ch.admin.bit.jeap.server.sent.events;

import ch.admin.bit.jeap.command.notify.client.NotifyClientCommand;
import ch.admin.bit.jeap.messaging.annotations.JeapMessageConsumerContract;
import ch.admin.bit.jeap.messaging.annotations.JeapMessageProducerContract;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@JeapMessageProducerContract(value = NotifyClientCommand.TypeRef.class, topic = "jeap-testapp-notifyclient")
@JeapMessageConsumerContract(value = NotifyClientCommand.TypeRef.class, topic = "jeap-testapp-notifyclient")
class TestApp {
}
