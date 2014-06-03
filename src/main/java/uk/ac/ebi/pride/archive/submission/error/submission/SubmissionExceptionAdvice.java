package uk.ac.ebi.pride.prider.submission.error.submission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.pride.web.util.exception.RestError;

/**
 * @author Rui Wang
 * @version $Id$
 */
@ControllerAdvice
public class SubmissionExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionExceptionAdvice.class);

    @Value("#{pxProperties['px.web.url']}")
    private String pxUrl;

    @ExceptionHandler(SubmissionException.class)
    @ResponseBody
    public RestError handleSubmissionException(SubmissionException ex) {

        logger.error(ex.getMessage(), ex);

        return new RestError.Builder()
                .setStatus(HttpStatus.BAD_REQUEST)
                .setCode(11000)
                .setMessage("Submission exception")
                .setDeveloperMessage(ex.getMessage())
                .setMoreInfoUrl(pxUrl)
                .setThrowable(ex)
                .build();
    }
}
