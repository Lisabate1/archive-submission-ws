package uk.ac.ebi.pride.archive.submission.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Jose A. Dianes
 * @author Rui Wang
 * @version $Id$
 */

@Component
public class PrideEmailNotifier {

    private static final Logger logger = LoggerFactory.getLogger(PrideEmailNotifier.class);

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private MailSender mailSender;

    private SimpleMailMessage templateMessage;

    @Value("${px.submission.mount.path}")
    private String submissionMountPath;

    @Autowired
    public PrideEmailNotifier(MailSender mailSender, SimpleMailMessage templateMessage) {
        this.mailSender = mailSender;
        this.templateMessage = templateMessage;
    }

    /**
     * Notify pride using email
     *                                                 `
     * @param userEmail        submitter's email
     * @param submissionFolder submission folder
     * @param submissionRef    submission reference
     * @throws javax.mail.MessagingException
     */
    public void notifyPride(String userEmail,
                            String submissionFolder,
                            String submissionRef, String uploadMethod) throws MessagingException {
        String message = "Submission Reference: " + submissionRef + LINE_SEPARATOR +
                "Submission Path: " + (submissionFolder != null ? submissionFolder : "") + LINE_SEPARATOR +
            "Upload type: " + uploadMethod + LINE_SEPARATOR;
        logger.info("Sending email to pride-support:\n" + message);
        sendEmail(userEmail, message);
    }

    /**
     * This method calculates long bytes into a human-readable format.
     * @param bytes file size to calculate
     * @param si si units, otherwise binary units
     * @return
     */
    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Send an email
     *
     * @param from    email from
     * @param body    email body
     * @throws javax.mail.MessagingException error while sending email
     */
    private void sendEmail(String from, String body) throws MessagingException {
        SimpleMailMessage msg = new SimpleMailMessage(this.templateMessage);
        msg.setFrom(from);
        msg.setText(body);
        mailSender.send(msg);
    }

    /**
     * Get the file size within a folder in mega bytes, does not include sub-folders
     *
     * @param files array of Files
     * @return file size in mega bytes
     */
    private long getFolderSize(File[] files) {
        long size = 0;
        for (File file : files) {
            try {
                size += Files.size(file.toPath());
            } catch (IOException ioe) {
                logger.error("Error reading file to calculate file size for: " + file.getAbsolutePath());
            }
        }
        return size;
    }
}
