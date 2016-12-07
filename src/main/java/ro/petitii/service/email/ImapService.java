package ro.petitii.service.email;

import com.sun.mail.util.BASE64DecoderStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.petitii.model.Email;
import ro.petitii.service.EmailService;

import javax.mail.*;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

@Service
public class ImapService {

    @Autowired
    ImapConfig imapConfig;

    @Autowired
    EmailService emailService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImapConfig.class);
    private final static String protocol = "imap";

    public void getMail() {
        // open session
        Session session = Session.getInstance(getServerProperties());
        try {
            // connect to the message store
            Store store = session.getStore(protocol);
            store.connect(imapConfig.getUsername(),imapConfig.getPassword());
            // open folder
            Folder folder = store.getFolder("[Gmail]/All Mail");
            UIDFolder uidFolder = (UIDFolder)folder;
            folder.open(Folder.READ_ONLY);
            Message[] messages;
            long latestuid = -1;
            if (emailService.count()<1) {
                // set filters
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                df.setTimeZone(TimeZone.getTimeZone("EET"));
                Date date;
                try {
                    date = df.parse(imapConfig.getStartDate());
                } catch (ParseException e) {
                    date = df.parse("07/12/2016", new ParsePosition(0));
                    LOGGER.error("Could not parse date");
                }
                SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GT, date);
                // fetch messages
                messages = folder.search(newerThan);
            } else {
                latestuid = emailService.lastUid();
                LOGGER.info("Last uid: "+latestuid);
                messages = uidFolder.getMessagesByUID(latestuid,UIDFolder.LASTUID);
            }
            for (Message msg : messages) {
                long uid = uidFolder.getUID(msg);
                if (uid!=latestuid) saveMessage(msg, uid);
            }
        } catch (NoSuchProviderException e) {
            LOGGER.error("No such provider: " + e.getMessage());
        } catch (MessagingException e) {
            LOGGER.error("Messaging exception:" + e.getMessage());
        }
    }

    private void saveMessage(Message msg, long uid) throws MessagingException {
        Address[] fromAddress = msg.getFrom();
        String from = fromAddress[0].toString();
        String subject = msg.getSubject();
        String toList = parseAddresses(msg.getRecipients(Message.RecipientType.TO));
        String ccList = parseAddresses(msg.getRecipients(Message.RecipientType.CC));
        String bccList = parseAddresses(msg.getRecipients(Message.RecipientType.BCC));
        Date sentDate = msg.getSentDate();

        Object messageContent = null;
        String attachments = "";
        try {
            messageContent = msg.getContent();
            if (messageContent instanceof Multipart) {
                Multipart multipart = (Multipart) messageContent;
                for (int j=0; j<multipart.getCount(); j++) {
                    BodyPart bodyPart = multipart.getBodyPart(j);
                    if ((bodyPart.getContentType() == BodyPart.ATTACHMENT)||(bodyPart.getContent() instanceof BASE64DecoderStream)) {
                        if (attachments.length()>0) {
                            attachments += ", ";
                        }
                        attachments += bodyPart.getFileName();
                    } else {
                        messageContent = bodyPart.getContent().toString();
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not get message content");
        }

        Email email = new Email();
        email.setUid(uid);
        email.setSender(from);
        email.setRecipients(toList);
        email.setCc(ccList);
        email.setBcc(bccList);
        email.setSubject(subject);
        email.setBody(messageContent.toString());
        email.setDate(sentDate);
        email.setSize((float)(msg.getSize()));
        emailService.save(email);
        // print out details of each message
        System.out.println("Message #" + uid + ":");
        System.out.println("\t From: " + from);
        System.out.println("\t To: " + toList);
        System.out.println("\t CC: " + ccList);
        System.out.println("\t Subject: " + subject);
        System.out.println("\t Sent Date: " + sentDate);
        System.out.println("\t Message: " + messageContent);
        System.out.println("\t Attachments: " + attachments);
        System.out.println("\t Size: " + msg.getSize());
    }

    private Properties getServerProperties() {
        Properties properties = new Properties();
        // server setting
        properties.put(String.format("mail.%s.host", protocol), imapConfig.getServer());
        properties.put(String.format("mail.%s.port", protocol), imapConfig.getPort());
        // SSL setting
        properties.setProperty(String.format("mail.%s.ssl.enable", protocol), imapConfig.getSsl().toString());
        return properties;
    }

    private String parseAddresses(Address[] address) {
        String listAddress = "";
        if (address != null) {
            for (int i = 0; i < address.length; i++) {
                listAddress += address[i].toString() + ", ";
            }
        }
        if (listAddress.length() > 1) {
            listAddress = listAddress.substring(0, listAddress.length() - 2);
        }
        return listAddress;
    }

}
