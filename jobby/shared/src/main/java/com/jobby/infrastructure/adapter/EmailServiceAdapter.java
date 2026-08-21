package com.jobby.infrastructure.adapter;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.EmailService;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Slf4j
@AllArgsConstructor
public class EmailServiceAdapter implements EmailService {

    private final JavaMailSender mailSender;
    private final ObservationRegistry observationRegistry;

    private final static String DEFAULT_ENCODING = "UTF-8";

    @Override
    public Result<Void, Error> send(String from, String to,
                                    String subject, String message) {

        var observation = Observation.createNotStarted("email.send", observationRegistry).start();
        MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, DEFAULT_ENCODING);

        try{
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message, true);
            this.mailSender.send(mimeMessage);
            observation.stop();
            return Result.success();
        }
        catch (MailAuthenticationException ex){
            observation.error(ex);
            log.error("[ITS_CONFIGURATION_ERROR] Mail server authentication failed: from={}, to={}", from, to, ex);
            return Result.failure(ErrorType.ITS_CONFIGURATION_ERROR,
                    new Field("mail service",
                            ex.getClass().getSimpleName() + ": authentication error"));
        }
        catch (MessagingException ex) {
            observation.error(ex);
            log.error("[ITS_OPERATION_ERROR] Mail message construction failed: to={}", to, ex);
            return Result.failure(ErrorType.ITS_OPERATION_ERROR, new Field("mail service",
                    ex.getClass().getSimpleName() + ": mail setup error"));
        }
        catch (MailSendException ex){
            observation.error(ex);
            log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] SMTP send failed: to={}", to, ex);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE, new Field("mail service",
                    ex.getClass().getSimpleName() + ": error in email sending"));
        }
        catch (MailException ex){
            observation.error(ex);
            log.warn("[VALIDATION_ERROR] Mail service rejected request: to={}", to, ex);
            return Result.failure(ErrorType.VALIDATION_ERROR, new Field("email service",
                    ex.getClass().getSimpleName() + ": email service error"));
        }
        catch (Exception ex){
            observation.error(ex);
            log.error("[ITS_UNKNOWN_ERROR] Unexpected mail failure: to={}", to, ex);
            return Result.failure(ErrorType.ITS_UNKNOWN_ERROR, new Field("email service",
                    ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
    }
}
