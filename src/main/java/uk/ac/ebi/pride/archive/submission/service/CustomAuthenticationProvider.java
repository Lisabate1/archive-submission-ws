package uk.ac.ebi.pride.archive.submission.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import uk.ac.ebi.pride.archive.dataprovider.utils.RoleConstants;
import uk.ac.ebi.pride.archive.repo.models.user.Credentials;
import uk.ac.ebi.pride.archive.repo.models.user.UserSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private UserService userService;

    public CustomAuthenticationProvider(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        try {
            Credentials credentials = new Credentials();
            credentials.setUsername(name);
            credentials.setPassword(password);
            UserSummary userSummary = userService.getUserDetails(credentials);
            return new UsernamePasswordAuthenticationToken(
                    userSummary.getEmail(), userSummary.getPassword(), getAuthorityList(userSummary.getUserAuthorities()));
        } catch (Exception ex) {
            throw new AccessDeniedException("Exception in authenticating user", ex);
        }
    }

    private List<GrantedAuthority> getAuthorityList(Set<RoleConstants> roleConstants) {
        List<GrantedAuthority> authorityList = new ArrayList<>();
        if (roleConstants != null)
            roleConstants.stream().forEach(ua -> authorityList.add(new SimpleGrantedAuthority(ua.toString())));
        return authorityList;
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
