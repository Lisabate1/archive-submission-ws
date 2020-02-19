package uk.ac.ebi.pride.archive.submission.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import uk.ac.ebi.pride.archive.repo.assay.AssayRepository;
import uk.ac.ebi.pride.archive.repo.project.ProjectRepository;
import uk.ac.ebi.pride.archive.repo.user.UserRepository;
import uk.ac.ebi.pride.archive.security.framework.UserDetailsSecurityServiceImpl;
import uk.ac.ebi.pride.archive.security.framework.UserServicePermissionEvaluator;
import uk.ac.ebi.pride.archive.security.framework.permission.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebMvc
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.httpBasic().and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).and()
                .authorizeRequests()
                .antMatchers("/resubmission/**").hasRole("SUBMITTER")
                .antMatchers("/submission/**").hasRole("SUBMITTER")
                .antMatchers("/**").denyAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }

    @Bean
    protected UserDetailsService userDetailsService(UserRepository userRepository) {
        return new UserDetailsSecurityServiceImpl(userRepository);
    }

    @Bean
    public AssayIdPermission assayIdPermission(AssayRepository assayRepository, ProjectRepository projectRepository) {
        return new AssayIdPermission(assayRepository, projectRepository);
    }

    @Bean
    public ProjectIdPermission projectIdPermission(ProjectRepository projectRepository) {
        return new ProjectIdPermission(projectRepository);
    }

    @Bean
    public FilePermission filePermission(ProjectRepository projectRepository) {
        return new FilePermission(projectRepository);
    }

    @Bean
    public AssayAccessionPermission assayAccessionPermission(AssayRepository assayRepository, ProjectRepository projectRepository) {
        return new AssayAccessionPermission(assayRepository, projectRepository);
    }

    @Bean
    public AssayPermission assayPermission(ProjectRepository projectRepository) {
        return new AssayPermission(projectRepository);
    }

    @Bean
    public ProjectAccessionPermission projectAccessionPermission(ProjectRepository projectRepository) {
        return new ProjectAccessionPermission(projectRepository);
    }

    @Bean
    public ProjectPermission projectPermission(ProjectRepository projectRepository) {
        return new ProjectPermission();
    }

    @Bean
    public PermissionEvaluator permissionEvaluator(AssayIdPermission assayIdPermission,
                                                   ProjectIdPermission projectIdPermission,
                                                   FilePermission filePermission,
                                                   AssayAccessionPermission assayAccessionPermission,
                                                   AssayPermission assayPermission,
                                                   ProjectAccessionPermission projectAccessionPermission,
                                                   ProjectPermission projectPermission) {
        Map<String, Permission> permissionMap = new HashMap<>();
        permissionMap.put("isAccessibleProject", projectPermission);
        permissionMap.put("isAccessibleProjectAccession", projectAccessionPermission);
        permissionMap.put("isAccessibleAssay", assayPermission);
        permissionMap.put("isAccessibleAssayAccession", assayAccessionPermission);
        permissionMap.put("isAccessibleFile", filePermission);
        permissionMap.put("isAccessibleProjectId", projectIdPermission);
        permissionMap.put("isAccessibleAssayId", assayIdPermission);
        return new UserServicePermissionEvaluator(permissionMap);
    }

    @Bean
    public MethodSecurityExpressionHandler expressionHandler(PermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler = new DefaultMethodSecurityExpressionHandler();
        methodSecurityExpressionHandler.setPermissionEvaluator(permissionEvaluator);
        return methodSecurityExpressionHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
