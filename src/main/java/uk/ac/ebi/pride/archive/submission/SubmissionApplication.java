package uk.ac.ebi.pride.archive.submission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.ServletContext;

@SpringBootApplication
@EnableWebMvc
public class SubmissionApplication implements ServletContextAware {

    private ServletContext context;

    public static void main(String[] args) {
        SpringApplication.run(SubmissionApplication.class, args);
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.context = servletContext;
    }
}
