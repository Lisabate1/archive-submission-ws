package uk.ac.ebi.pride.archive.submission.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import uk.ac.ebi.pride.archive.submission.model.submission.DropBoxDetail;
import uk.ac.ebi.pride.archive.submission.util.DropBoxManager;
import uk.ac.ebi.pride.archive.submission.util.PrideEmailNotifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
public class BootStrapConfig {

    public static final String DIRECTORY = "directory";
    public static final String USER_NAME = "user";
    public static final String PASSWORD = "password";
    public static final String TO = "to";
    public static final String SUBJECT = "subject";
    public static final String HOST = "host";

    @Bean
    @ConfigurationProperties(prefix = "px.drop.box")
    public HashMap<String, HashMap<String, String>> dropBoxInitializations() {
        return new HashMap<>();
    }

    @Bean
    @ConfigurationProperties(prefix = "px.notification.email")
    public Properties mailInitializations() {
        return new Properties();
    }


    @Bean
    public PrideEmailNotifier prideEmailNotifier(MailSender mailSender, SimpleMailMessage templateMessage) {
        return new PrideEmailNotifier(mailSender, templateMessage);
    }

    @Bean
    public DropBoxManager dropBoxManager() {
        return new DropBoxManager(dropBoxDetails());
    }

    public List<DropBoxDetail> dropBoxDetails() {
        List<DropBoxDetail> dropBoxDetails = new ArrayList<>();
        Map<String, HashMap<String, String>> dropBoxInitializations = dropBoxInitializations();
        for (Map.Entry<String, HashMap<String, String>> e : dropBoxInitializations.entrySet()) {
            Map<String, String> dropBox = e.getValue();
            dropBoxDetails.add(new DropBoxDetail(dropBox.get(DIRECTORY), dropBox.get(USER_NAME), dropBox.get(PASSWORD)));
        }
        return dropBoxDetails;

    }

    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        Properties properties = mailInitializations();
        mailSender.setHost(properties.getProperty(HOST));
        return mailSender;
    }

    @Bean
    public SimpleMailMessage templateMessage() {
        Properties properties = mailInitializations();
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(properties.getProperty(TO));
        simpleMailMessage.setSubject(properties.getProperty(SUBJECT));
        return simpleMailMessage;
    }
}
