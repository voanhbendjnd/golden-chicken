package vn.edu.fpt.golden_chicken.services;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MailService {
    JavaMailSender javaMailSender;
    SpringTemplateEngine templateEngine;

    public void configBeforeSendForStaff(String name, String email, String password) {
        this.sendEmailAndPasswordForStaff(email, "Password At Golden Chicken", "mail/sas", email, name,
                password);
    }

    public void allowMailForUser(String name, String email) {
        this.sendNotice(email, "Register Success", "mail/anu", email, name);
    }

    @Async
    public void sendNotice(String to, String subject, String template, String email, String name) {
        var ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("email", email);
        var at = new ArrayList<FileSystemResource>();
        var content = this.templateEngine.process(template, ctx);
        this.sendEmailSync(to, subject, content, false, true, at);
    }

    // turn on enalbleAsync in application
    @Async
    public void sendEmailAndPasswordForStaff(String to, String subject, String template, String email, String name,
            String password) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("password", password);
        ctx.setVariable("email", email);
        var at = new ArrayList<FileSystemResource>();
        var content = this.templateEngine.process(template, ctx);
        this.sendEmailSync(to, subject, content, false, true, at);

    }

    public void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml,
            List<FileSystemResource> attachments) {
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        try {
            var msg = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(content, isHtml);
            if (isMultipart) {
                for (var file : attachments) {
                    msg.addInline(file.getFilename(), file);
                }
            }
            this.javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            System.out.println("ERROR WITH SEND EMAIL");
            System.out.println("DESCRIPTION: " + e);
        }
    }

}
