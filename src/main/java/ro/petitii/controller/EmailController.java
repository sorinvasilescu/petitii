package ro.petitii.controller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.ModelAndView;
import ro.petitii.config.EmailAttachmentConfig;
import ro.petitii.config.ImapConfig;
import ro.petitii.model.Email;
import ro.petitii.model.Attachment;
import ro.petitii.service.AttachmentService;
import ro.petitii.service.EmailService;
import ro.petitii.util.Pair;
import ro.petitii.util.ZipUtils;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@Controller
public class EmailController extends ControllerBase {

    @Autowired
    ImapConfig config;

    @Autowired
    EmailAttachmentConfig attConfig;

    @Autowired
    EmailService emailService;

    @Autowired
    AttachmentService attachmentService;

    @Autowired
    MessageSource messageSource;

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailController.class);

    @RequestMapping("/inbox")
    public ModelAndView inbox() {
        ModelAndView modelAndView = new ModelAndView("email_page");
        modelAndView.addObject("page", "inbox");
        modelAndView.addObject("title", "Email-uri primite");
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("restUrl", "/rest/emails");
        //modelAndView.addObject("toast",this.createToast("Lorem ipsum sit amet dolor",ToastType.info));

        return modelAndView;
    }

    @RequestMapping("/spam")
    public ModelAndView spam() {
        ModelAndView modelAndView = new ModelAndView("email_page");
        modelAndView.addObject("page", "spam");
        modelAndView.addObject("title", "Spam");
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("restUrl", "/rest/spam");
        return modelAndView;
    }

    @RequestMapping("/emailDetails")
    public ModelAndView emailDetails(@RequestParam("id") Long id) {
        Email email = emailService.searchById(id);
        if (email == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

        ModelAndView modelAndView = new ModelAndView("email_detail");
        modelAndView.addObject("page", "Detalii");
        modelAndView.addObject("title", "Email-uri primite");
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("data", email);
        if (email.getPetition()!=null) {
            modelAndView.addObject("status", messageSource.getMessage(email.getPetition().statusString(), null , new Locale("ro")));
        }
        return modelAndView;
    }

    @RequestMapping("/action/download/{id}")
    public void download(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            Attachment att = attachmentService.findById(id);
            Path filepath = Paths.get(att.getFilename());
            FileInputStream is = new FileInputStream(new File(filepath.toUri()));
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment; filename=" + att.getOriginalFilename());
            IOUtils.copy(is, response.getOutputStream());
            is.close();
            response.flushBuffer();
        } catch (IOException e) {
            LOGGER.error("Could not find attachment with id " + id + " on disk: " + e.getMessage());
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find attachment with id " + id + ": " + e.getMessage());
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping("/action/download/email/{id}")
    public void downloadAll(@PathVariable("id") Long id, HttpServletResponse response) {
        try {

            Email email = emailService.searchById(id);
            if (email == null) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
            }

            List<Pair<String, Path>> attachments = new LinkedList<>();
            for (Attachment att : email.getAttachments()) {
                attachments.add(new Pair<>(att.getOriginalFilename(), Paths.get(att.getFilename())));
            }
            String zipFilename = "email-" + id + ".zip";
            InputStream is = ZipUtils.create(attachments);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment; filename=" + zipFilename);
            IOUtils.copy(is, response.getOutputStream());
            is.close();
            response.flushBuffer();
        } catch (IOException e) {
            LOGGER.error("Could not find attachment with id " + id + " on disk: " + e.getMessage());
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find attachment with id " + id + ": " + e.getMessage());
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
    }
}
