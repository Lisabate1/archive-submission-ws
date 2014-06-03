package uk.ac.ebi.pride.archive.submission.error.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.pride.web.util.exception.RestError;
import uk.ac.ebi.pride.web.util.exception.RestErrorRegistry;

import java.security.Principal;

/**
 * @author Jose A. Dianes
 * @author Rui Wang
 * @version $Id$
 */
@ControllerAdvice
public class AccessDeniedAdvice {
    private static final Logger logger = LoggerFactory.getLogger(AccessDeniedAdvice.class);

    @ExceptionHandler(AccessDeniedException.class)
    public @ResponseBody RestError handleAccessDeniedException(AccessDeniedException ex, Principal principal) {
        logger.error(ex.getMessage(), ex);
        RestError accessDeny = RestErrorRegistry.getRestErrorByClass(AccessDeniedException.class);

        if (principal != null) {
            accessDeny.setDeveloperMessage("Access denied for user " + principal.getName());
        }

        return accessDeny;
    }
}
