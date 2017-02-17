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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ro.petitii.validation.ValidationUtil.*;

@Controller
public class UserManagementController extends ViewController {
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
        String currentHash = ((UserDetail) auth.getPrincipal()).getPassword();

        check(assertTrue(passwordEncoder.matches(currentPassword, currentHash), i18n("controller.user.invalid_password")), logger, view);
        check(assertEquals(newPassword, duplicate, i18n("controller.user.invalid_repeat_password")), logger, view);
        check(assertNotEquals(currentPassword, newPassword, i18n("controller.user.invalid_new_password")), logger, view);

        User user = userService.findById(((UserDetail) auth.getPrincipal()).getUserId());
        user.setPassword(passwordEncoder.encode(newPassword));

        check(failOnException(() -> userService.save(user), i18n("controller.user.password_reset_failure")), logger, view);

        return success(i18n("controller.user.password_saved"), view);
    }

    @RequestMapping(value = "/reset_password", method = RequestMethod.GET)
    public String resetPassword() {
        return "reset_password";
    }

    @RequestMapping(value = "/reset_password", method = RequestMethod.POST)
    public ModelAndView resetPassword(@RequestParam("username") String username) {
        ModelAndView view = new ModelAndView("reset_password");
        List<User> users = userService.findUserByEmail(username);

        check(assertFalse(users.isEmpty(), i18n("controller.user.invalid_user")), logger, view);

        for (User user : users) {
            String newPassword = UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.save(user);
            sendUserPasswordReset(newPassword, user, view);
        }

        return success(i18n("controller.user.reset_password_email_sent"), view);
    }

    private void sendUserPasswordReset(String password, User user, ModelAndView view) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("pass", password);
        vars.put("user", user);
        String emailBody = emailTemplateProcessorService.processStaticTemplate("recover_password", vars);
        assertNotNull(emailBody, i18n("controller.user.reset_password_email_failed"))
                .logMessages(logger, "Could not compile the reset password for user = " + user.getEmail()).failIfInvalid(view);

        Email email = new Email();
        email.setSender(config.getUsername());
        email.setSubject(i18n("controller.user.password_reset_success"));
        email.setRecipients(user.getEmail());
        email.setBody(emailBody);
        try {
            smtpService.send(email);
        } catch (MessagingException e) {
            fail(i18n("controller.user.reset_password_email_failed_smtp_error"))
                    .logMessages(logger, "Could not send email with password reset for user = " + user.getEmail() + " Reason:" + e.getMessage(), e)
                    .failIfInvalid(view);
        }
    }
}