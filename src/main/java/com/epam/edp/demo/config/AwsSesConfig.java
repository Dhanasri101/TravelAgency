package com.epam.edp.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

/**
 * Creates and configures the AWS SES v2 client bean.
 *
 * <p>Credentials are resolved in this order:
 * <ol>
 *   <li>Explicit key + secret + optional session-token (static / temporary credentials)</li>
 *   <li>AWS Default Credential Provider Chain (IAM role, ~/.aws/credentials, etc.)</li>
 * </ol>
 */
@Configuration
public class AwsSesConfig {

    @Value("${aws.ses.region:us-east-1}")
    private String region;

    @Value("${aws.ses.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.ses.secret-access-key:}")
    private String secretAccessKey;

    /** Optional session token for temporary (STS) credentials */
    @Value("${aws.ses.session-token:}")
    private String sessionToken;

    @Bean
    public SesV2Client sesV2Client() {
        var builder = SesV2Client.builder()
                .region(Region.of(region));

        if (!accessKeyId.isBlank() && !secretAccessKey.isBlank()) {
            // Use AwsSessionCredentials so session-token is included when present
            AwsSessionCredentials creds = AwsSessionCredentials.create(
                    accessKeyId,
                    secretAccessKey,
                    sessionToken.isBlank() ? null : sessionToken
            );
            builder.credentialsProvider(StaticCredentialsProvider.create(creds));
        } else {
            // Fall back to IAM role / env vars / ~/.aws/credentials
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
