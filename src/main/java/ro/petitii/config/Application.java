package ro.petitii.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import ro.petitii.service.email.SmtpService;
import ro.petitii.tasks.CronScheduler;

import javax.mail.MessagingException;
import java.util.Locale;

@SpringBootApplication(scanBasePackages = "ro.petitii")
@EnableJpaRepositories(basePackages = {"ro.petitii"}, repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class)
@EntityScan(basePackages = {"ro.petitii.model"})
public class Application extends WebMvcConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
        try {
            ctx.getBean(SmtpService.class).connect();
        } catch (MessagingException e) {
            LOGGER.error("SMTP service failed to connect!");
        }
        ctx.getBean(CronScheduler.class).setupScheduledTasks();
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(new Locale("ro"));
        return slr;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/config/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public TaskScheduler executor() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(8);
        return executor;
    }
}
