package ro.petitii.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.petitii.config.SmtpConfig;
import ro.petitii.model.Email;
import ro.petitii.model.EmailAttachment;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

@Service
public class SmtpService {

    @Autowired
    SmtpConfig config;

    public void send(Email email) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", config.getSsl());
        props.put("mail.smtp.host", config.getServer());
        props.put("mail.smtp.port", config.getPort());

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getUsername(), config.getPassword());
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(config.getUsername()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getRecipients()));
        if (email.getCc() != null) message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(email.getCc()));
        if (email.getBcc() != null) message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(email.getBcc()));
        message.setSubject(email.getSubject());
        Multipart content = new MimeMultipart();
        BodyPart part = new MimeBodyPart();
        part.setText(email.getBody());
        content.addBodyPart(part);
        if (email.getAttachments() != null)
            for (EmailAttachment att : email.getAttachments()) {
                content.addBodyPart(att.getBodyPart());
            }
        message.setContent(content);
        Transport.send(message);
    }
}
