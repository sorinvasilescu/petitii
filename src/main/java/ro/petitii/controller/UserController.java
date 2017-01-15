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
import ro.petitii.model.EmailTemplate;
import ro.petitii.model.User;
import ro.petitii.service.EmailTemplateService;
import ro.petitii.service.UserService;
import ro.petitii.service.email.SmtpService;
import ro.petitii.service.template.EmailTemplateProcessorService;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.util.*;

@Controller
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController extends ControllerBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SmtpConfig config;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private EmailTemplateProcessorService emailTemplateProcessorService;

    @Autowired
    private SmtpService smtpService;

    @RequestMapping("/users")
    public ModelAndView users() {
        ModelAndView modelAndView = new ModelAndView("users_list");
        modelAndView.addObject("page", "inbox");
        modelAndView.addObject("title", "Useri");
        modelAndView.addObject("apiUrl", "/api/users");
        return modelAndView;
    }

    @RequestMapping(path = "/user/{id}/edit", method = RequestMethod.GET)
    public ModelAndView editUser(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView("users_crud");

        User user = userService.findById(id);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @RequestMapping(path = "/user/{id}/suspend", method = RequestMethod.GET)
    public ModelAndView removeUser(@PathVariable("id") Long id, final RedirectAttributes attr) {
        User user = userService.findById(id);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(User.UserRole.SUSPENDED);
        userService.save(user);
        attr.addFlashAttribute("toast", createToast("Contul a fost suspendat", ToastType.success));
        return new ModelAndView("redirect:/users");
    }

    @RequestMapping(path = "/user/{id}/reset", method = RequestMethod.GET)
    public ModelAndView resetPasswordUser(@PathVariable("id") Long id, final RedirectAttributes attr) {
        User user = userService.findById(id);
        String newPassword = UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);

        List<Map<String, String>> toasts = sendUserPasswordReset(newPassword, user);
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
                user.setPassword(userService.findById(user.getId()).getPassword());
            }
        }
        return newPassword;
    }

    @RequestMapping(path = "/user", method = RequestMethod.POST)
    public ModelAndView saveUser(@Valid User user, final RedirectAttributes attr) {
        boolean newUser = user.getId() == null;
        String newPassword = setNewPassword(user);

        if (newUser && userService.findUserByEmail(user.getEmail()) != null) {
            attr.addFlashAttribute("toast", createToast("Adresa de e-mail existenta", ToastType.danger));
        } else {
            userService.save(user);
            if (newUser) {
                List<Map<String, String>> toasts = sendUserPasswordReset(newPassword, user);
                attr.addFlashAttribute("toasts", toasts);
            }
        }

        return new ModelAndView("redirect:/users");
    }

    private List<Map<String, String>> sendUserPasswordReset(String password, User user) {
        List<Map<String, String>> toasts = new LinkedList<>();

        String emailBody;
        EmailTemplate emailTemplate = emailTemplateService.findOneByCategory(EmailTemplate.Category.recover_password);
        if (emailTemplate == null) {
            LOGGER.error("Email template not found for start work, using standard text messages ...");
            toasts.add(createToast("Template-urile pentru e-mail nu sunt configurate corect ...", ToastType.warning));
            emailBody = createDefaultResetEmail(password);
        } else {
            Map<String, Object> vars = new HashMap<>();
            vars.put("pass", password);
            vars.put("user", user);
            emailBody = emailTemplateProcessorService.processTemplateWithId(emailTemplate.getId(), vars);
            if (emailBody == null) {
                emailBody = createDefaultResetEmail(password);
            }
        }

        Email email = new Email();
        email.setSender(config.getUsername());
        email.setSubject("Parola dumneavoastră a fost resetată cu success");
        email.setRecipients(user.getEmail());
        email.setBody(emailBody);
        try {
            LOGGER.info("Sending reset password email" + emailBody);
            smtpService.send(email);
            toasts.add(createToast("Emailul de resetare a parolei a fost trimis", ToastType.success));
        } catch (MessagingException e) {
            LOGGER.error("Could not send email with password reset for user = " + user.getEmail() + " Reason:" + e.getMessage());
            toasts.add(createToast("Emailul de resetare a parolei nu a fost trimis. Motiv: " + e.getMessage(), ToastType.danger));
        }
        return toasts;
    }

    private String createDefaultResetEmail(String password) {
        return "Un administrator a cerut resetarea parolei dumneavoastră. Noua parolă este: " + password + "\n " +
                "Este recomandată schimbarea parolei imediat după primirea acestui e-mail.";
    }
}
