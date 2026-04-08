package com.gym.gymtracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gymTrackerOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Gym Tracker API")
                .description("REST API для управления пользователями, тренировками, упражнениями и сетами")
                .version("v1")
                .contact(new Contact().name("Gym Tracker Team"))
                .license(new License().name("Apache 2.0")));
    }
}
