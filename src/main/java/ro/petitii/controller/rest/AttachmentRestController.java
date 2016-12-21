package ro.petitii.controller.rest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import ro.petitii.model.Attachment;
import ro.petitii.model.Email;
import ro.petitii.model.Petition;
import ro.petitii.service.AttachmentService;
import ro.petitii.service.EmailService;
import ro.petitii.service.PetitionService;
import ro.petitii.util.Pair;
import ro.petitii.util.ZipUtils;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

@Controller
public class AttachmentRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentRestController.class);

    @Autowired
    private PetitionService petitionService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AttachmentService attachmentService;

    @RequestMapping("/rest/attachments/download/{id}")
    public void download(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            Attachment att = attachmentService.findById(id);
            Path filepath = Paths.get(att.getFilename());
            FileInputStream is = new FileInputStream(new File(filepath.toUri()));
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(att.getOriginalFilename(), "UTF-8"));
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
