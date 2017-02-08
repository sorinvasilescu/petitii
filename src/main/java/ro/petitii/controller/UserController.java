package ro.petitii.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.petitii.config.SmtpConfig;
import ro.petitii.model.Email;
import ro.petitii.model.User;
import ro.petitii.service.UserService;
import ro.petitii.service.email.SmtpService;
import ro.petitii.service.template.EmailTemplateProcessorService;
import ro.petitii.util.ToastMaster;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.util.*;

import static org.springframework.util.StringUtils.isEmpty;
import static ro.petitii.validation.ValidationUtil.*;

@Controller
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController extends ViewController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SmtpConfig config;

    @Autowired
    private EmailTemplateProcessorService emailTemplateProcessorService;

    @Autowired
    private SmtpService smtpService;

    @RequestMapping("/users")
    public ModelAndView users() {
        ModelAndView modelAndView = new ModelAndView("users_list");
        modelAndView.addObject("page", "inbox");
        modelAndView.addObject("title", i18n("controller.user.title_users"));
        modelAndView.addObject("apiUrl", "/api/users");
        return modelAndView;
    }

    private ModelAndView editUser(User user) {
        ModelAndView modelAndView = new ModelAndView("users_crud");
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @RequestMapping(path = "/user/{id}/edit", method = RequestMethod.GET)
    public ModelAndView editUser(@PathVariable("id") Long id) {
        User user = userService.findById(id);
        check(assertNotNull(user, i18n("controller.user.invalid_user")), logger, redirect("users"));
        return editUser(user);
    }

    @RequestMapping(path = "/user/{id}/suspend", method = RequestMethod.GET)
    public ModelAndView removeUser(@PathVariable("id") Long id, final RedirectAttributes attr) {
        User user = userService.findById(id);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(User.UserRole.SUSPENDED);

        failOnException(() -> userService.save(user), i18n("controller.user.account_disabled_failed"))
                .logMessages(logger, "Failed to save suspended user = " + id).failIfInvalid(redirect("users"));

        return successAndRedirect(i18n("controller.user.account_disabled_success"), "users", attr);
    }

    @RequestMapping(path = "/user/{id}/reset", method = RequestMethod.GET)
    public ModelAndView resetPasswordUser(@PathVariable("id") Long id, final RedirectAttributes attr) {
        User user = userService.findById(id);
        check(assertNotNull(user, i18n("controller.user.invalid_user")), logger, redirect("users"));
        String newPassword = UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(newPassword));
        check(failOnException(() -> userService.save(user), i18n("controller.user.account_password_reset.failure")), logger, editUser(user));

        String subject = i18n("controller.user.account_password_reset.success");
        List<ToastMaster.Toast> toasts = sendUserPasswordReset("reset_password", subject, newPassword, user);
        check(toasts, logger, editUser(user));
        toasts.add(i18nToast("controller.user.account_password_reset.success", ToastMaster.ToastType.success));
        return successAndRedirect(toasts, "users", attr);
    }


    @RequestMapping(path = "/user", method = RequestMethod.GET)
    public ModelAndView addUser() {
        User user = new User();
        user.setRole(User.UserRole.USER);

        ModelAndView modelAndView = new ModelAndView("users_crud");
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    private String setNewPassword(User user) {
        String newPassword = null;
        if (user.getChangePassword()) {
            boolean condition = !isEmpty(user.getPassword()) && !isEmpty(user.getPasswordCopy()) && Objects.equals(user.getPassword(), user.getPasswordCopy());
            check(assertTrue(condition, i18n("controller.user.invalid_password")), logger, editUser(user));
            newPassword = user.getPassword();
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            if (user.getId() == null) {
                //set some random temporary password
                newPassword = UUID.randomUUID().toString();
                user.setPassword(passwordEncoder.encode(newPassword));
            } else {
                //set the previous password
                user.setPassword(userService.findById(user.getId()).getPassword());
            }
        }
        return newPassword;
    }

    @RequestMapping(path = "/user", method = RequestMethod.POST)
    public ModelAndView saveUser(@Valid User user, final RedirectAttributes attr) {
        boolean newUser = user.getId() == null;
        String newPassword = setNewPassword(user);

        List<User> existingUser = userService.findUserByEmail(user.getEmail());
        check(assertFalse(newUser && existingUser != null && !existingUser.isEmpty(), i18n("controller.user.email_address_exists")), logger, editUser(user));

        check(failOnException(() -> userService.save(user), i18n("controller.user.save.failed")), logger, editUser(user));

        if (newUser) {
            String subject = i18n("controller.user.welcome");
            List<ToastMaster.Toast> toasts = sendUserPasswordReset("welcome_user", subject, newPassword, user);
            attr.addFlashAttribute("toasts", toasts);
            check(toasts, logger, editUser(user));
            toasts.add(i18nToast("controller.user.save.success", ToastMaster.ToastType.success));
            return successAndRedirect(toasts, "users", attr);
        } else {
            return successAndRedirect(i18n("controller.user.save.success"), "users", attr);
        }
    }

    private List<ToastMaster.Toast> sendUserPasswordReset(String template, String subject, String password, User user) {
        List<ToastMaster.Toast> toasts = new LinkedList<>();
        Map<String, Object> vars = new HashMap<>();
        vars.put("pass", password);
        vars.put("user", user);
        String emailBody = emailTemplateProcessorService.processStaticTemplate(template, vars);
        if (emailBody == null) {
            logger.error("Could not compile the reset password for user = " + user.getEmail());
            toasts.add(i18nToast("controller.user.reset_password_email_failed", ToastMaster.ToastType.danger));
        } else {
            Email email = new Email();
            email.setSender(config.getUsername());
            email.setSubject(subject);
            email.setRecipients(user.getEmail());
            email.setBody(emailBody);
            try {
                logger.info("Sending reset password email" + emailBody);
                smtpService.send(email);
                toasts.add(i18nToast("controller.user.reset_password_email_sent", ToastMaster.ToastType.success));
            } catch (MessagingException e) {
                logger.error("Could not send email with password reset for user = " + user.getEmail(), e);
                toasts.add(i18nToast("controller.user.reset_password_email_failed_with_error", ToastMaster.ToastType.danger, e.getMessage()));
            }
        }
        return toasts;
    }
}
