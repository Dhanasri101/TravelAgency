package com.epam.edp.demo.dto;

/**
 * Request body for the manual email send endpoint.
 *
 * <pre>
 * POST /api/emails/send
 * {
 *   "to": "recipient@example.com",
 *   "subject": "Hello",
 *   "body": "Some text or &lt;b&gt;HTML&lt;/b&gt;",
 *   "html": false
 * }
 * </pre>
 */
public class ManualEmailRequest {

    /** Recipient email address. */
    private String to;

    /** Email subject line. */
    private String subject;

    /** Email body – plain text or HTML depending on {@link #html}. */
    private String body;

    /**
     * When {@code true} the {@link #body} is treated as HTML;
     * otherwise it is sent as plain text (default: {@code false}).
     */
    private boolean html = false;

    // ── Constructors ──────────────────────────────────────────────────────────

    public ManualEmailRequest() {}

    public ManualEmailRequest(String to, String subject, String body, boolean html) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.html = html;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public boolean isHtml() { return html; }
    public void setHtml(boolean html) { this.html = html; }
}

