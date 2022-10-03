package no.ssb.notification.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Altinn standalone notification main application class
 */
@SpringBootApplication
public class NotificationApi {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(NotificationApi.class);
        app.run();
    }

}
