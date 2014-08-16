package javaone.web;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.resourceadapter.VertxConnection;
import org.vertx.java.resourceadapter.VertxConnectionFactory;

@WebServlet(urlPatterns = {"/notification"}, asyncSupported = true)
public class NotificationServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(NotificationServlet.class.getName());

    private static final String JNDI_NAME = "java:/eis/VertxConnectionFactory";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");

        final String address = request.getParameter("address");
        final String message = request.getParameter("message");

        if (address != null && address.trim().length() > 0
                && message != null && message.trim().length() > 0) {

            final AsyncContext async = request.startAsync();

            async.setTimeout(1000);
            async.start(new Runnable() {

                @Override
                public void run() {
                    InitialContext ctx = null;
                    VertxConnection conn = null;
                    try {
                        ctx = new InitialContext();
                        VertxConnectionFactory connFactory = (VertxConnectionFactory) ctx.lookup(JNDI_NAME);
                        conn = connFactory.getVertxConnection();
                        conn.eventBus().send(address, message, new Handler<Message>() {
                            @Override
                            public void handle(Message message) {
                                try {
                                    final ServletOutputStream out = async.getResponse().getOutputStream();
                                    out.print(message.body().toString());
                                    out.flush();

                                    async.complete();
                                } catch (IOException ex) {
                                    LOGGER.log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                    } catch (NamingException e) {
                        LOGGER.log(Level.SEVERE, null, e);
                    } catch (ResourceException e) {
                        LOGGER.log(Level.SEVERE, null, e);
                    } finally {
                        if (ctx != null) {
                            try {
                                ctx.close();
                            } catch (NamingException ex) {
                                LOGGER.log(Level.SEVERE, null, ex);
                            }
                        }
                        if (conn != null) {
                            try {
                                conn.close();
                            } catch (ResourceException ex) {
                                LOGGER.log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            });
        } else {
            response.sendError(400, "Wrong parameters");
        }
    }

}
