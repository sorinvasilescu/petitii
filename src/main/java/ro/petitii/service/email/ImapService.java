package ro.petitii.service.email;

import com.sun.mail.util.BASE64DecoderStream;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.petitii.config.ImapConfig;
import ro.petitii.model.Email;
import ro.petitii.model.EmailAttachment;
import ro.petitii.service.EmailService;

import javax.mail.*;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ImapService {
    @Autowired
    ImapConfig imapConfig;

    @Autowired
    EmailService emailService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImapService.class);
    private final static String protocol = "imap";

    public synchronized void getMail() throws IOException, MessagingException {
        // set -Djava.security.egd=file:///dev/urandom
        LOGGER.info("Fetching mail");
        Date startDate = new Date();
        // set session properties
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.imap.partialfetch", "false");
        props.setProperty("mail.imaps.partialfetch", "false");
        props.setProperty("mail.imap.fetchsize", "1000000");
        // open session
        Session session = Session.getInstance(getServerProperties());
        Folder folder = null;
        try {
            // connect to the message store
            Store store = session.getStore(protocol);
            store.connect(imapConfig.getUsername(), imapConfig.getPassword());
            // open folder
            folder = store.getFolder("[Gmail]/All Mail");
            UIDFolder uidFolder = (UIDFolder) folder;
            folder.open(Folder.READ_ONLY);
            Message[] messages;
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.FLAGS);
            fp.add(FetchProfile.Item.CONTENT_INFO);
            long latestuid = -1;
            if (emailService.count() < 1) {
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
                LOGGER.info("Last uid: " + latestuid);
                messages = uidFolder.getMessagesByUID(latestuid, UIDFolder.LASTUID);
            }
            folder.fetch(messages,fp);
            for (Message msg : messages) {
                long uid = uidFolder.getUID(msg);
                if (uid != latestuid) saveMessage(msg, uid);
            }
        } catch (NoSuchProviderException e) {
            LOGGER.error("No such provider: " + e.getMessage());
            throw e;
        } catch (MessagingException e) {
            LOGGER.error("Messaging exception:" + e.getMessage());
            throw e;
        } finally {
            if (folder != null)
                try { folder.close(false); } catch (MessagingException e) {
                    LOGGER.error("Messaging exception:" + e.getMessage());
                }
            Date endDate = new Date();
            float time = (float)(endDate.getTime() - startDate.getTime())/1000;
            LOGGER.info("Fetch mail time: " + time);
        }
    }

    private void saveMessage(Message msg, long uid) throws MessagingException, IOException {
        Address[] fromAddress = msg.getFrom();
        String from = fromAddress[0].toString();
        String subject = msg.getSubject();
        String toList = parseAddresses(msg.getRecipients(Message.RecipientType.TO));
        String ccList = parseAddresses(msg.getRecipients(Message.RecipientType.CC));
        String bccList = parseAddresses(msg.getRecipients(Message.RecipientType.BCC));
        Date sentDate = msg.getSentDate();

        Collection<EmailAttachment> attachments = new ArrayList<>();
        Object messageContent = parseBody(msg.getContent(), attachments);

        Email email = new Email();
        email.setUid(uid);
        email.setSender(from);
        email.setRecipients(toList);
        email.setCc(ccList);
        email.setBcc(bccList);
        email.setSubject(subject);
        if (messageContent != null) {
            // required to prevent image tracking and js injection in our UI
            // if the sender did send something not parsed correctly, probably it is not important
            email.setBody(Jsoup.clean(preserveNewLines(messageContent.toString()), Whitelist.relaxed()));
        }
        email.setDate(sentDate);
        email.setSize((float) (msg.getSize()));
        email.setAttachments(attachments);
        email.setType(Email.EmailType.Inbox);
        emailService.save(email);
        // print out details of each message
        LOGGER.info("Message #" + uid + ":");
        LOGGER.info("\t From: " + from);
        LOGGER.info("\t To: " + toList);
        LOGGER.info("\t CC: " + ccList);
        LOGGER.info("\t Subject: " + subject);
        LOGGER.info("\t Sent Date: " + sentDate);
        LOGGER.info("\t Message: " + email.getBody());
        String att = "";
        for (EmailAttachment a : attachments) {
            if (att.length() > 0) att += ",";
            att += a.getOriginalFilename();
        }
        LOGGER.info("\t Attachments: " + att);
        LOGGER.info("\t Size: " + msg.getSize());
    }

    private String parseBody(Object content, Collection<EmailAttachment> attachments)
            throws IOException, MessagingException {
        String body = null;
        try {
            if (content != null) {
                if (content instanceof Multipart) {
                    Multipart multipart = (Multipart) content;
                    for (int j = 0; j < multipart.getCount(); j++) {
                        BodyPart bodyPart = multipart.getBodyPart(j);
                        // todo; check inline
                        if (isTextBody(bodyPart)) {
                            body = bodyPart.getContent().toString();
                        } else if (isAttachment(bodyPart)) {
                            EmailAttachment attachment = new EmailAttachment();
                            attachment.setBodyPart(bodyPart);
                            attachments.add(attachment);
                        } else if (bodyPart.getContent() instanceof Multipart) {
                            // inception
                            body = parseBody(bodyPart.getContent(), attachments);
                        } else if (body == null) {
                            // do not override existing results
                            body = bodyPart.getContent().toString();
                        }
                    }
                } else {
                    body = content.toString();
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not get message content");
        }

        return body;
    }

    private boolean isTextBody(BodyPart bodyPart) throws MessagingException {
        return bodyPart.getContentType().toLowerCase().contains("text");
    }

    private boolean isAttachment(BodyPart bodyPart) throws MessagingException, IOException {
        return bodyPart.getContentType().equals(BodyPart.ATTACHMENT) || bodyPart.getContent() instanceof BASE64DecoderStream;
    }

    private String preserveNewLines(String content) {
        return content.replaceAll("\n", "<br />");
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

    private String parseAddresses(Address[] addresses) {
        String listAddress = "";
        if (addresses != null) {
            for (Address address : addresses) {
                listAddress += address.toString() + ", ";
            }
        }
        if (listAddress.length() > 1) {
            listAddress = listAddress.substring(0, listAddress.length() - 2);
        }
        return listAddress;
    }
}
