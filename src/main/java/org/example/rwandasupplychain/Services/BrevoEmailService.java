package org.example.rwandasupplychain.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around Brevo's (formerly Sendinblue) transactional email API
 * (https://api.brevo.com/v3/smtp/email). Credentials come from BREVO_API,
 * BREVO_MAIL and BREVO_SENDER_NAME (see .env / application.yaml).
 * <p>
 * If no valid API key is configured (e.g. local/dev environments still holding
 * the placeholder value), email sending is skipped rather than throwing, so the
 * rest of the alert job (in-app/push notifications) can still run.
 */
@Service
public class BrevoEmailService {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailService.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;

    public BrevoEmailService(@Value("${app.brevo.base-url:https://api.brevo.com/v3/smtp/email}") String baseUrl,
                             @Value("${app.brevo.api-key:}") String apiKey,
                             @Value("${app.brevo.sender-email:no-reply@rwandasupplychain.com}") String senderEmail,
                             @Value("${app.brevo.sender-name:Rwanda Supply Chain}") String senderName) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey) && !apiKey.startsWith("add-");
    }

    public boolean sendEmail(List<String> toEmails, String subject, String htmlContent) {
        if (toEmails == null || toEmails.isEmpty()) {
            log.warn("Skipping email '{}' - no recipients configured", subject);
            return false;
        }
        if (!isConfigured()) {
            log.warn("Skipping email '{}' - BREVO_API key is not configured", subject);
            return false;
        }

        Map<String, Object> payload = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", toEmails.stream().map(email -> Map.of("email", email)).toList(),
                "subject", subject,
                "htmlContent", htmlContent
        );

        try {
            restClient.post()
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            log.error("Failed to send email '{}' via Brevo: {}", subject, e.getMessage());
            return false;
        }
    }
}