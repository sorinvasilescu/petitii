package ro.petitii.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
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
import ro.petitii.util.Pair;
import ro.petitii.util.TranslationUtil;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.util.*;

@Controller
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController extends ControllerBase {

	private static final String WELCOME_MESSAGE = TranslationUtil.i18n("controller.user.welcome");
	private static final Pair<String, String> WELCOME = new Pair<>("welcome_user", WELCOME_MESSAGE);
	private static final String PASSWORD_RESETED = TranslationUtil.i18n("controller.user.password_reseted");
	private static final Pair<String, String> RESET = new Pair<>("reset_password", PASSWORD_RESETED);

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

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
        String title = TranslationUtil.i18n("controller.user.title_users");
        modelAndView.addObject("title", title);
        modelAndView.addObject("apiUrl", "/api/users");
        return modelAndView;
    }

    @RequestMapping(path = "/user/{id}/edit", method = RequestMethod.GET)
    public ModelAndView editUser(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView("users_crud");
        //TODO: catch exceptions, add  error/success message
        User user = userService.findById(id);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @RequestMapping(path = "/user/{id}/suspend", method = RequestMethod.GET)
    public ModelAndView removeUser(@PathVariable("id") Long id, final RedirectAttributes attr) {
        User user = userService.findById(id);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(User.UserRole.SUSPENDED);
        //TODO: catch exceptions, add  error/success message
        userService.save(user);
        String message = TranslationUtil.i18n("controller.user.account_disabled");
        attr.addFlashAttribute("toast", createToast(message, ToastType.success));
        return new ModelAndView("redirect:/users");
    }

    @RequestMapping(path = "/user/{id}/reset", method = RequestMethod.GET)
    public ModelAndView resetPasswordUser(@PathVariable("id") Long id, final RedirectAttributes attr) {
    	//TODO: catch exceptions, add  error/success message
        User user = userService.findById(id);
        String newPassword = UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(newPassword));
        //TODO: catch exceptions, add  error/success message
        userService.save(user);

        //TODO: catch exceptions, add  error/success message
        List<Map<String, String>> toasts = sendUserPasswordReset(RESET, newPassword, user);
        attr.addFlashAttribute("toasts", toasts);
        return new ModelAndView("redirect:/users");
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
            if (!StringUtils.isEmpty(user.getPassword())
                    && !StringUtils.isEmpty(user.getPasswordCopy()) &&
                    user.getPassword().equals(user.getPasswordCopy())) {
                newPassword = user.getPassword();
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                throw new RuntimeException("Password not provided");
            }
        } else {
            if (user.getId() == null) {
                //set some random temporary password
                newPassword = UUID.randomUUID().toString();
                user.setPassword(passwordEncoder.encode(newPassword));
            } else {
                //set the previous password
            	//TODO: catch exceptions, add  error/success message
                user.setPassword(userService.findById(user.getId()).getPassword());
            }
        }
        return newPassword;
    }

    @RequestMapping(path = "/user", method = RequestMethod.POST)
    public ModelAndView saveUser(@Valid User user, final RedirectAttributes attr) {
        boolean newUser = user.getId() == null;
        String newPassword = setNewPassword(user);

        //TODO: catch exceptions, add  error/success message
        List<User> existingUser = userService.findUserByEmail(user.getEmail());
        if (newUser && existingUser != null && !existingUser.isEmpty()) {
        	String message = TranslationUtil.i18n("controller.user.email_address_exists");
            attr.addFlashAttribute("toast", createToast(message, ToastType.danger));
        } else {
        	//TODO: catch exceptions, add  error/success message
            userService.save(user);
            if (newUser) {
                List<Map<String, String>> toasts = sendUserPasswordReset(WELCOME, newPassword, user);
                attr.addFlashAttribute("toasts", toasts);
            }
        }

        return new ModelAndView("redirect:/users");
    }

    private List<Map<String, String>> sendUserPasswordReset(Pair<String, String> template, String password, User user) {
        List<Map<String, String>> toasts = new LinkedList<>();

        Map<String, Object> vars = new HashMap<>();
        vars.put("pass", password);
        vars.put("user", user);
        String emailBody = emailTemplateProcessorService.processStaticTemplate(template.getFirst(), vars);
        if (emailBody == null) {
            LOGGER.error("Could not compile the reset password for user = " + user.getEmail());
            String message = TranslationUtil.i18n("controller.user.reset_password_email_failed");
            toasts.add(createToast(message, ToastType.danger));
        } else {
            Email email = new Email();
            email.setSender(config.getUsername());
            email.setSubject(template.getSecond());
            email.setRecipients(user.getEmail());
            email.setBody(emailBody);
            try {
                LOGGER.info("Sending reset password email" + emailBody);
                smtpService.send(email);
                String message = TranslationUtil.i18n("controller.user.reset_password_email_sent");
                toasts.add(createToast(message, ToastType.success));
            } catch (MessagingException e) {
                LOGGER.error("Could not send email with password reset for user = " + user.getEmail(), e);
                String message = TranslationUtil.i18n("controller.user.reset_password_email_failed_with_error", new String[]{e.getMessage()});
                toasts.add(createToast(message , ToastType.danger));
            }
        }
        return toasts;
    }
}
