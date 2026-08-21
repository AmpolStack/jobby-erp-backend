package com.jobby.infrastructure.adapter;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.ErrorType;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@DisplayName("EmailServiceAdapter - Unit Tests")
@ExtendWith(MockitoExtension.class)
class EmailServiceAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceAdapter adapter;

    @BeforeEach
    void setUp() {
        var session = Session.getInstance(new Properties());
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(session));
        adapter = new EmailServiceAdapter(mailSender, null);
    }

    @Test
    @DisplayName("send: success returns success")
    void givenValidParameters_whenSend_returnsSuccess() {
        var result = adapter.send("from@test.com", "to@test.com", "Subject", "<p>Body</p>");

        ResultAssertions.assertSuccess(result);
    }

    @Test
    @DisplayName("send: MailAuthenticationException returns ITS_CONFIGURATION_ERROR")
    void givenMailAuthenticationException_whenSend_returnsConfigurationError() {
        doThrow(new MailAuthenticationException("auth failed"))
                .when(mailSender).send(any(MimeMessage.class));

        var result = adapter.send("from@test.com", "to@test.com", "Subject", "Body");

        ResultAssertions.assertFailure(result, ErrorType.ITS_CONFIGURATION_ERROR);
    }

    @Test
    @DisplayName("send: MailSendException returns ITS_EXTERNAL_SERVICE_FAILURE")
    void givenMailSendException_whenSend_returnsExternalServiceFailure() {
        doThrow(new MailSendException("send failed", new RuntimeException("smtp error")))
                .when(mailSender).send(any(MimeMessage.class));

        var result = adapter.send("from@test.com", "to@test.com", "Subject", "Body");

        ResultAssertions.assertFailure(result, ErrorType.ITS_EXTERNAL_SERVICE_FAILURE);
    }

    @Test
    @DisplayName("send: generic MailException returns VALIDATION_ERROR")
    void givenGenericMailException_whenSend_returnsValidationError() {
        doThrow(new MailException("generic mail error") {})
                .when(mailSender).send(any(MimeMessage.class));

        var result = adapter.send("from@test.com", "to@test.com", "Subject", "Body");

        ResultAssertions.assertFailure(result, ErrorType.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("send: unhandled Exception returns ITS_UNKNOWN_ERROR")
    void givenUnhandledException_whenSend_returnsUnknownError() {
        doThrow(new RuntimeException("unexpected crash"))
                .when(mailSender).send(any(MimeMessage.class));

        var result = adapter.send("from@test.com", "to@test.com", "Subject", "Body");

        ResultAssertions.assertFailure(result, ErrorType.ITS_UNKNOWN_ERROR);
    }

    @Test
    @DisplayName("send: failure result contains mail service field")
    void givenMailAuthenticationException_whenSend_failureContainsMailServiceField() {
        doThrow(new MailAuthenticationException("bad credentials"))
                .when(mailSender).send(any(MimeMessage.class));

        var result = adapter.send("from@test.com", "to@test.com", "Subject", "Body");

        ResultAssertions.assertFailure(result);
        assertThat(result.error().getFields())
                .anySatisfy(field -> assertThat(field.getInstance()).isEqualTo("mail service"));
    }
}
