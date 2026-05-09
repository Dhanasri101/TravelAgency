package com.epam.edp.demo.config;

import com.epam.edp.demo.controller.AuthController;
import com.epam.edp.demo.security.JwtAuthenticationFilter;
import com.epam.edp.demo.security.JwtService;
import com.epam.edp.demo.service.UserService;
import org.mockito.Mockito;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @TestConfiguration
    static class TestBeans {
        @Bean
        UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean(name = "mongoMappingContext")
        MongoMappingContext mongoMappingContext() {
            return Mockito.mock(MongoMappingContext.class);
        }
    }

    @Test
    void passwordEncoder_usesConfiguredBcryptStrength() {
        String hash = passwordEncoder.encode("Strong#123");

        // BCrypt hash prefix includes the cost factor as two digits, e.g. $2a$12$
        org.junit.jupiter.api.Assertions.assertTrue(hash.matches("^\\$2[aby]\\$12\\$.+"));
    }

    @Test
    void bookingsEndpoint_withoutAuthentication_returnsConfiguredUnauthorizedJson() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/secure"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthenticated"))
            .andExpect(jsonPath("$.message").value("Authentication is required"))
            .andExpect(jsonPath("$.timestamp", startsWith("20")));
    }

    @Test
    void signInEndpoint_isPermitAllAndNotBlockedBySecurity() throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }
}



