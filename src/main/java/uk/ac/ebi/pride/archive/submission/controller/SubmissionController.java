package uk.ac.ebi.pride.archive.submission.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.submission.model.submission.DropBoxDetail;
import uk.ac.ebi.pride.archive.submission.model.submission.SubmissionReferenceDetail;
import uk.ac.ebi.pride.archive.submission.model.submission.UploadDetail;
import uk.ac.ebi.pride.archive.submission.model.submission.UploadMethod;
import uk.ac.ebi.pride.archive.submission.model.user.ContactDetail;
import uk.ac.ebi.pride.prider.service.person.UserService;
import uk.ac.ebi.pride.prider.service.person.UserSummary;
import uk.ac.ebi.pride.archive.submission.error.submission.SubmissionException;
import uk.ac.ebi.pride.archive.submission.util.DropBoxManager;
import uk.ac.ebi.pride.archive.submission.util.PrideEmailNotifier;
import uk.ac.ebi.pride.archive.submission.util.SubmissionUtilities;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.security.Principal;

/**
 * Spring MVC controller for handling incoming submission requests
 *
 * @author Rui Wang
 * @version $Id$
 */
@Controller
@RequestMapping("/submission")
public class SubmissionController {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);

    @Autowired
    private DropBoxManager dropBoxManager;

    @Autowired
    private PrideEmailNotifier prideEmailNotifier;

    @Autowired
    private UserService userService;

    @Value("#{pxProperties['px.submission.queue.dir']}")
    private String submissionQueue;

    @Value("#{pxProperties['px.ftp.server.address']}")
    private String ftpHost;

    @Value("#{pxProperties['px.ftp.server.port']}")
    private int ftpPort;

    @Value("#{pxProperties['px.aspera.server.address']}")
    private String asperaHost;


    /**
     * Request for ftp upload details
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ContactDetail getUserDetail(Principal user) {

        UserSummary userSummary = userService.findByEmail(user.getName());

        return new ContactDetail(userSummary.getEmail(),
                                 userSummary.getTitle(),
                                 userSummary.getFirstName(),
                                 userSummary.getLastName(),
                                 userSummary.getAffiliation());
    }

    /**
     * Request for ftp upload details
     */
    @RequestMapping(value = "/upload/{method}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UploadDetail getUploadDetail(@PathVariable("method") String method, Principal user) {
        final UploadMethod uploadMethod = UploadMethod.findMethod(method);

        // select drop box
        DropBoxDetail selectedDropBox = dropBoxManager.selectDropBox();
        logger.debug("Drop box selected: {} for {} method", selectedDropBox.getDropBoxDirectory(), method);

        // create submission folder
        File submissionDirectory = SubmissionUtilities.createUploadFolder(new File(selectedDropBox.getDropBoxDirectory()), user.getName());
        logger.debug("Upload folder: " + submissionDirectory.getAbsolutePath());

        // generate response
        switch (uploadMethod) {
            case FTP:
                return new UploadDetail(UploadMethod.FTP, ftpHost, ftpPort, submissionDirectory.getAbsolutePath(), selectedDropBox);
            case ASPERA:
                return new UploadDetail(UploadMethod.ASPERA, asperaHost, -1, submissionDirectory.getAbsolutePath(), selectedDropBox);
            default:
                throw new SubmissionException("Unrecogonised submission method: " + method);
        }
    }

    /**
     * Confirm a ProteomeXchange submission is finished
     * This method will:
     * 1. Send a confirmation email to prides-support
     * 2. Place a ticket on the submission queue
     * 3. Send the submission temporary reference back to the user
     */
    @RequestMapping(value = "/submit", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SubmissionReferenceDetail completeSubmission(@RequestBody UploadDetail uploadDetail, Principal user) {
        logger.info("New -submit- request for user:" + user.getName() + " folder:" + uploadDetail.getFolder());

        // generate submission reference id
        String submissionRef = SubmissionUtilities.generateSubmissionReference();

        try {

            // create submission ticket in the submission queue
            File folderToSubmit = new File(uploadDetail.getFolder());

            SubmissionUtilities.generateSubmissionTicket(new File(submissionQueue), folderToSubmit, submissionRef);

            // notify pride using email
            prideEmailNotifier.notifyPride(user.getName(), folderToSubmit, submissionRef);

        } catch (MessagingException e) {
            String msg = "Failed to send confirmation email to PRIDE";
            logger.error(msg, e);
            throw new SubmissionException(msg, e);
        } catch (IOException e) {
            String msg = "Failed to generate submission ticket";
            logger.error(msg, e);
            throw new SubmissionException(msg, e);
        }

        return new SubmissionReferenceDetail(submissionRef);
    }
}
