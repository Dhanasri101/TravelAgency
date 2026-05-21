package com.epam.edp.demo.controller;

import com.epam.edp.demo.dto.ManualEmailRequest;
import com.epam.edp.demo.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailControllerTest {

    @Mock EmailService emailService;
    @InjectMocks EmailController controller;

    @Test
    void sendEmail_missingTo_returns400() {
        ManualEmailRequest req = new ManualEmailRequest();
        req.setSubject("Hi");
        req.setBody("Body");

        ResponseEntity<Map<String, String>> resp = controller.sendEmail(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).containsKey("error");
    }

    @Test
    void sendEmail_missingSubject_returns400() {
        ManualEmailRequest req = new ManualEmailRequest();
        req.setTo("test@test.com");
        req.setBody("Body");

        ResponseEntity<Map<String, String>> resp = controller.sendEmail(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void sendEmail_missingBody_returns400() {
        ManualEmailRequest req = new ManualEmailRequest();
        req.setTo("test@test.com");
        req.setSubject("Hello");

        ResponseEntity<Map<String, String>> resp = controller.sendEmail(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void sendEmail_valid_returns200() {
        ManualEmailRequest req = new ManualEmailRequest();
        req.setTo("to@test.com");
        req.setSubject("Hello");
        req.setBody("World");

        doNothing().when(emailService).sendCustomEmail("to@test.com", "Hello", "World", false);

        ResponseEntity<Map<String, String>> resp = controller.sendEmail(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsEntry("status", "sent");
        assertThat(resp.getBody()).containsEntry("to", "to@test.com");
        verify(emailService).sendCustomEmail("to@test.com", "Hello", "World", false);
    }

    @Test
    void sendEmail_htmlTrue_passesHtmlFlag() {
        ManualEmailRequest req = new ManualEmailRequest();
        req.setTo("to@test.com");
        req.setSubject("Hi");
        req.setBody("<b>Hello</b>");
        req.setHtml(true);

        doNothing().when(emailService).sendCustomEmail("to@test.com", "Hi", "<b>Hello</b>", true);

        ResponseEntity<Map<String, String>> resp = controller.sendEmail(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(emailService).sendCustomEmail("to@test.com", "Hi", "<b>Hello</b>", true);
    }

    @Test
    void sendEmail_sesException_returns502() {
        ManualEmailRequest req = new ManualEmailRequest();
        req.setTo("bad@bad.com");
        req.setSubject("Hi");
        req.setBody("Body");

        SesV2Exception ex = (SesV2Exception) SesV2Exception.builder()
                .message("Sending paused")
                .awsErrorDetails(AwsErrorDetails.builder().errorMessage("Sending paused").build())
                .build();
        doThrow(ex).when(emailService).sendCustomEmail("bad@bad.com", "Hi", "Body", false);

        ResponseEntity<Map<String, String>> resp = controller.sendEmail(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(resp.getBody()).containsKey("error");
    }
}

