package uk.ac.ebi.pride.archive.submission.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.pride.archive.repo.client.TicketRepoClient;
import uk.ac.ebi.pride.archive.repo.models.ticket.Ticket;
import uk.ac.ebi.pride.archive.submission.error.submission.SubmissionException;
import uk.ac.ebi.pride.archive.submission.model.submission.DropBoxDetail;
import uk.ac.ebi.pride.archive.submission.model.submission.SubmissionReferenceDetail;
import uk.ac.ebi.pride.archive.submission.model.submission.UploadDetail;
import uk.ac.ebi.pride.archive.submission.model.submission.UploadMethod;
import uk.ac.ebi.pride.archive.submission.service.ValidationService;
import uk.ac.ebi.pride.archive.submission.util.DropBoxManager;
import uk.ac.ebi.pride.archive.submission.util.PrideEmailNotifier;
import uk.ac.ebi.pride.archive.submission.util.SubmissionUtilities;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;

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
  private TicketRepoClient ticketRepoClient;

  @Autowired
  private ValidationService validationService;

  @Value("${px.ftp.server.address}")
  private String ftpHost;

  @Value("${px.ftp.server.port}")
  private int ftpPort;

  @Value("${px.aspera.server.address}")
  private String asperaHost;

  @Value("${px.aspera.server.port}")
  private int asperaPort;

  /** Request for ftp upload details */
  @RequestMapping(
    value = "/upload/{method}",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UploadDetail getUploadDetail(@PathVariable("method") String method, Principal user) {
    final UploadMethod uploadMethod = UploadMethod.findMethod(method);
    UploadDetail result;
    DropBoxDetail selectedDropBox = dropBoxManager.selectDropBox();
    logger.debug(
        "Drop box selected: {} for {} method", selectedDropBox.getDropBoxDirectory(), method);
    File submissionDirectory =
        SubmissionUtilities.createUploadFolder(
            new File(selectedDropBox.getDropBoxDirectory()), user.getName());
    logger.debug("Upload folder: " + submissionDirectory.getAbsolutePath());
    switch (uploadMethod) {
      case FTP:
        result =
            new UploadDetail(
                UploadMethod.FTP,
                ftpHost,
                ftpPort,
                submissionDirectory.getAbsolutePath(),
                selectedDropBox);
        break;
      case ASPERA:
        result =
            new UploadDetail(
                UploadMethod.ASPERA,
                asperaHost,
                asperaPort,
                submissionDirectory.getName(),
                selectedDropBox);
        break;
      default:
        throw new SubmissionException("Unrecognised submission method: " + method);
    }
    return result;
  }

  /**
   * Confirm a ProteomeXchange submission is finished This method will: 1. Send a confirmation email
   * to prides-support 2. Place a ticket on the submission queue 3. Send the submission temporary
   * reference back to the user
   */
  @RequestMapping(
    value = "/submit",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public SubmissionReferenceDetail completeSubmission(
      @RequestBody UploadDetail uploadDetail, Principal user) {
    logger.info(
        "New -submit- request for user:" + user.getName() + " folder: " + uploadDetail.getFolder());
    String submissionRef = SubmissionUtilities.generateSubmissionReference();
    try {
      File folderToSubmit =
              uploadDetail.getMethod() == UploadMethod.ASPERA
                      ? new File(
                      uploadDetail.getDropBox().getDropBoxDirectory()
                              + System.getProperty("file.separator")
                              + uploadDetail.getFolder())
                      : new File(uploadDetail.getFolder());
      Ticket ticket = new Ticket();
      ticket.setSubmittedFilesPath(folderToSubmit.getPath());
      ticket.setState(Ticket.State.INCOMING);
      ticket.setCreatedDate(new Date());
      ticket.setLastModifiedDate(new Date());
      ticket.setSubmitterEmail(user.getName());
      ticket.setTicketId(submissionRef);
      ticketRepoClient.save(ticket);

      validationService.validateTicket(ticket.getTicketId());
      prideEmailNotifier.notifyPride(
              user.getName(), folderToSubmit, submissionRef, uploadDetail.getMethod().getMethod());

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
