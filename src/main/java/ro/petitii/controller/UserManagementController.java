package ro.petitii.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ro.petitii.config.SmtpConfig;
import ro.petitii.model.Email;
import ro.petitii.model.User;
import ro.petitii.model.UserDetail;
import ro.petitii.service.UserService;
import ro.petitii.service.email.SmtpService;
import ro.petitii.service.template.EmailTemplateProcessorService;

import javax.mail.MessagingException;
import java.util.*;

@Controller
public class UserManagementController extends ControllerBase {
    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private SmtpConfig config;

    @Autowired
    private EmailTemplateProcessorService emailTemplateProcessorService;

    @Autowired
    private SmtpService smtpService;

    @RequestMapping(value = "/change_password", method = RequestMethod.GET)
    public String changePassword() {
        return "change_password";
    }

    @RequestMapping(value = "/change_password", method = RequestMethod.POST)
    public ModelAndView changePassword(@RequestParam("currentPassword") String currentPassword,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("duplicate") String duplicate) {
        ModelAndView view = new ModelAndView("change_password");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (passwordEncoder.matches(currentPassword, ((UserDetail) auth.getPrincipal()).getPassword())) {
            if (Objects.equals(newPassword, duplicate)) {
                // just to be safe if Parsley fails to check this
                if (Objects.equals(currentPassword, newPassword)) {
                    view.addObject("toast", createToast("Parola nouă e identică cu cea curentă", ControllerBase.ToastType.danger));
                } else {
                    User user = userService.findById(((UserDetail) auth.getPrincipal()).getUserId());
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userService.save(user);
                    view.addObject("toast", createToast("Parola salvata cu success", ToastType.success));
                }
            } else {
                view.addObject("toast", createToast("Parola nouă nu e identică cu cea repetată", ControllerBase.ToastType.danger));
            }
        } else {
            view.addObject("toast", createToast("Parola curentă invalidă", ControllerBase.ToastType.danger));
        }

        return view;
    }

    @RequestMapping(value = "/reset_password", method = RequestMethod.GET)
    public String resetPassword() {
        return "reset_password";
    }

    @RequestMapping(value = "/reset_password", method = RequestMethod.POST)
    public ModelAndView resetPassword(@RequestParam("username") String username) {
        ModelAndView view = new ModelAndView("reset_password");
        List<User> users = userService.findUserByEmail(username);
        if (users.isEmpty()) {
            view.addObject("toast", createToast("Utilizator invalid", ControllerBase.ToastType.danger));
        } else {
            for (User user : users) {
                String newPassword = UUID.randomUUID().toString();
                user.setPassword(passwordEncoder.encode(newPassword));
                userService.save(user);
                view.addObject("toasts", sendUserPasswordReset(newPassword, user));
            }
        }
        return view;
    }

    private List<Map<String, String>> sendUserPasswordReset(String password, User user) {
        List<Map<String, String>> toasts = new LinkedList<>();

        Map<String, Object> vars = new HashMap<>();
        vars.put("pass", password);
        vars.put("user", user);
        String emailBody = emailTemplateProcessorService.processStaticTemplate("recover_password", vars);
        if (emailBody == null) {
            logger.error("Could not compile the reset password for user = " + user.getEmail());
            toasts.add(createToast("Emailul de resetare a parolei nu a fost trimis. Motiv: template compile failure", ToastType.danger));
        } else {
            Email email = new Email();
            email.setSender(config.getUsername());
            email.setSubject("Parola dumneavoastră a fost resetată cu success");
            email.setRecipients(user.getEmail());
            email.setBody(emailBody);
            try {
                smtpService.send(email);
                toasts.add(createToast("Emailul de resetare a parolei a fost trimis", ToastType.success));
            } catch (MessagingException e) {
                logger.error("Could not send email with password reset for user = " + user.getEmail() + " Reason:" + e.getMessage());
                logger.debug("Could not send email with password reset for user = " + user.getEmail() + " Reason:" + e.getMessage(), e);
                toasts.add(createToast("Emailul de resetare a parolei nu a fost trimis. Motiv: SMTP Error", ToastType.danger));
            }
        }
        return toasts;
    }
}