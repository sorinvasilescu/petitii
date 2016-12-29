package ro.petitii.service.email;

import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.petitii.config.SmtpConfig;
import ro.petitii.model.Email;
import ro.petitii.model.Attachment;
import ro.petitii.util.StringUtil;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

@Service
public class SmtpService {
    @Autowired
    private SmtpConfig config;

    private Session session = null;
    private Transport transport = null;
    private Properties props = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpService.class);

    public void send(Email email) throws MessagingException {
        LOGGER.info("Starting mail send");
        if (!transport.isConnected()) this.connect();
        Message message = new MimeMessage(session);
        MimeMultipart content = new MimeMultipart("mixed");
        message.setFrom(new InternetAddress(email.getSender()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getRecipients()));
        if (email.getCc() != null) message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(email.getCc()));
        if (email.getBcc() != null) message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(email.getBcc()));
        message.setSubject(email.getSubject());
        Multipart body = new MimeMultipart("alternative");
        // plain text body
        BodyPart part = new MimeBodyPart();
        part.setContent(StringUtil.toPlainText(email.getBody()),"text/plain; charset=utf-8");
        body.addBodyPart(part);
        // html text body
         part = new MimeBodyPart();
        part.setContent(email.getBody(),"text/html; charset=utf-8");
        body.addBodyPart(part);
        // add both to message
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(body);
        content.addBodyPart(bodyPart);
        // attachments
        if (email.getAttachments()!=null)
            for (Attachment att : email.getAttachments()) {
                part = new MimeBodyPart();
                part.setFileName(att.getOriginalFilename());
                DataSource source = new FileDataSource(att.getFilename());
                part.setDataHandler(new DataHandler(source));
                content.addBodyPart(part);
            }
        message.setContent(content);
        LOGGER.info("Mail constructed");
        message.saveChanges();
        LOGGER.info("Mail changes saved");
        transport.sendMessage(message,message.getAllRecipients());
        LOGGER.info("Mail sent successfully");
    }

    @Synchronized
    public void connect() throws MessagingException {
        LOGGER.info("Connecting to SMTP server");
        if (props == null) {
            props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", config.getSsl());
            props.put("mail.smtp.starttls.required", config.getSsl());
            props.put("mail.smtp.sendpartial", true);
            props.put("mail.smtp.host",config.getServer());
            props.put("mail.smtp.port",config.getPort());
        }
        if (session == null) {
            session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getUsername(), config.getPassword());
                }
            });
        }
        if (transport == null) {
            transport = session.getTransport("smtp");
        }
        transport.connect();
        LOGGER.info("Connected to SMTP server");
    }
}
