package com.epam.edp.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.RawMessage;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Sends emails via AWS Simple Email Service (SES v2).
 *
 * <ul>
 *   <li>{@link #sendReport} – attaches the generated .xlsx and emails it to the agency head.</li>
 *   <li>{@link #sendCustomEmail} – manually send a plain-text or HTML email to any recipient.</li>
 * </ul>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final SesV2Client sesClient;

    @Value("${app.report.recipient-email}")
    private String recipientEmail;

    @Value("${app.ses.sender-email}")
    private String senderEmail;

    public EmailService(SesV2Client sesClient) {
        this.sesClient = sesClient;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scheduled / report send
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sends the weekly Excel report as a MIME email with an .xlsx attachment via AWS SES.
     *
     * @param reportBytes the .xlsx file bytes
     * @param weekStart   start of the reporting period
     * @param weekEnd     end of the reporting period
     */
    public void sendReport(byte[] reportBytes, LocalDate weekStart, LocalDate weekEnd) {
        String subject = String.format("Weekly Travel Agency Report: %s – %s",
                weekStart.format(DATE_FMT), weekEnd.format(DATE_FMT));

        String fileName = String.format("report_%s_%s.xlsx",
                weekStart.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                weekEnd.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        String body = String.format("""
                Dear Travel Agency Head,

                Please find attached the weekly performance report for the period %s to %s.

                The report contains:
                  • Agent Performance – bookings, ratings, and revenue per travel agent
                  • Sales Statistics  – bookings, ratings, and revenue per tour

                This report was generated automatically by the Travel Agency Reporting System.

                Best regards,
                Travel Agency Reporting System
                """, weekStart.format(DATE_FMT), weekEnd.format(DATE_FMT));

        try {
            byte[] rawMime = buildRawMimeWithAttachment(
                    senderEmail, recipientEmail, subject, body, reportBytes, fileName,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            sendRaw(rawMime);
            log.info("Weekly report emailed to {} via SES ({})", recipientEmail, fileName);
        } catch (MessagingException | IOException ex) {
            log.error("Failed to send weekly report email via SES: {}", ex.getMessage(), ex);
        } catch (SesV2Exception ex) {
            log.error("AWS SES error while sending report: {} (requestId={})",
                    ex.getMessage(), ex.requestId(), ex);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Manual send
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Manually sends a plain-text or HTML email to any recipient via AWS SES.
     * Useful for admin/test purposes or ad-hoc notifications.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param body    email body (plain text)
     * @param isHtml  {@code true} to treat {@code body} as HTML
     */
    public void sendCustomEmail(String to, String subject, String body, boolean isHtml) {
        try {
            Content subjectContent = Content.builder().data(subject).charset("UTF-8").build();

            Body emailBody = isHtml
                    ? Body.builder()
                            .html(Content.builder().data(body).charset("UTF-8").build())
                            .build()
                    : Body.builder()
                            .text(Content.builder().data(body).charset("UTF-8").build())
                            .build();

            Message message = Message.builder()
                    .subject(subjectContent)
                    .body(emailBody)
                    .build();

            SendEmailRequest request = SendEmailRequest.builder()
                    .fromEmailAddress(senderEmail)
                    .destination(Destination.builder().toAddresses(to).build())
                    .content(EmailContent.builder().simple(message).build())
                    .build();

            SendEmailResponse response = sesClient.sendEmail(request);
            log.info("Manual email sent to {} via SES. MessageId={}", to, response.messageId());
        } catch (SesV2Exception ex) {
            log.error("AWS SES error while sending manual email: {} (requestId={})",
                    ex.getMessage(), ex.requestId(), ex);
            throw ex;
        }
    }

    /**
     * Convenience overload – sends a plain-text email.
     */
    public void sendCustomEmail(String to, String subject, String body) {
        sendCustomEmail(to, subject, body, false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a raw MIME message with a plain-text body and a single binary attachment.
     */
    private byte[] buildRawMimeWithAttachment(
            String from, String to, String subject,
            String textBody, byte[] attachmentBytes,
            String attachmentName, String attachmentMimeType)
            throws MessagingException, IOException {

        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(from));
        mimeMessage.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
        mimeMessage.setSubject(subject, "UTF-8");

        // Body part
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(textBody, "UTF-8");

        // Attachment part
        MimeBodyPart attachPart = new MimeBodyPart();
        attachPart.setContent(attachmentBytes, attachmentMimeType);
        attachPart.setFileName(attachmentName);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachPart);
        mimeMessage.setContent(multipart);
        mimeMessage.saveChanges();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mimeMessage.writeTo(baos);
        return baos.toByteArray();
    }

    /**
     * Sends a pre-built raw MIME message via SES {@code SendRawEmail}.
     */
    private void sendRaw(byte[] rawMime) {
        RawMessage rawMessage = RawMessage.builder()
                .data(SdkBytes.fromByteArray(rawMime))
                .build();

        SendEmailRequest request = SendEmailRequest.builder()
                .content(EmailContent.builder()
                        .raw(rawMessage)
                        .build())
                .build();

        sesClient.sendEmail(request);
    }
}
