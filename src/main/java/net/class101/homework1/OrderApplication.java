package net.class101.homework1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(OrderApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);

        // Call the run method
        app.run(args);
    }

}
