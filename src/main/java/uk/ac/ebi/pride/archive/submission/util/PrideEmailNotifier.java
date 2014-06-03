package uk.ac.ebi.pride.prider.submission.util;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import javax.mail.MessagingException;
import java.io.File;
import java.text.DecimalFormat;

/**
 * @author Jose A. Dianes
 * @author Rui Wang
 * @version $Id$
 */
public class PrideEmailNotifier {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private MailSender mailSender;
    private SimpleMailMessage templateMessage;

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
                            File submissionFolder,
                            String submissionRef) throws MessagingException {


        // generate email body
        StringBuilder builder = new StringBuilder();
        builder.append("Submission Reference: ");
        builder.append(submissionRef);
        builder.append(LINE_SEPARATOR);

        // get files submitted
        File files[] = submissionFolder.listFiles();
        if (files != null) {
            builder.append("Number of files submitted: ");
            builder.append(files.length - 1);
            builder.append(LINE_SEPARATOR);
            builder.append("Submission size [M]: ");
            long folderSize = getFolderSize(submissionFolder);
            double fileSize = (folderSize * 1.0) / (1024 * 1024);
            DecimalFormat df = new DecimalFormat("#.###");
            builder.append(df.format(fileSize));
            builder.append(LINE_SEPARATOR);
        }

        sendEmail(userEmail, builder.toString());
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
     * Get the file size within a folder in mega bytes
     *
     * @param folder folder
     * @return file size in mega bytes
     */
    private long getFolderSize(File folder) {
        long size = 0;

        File files[] = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                size += getFolderSize(file);
            } else {
                size += file.length();
            }
        }

        return size;
    }
}
