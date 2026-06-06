package com.jobby.infrastructure.adapter;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@AllArgsConstructor
public class EmailServiceAdapter implements EmailService {

    private final JavaMailSender mailSender;

    private final static String DEFAULT_ENCODING = "UTF-8";

    @Override
    public Result<Void, Error> send(String from, String to,
                                    String subject, String message) {

        MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, DEFAULT_ENCODING);

        try{
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message, true);
            this.mailSender.send(mimeMessage);
        }
        catch (MailAuthenticationException ex){
            return Result.failure(ErrorType.ITS_CONFIGURATION_ERROR,
                    new Field("mail service",
                            "An authentication error have been detected: " + ex));
        }
        catch (MessagingException ex) {
            return Result.failure(ErrorType.ITS_OPERATION_ERROR, new Field("mail service",
                    "An error has been detected in one mail setup: " + ex));
        }
        catch (MailSendException ex){
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE, new Field("mail service",
                    "Error in email sending: "  + ex));
        }
        catch (MailException ex){
            return Result.failure(ErrorType.VALIDATION_ERROR, new Field("email service",
                    "An error in email service has been detected: " + ex));
        }
        catch (Exception ex){
            return Result.failure(ErrorType.ITS_UNKNOWN_ERROR, new Field("email service",
                    ex.toString()));
        }

        return Result.success();
    }
}
