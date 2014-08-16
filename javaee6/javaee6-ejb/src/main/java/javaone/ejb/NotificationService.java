package javaone.ejb;

import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.jboss.ejb3.annotation.ResourceAdapter;
import org.vertx.java.core.eventbus.Message;

import org.vertx.java.resourceadapter.inflow.VertxListener;

@MessageDriven(name = "NotificationService",
        messageListenerInterface = VertxListener.class,
        activationConfig = {
            @ActivationConfigProperty(propertyName = "address", propertyValue = "inbound-address"),})
@ResourceAdapter("jca-adaptor-1.0.3.rar")
public class NotificationService implements VertxListener {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    public NotificationService() {
        LOGGER.info("NotificationService started.");
    }

    @Override
    public <String> void onMessage(Message<String> message) {
        LOGGER.info("Get a message from Vert.x");
        final String body = message.body();
        LOGGER.info("Body of the message: " + body.toString());
        message.reply("Hi, Got your message: " + body.toString());
    }

}
