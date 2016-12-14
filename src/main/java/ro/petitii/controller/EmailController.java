package ro.petitii.controller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import ro.petitii.model.EmailAttachment;
import ro.petitii.service.EmailAttachmentService;
import ro.petitii.service.EmailService;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class EmailController extends ControllerBase {

    @Autowired
    ImapConfig config;

    @Autowired
    EmailAttachmentConfig attConfig;

    @Autowired
    EmailService emailService;

    @Autowired
    EmailAttachmentService emailAttachmentService;

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
            throw new IllegalArgumentException("Invalid email id");
        }

        ModelAndView modelAndView = new ModelAndView("email_detail");
        modelAndView.addObject("page", "Detalii");
        modelAndView.addObject("title", "Email-uri primite");
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("data", email);
        //todo; add a proper status, eventually as an enum
        String status = "";
        if (email.getPetition() != null) {
            status = "&#xce;n curs";
        } else if (email.getType() == Email.EmailType.Inbox) {
            status = "Nou";
        } else if (email.getType() == Email.EmailType.Outbox) {
            status = "Rezolvat";
        } else if (email.getType() == Email.EmailType.Spam) {
            status = "Spam";
        }
        modelAndView.addObject("status", status);
        return modelAndView;
    }

    @RequestMapping("/download/{id}")
    public void download(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            EmailAttachment att = emailAttachmentService.findById(id);
            Path filepath = Paths.get(att.getFilename());
            FileInputStream is = new FileInputStream(new File(filepath.toUri()));
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment; filename="+ att.getOriginalFilename());
            IOUtils.copy(is,response.getOutputStream());
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
