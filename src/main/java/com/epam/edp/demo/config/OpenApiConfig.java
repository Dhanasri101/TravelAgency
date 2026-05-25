package com.epam.edp.demo.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Provide JWT token as: Bearer <token>"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(HttpServletRequest request) {
        // Dynamically build server URL from the incoming request
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        
        String serverUrl;
        // Don't include standard ports in the URL
        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            serverUrl = scheme + "://" + serverName;
        } else {
            serverUrl = scheme + "://" + serverName + ":" + serverPort;
        }

        Server currentServer = new Server()
                .url(serverUrl)
                .description("Current environment - automatically detected");

        Server productionServer = new Server()
                .url("https://sprint1-run23-team3-develop-dev-deploy.development.krci-dev.cloudmentor.academy")
                .description("Production environment");

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local development");

        return new OpenAPI()
                .info(new Info()
                        .title("Travel Agency API")
                        .version("v1")
                        .description("REST API for authentication, tours, and bookings.")
                        .contact(new Contact().name("Travel Agency Team")))
                .servers(List.of(productionServer, localServer, currentServer));
    }
}
