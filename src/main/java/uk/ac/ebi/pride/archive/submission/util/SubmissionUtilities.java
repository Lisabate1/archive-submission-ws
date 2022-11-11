package uk.ac.ebi.pride.archive.submission.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.UUID;

/**
 * @author Jose A. Dianes
 * @author Rui Wang
 * @version $Id$
 */
public class SubmissionUtilities {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionUtilities.class);
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    /**
     * Generate a unique reference for the PX submission
     *
     * @return submission reference
     */
    public static String generateSubmissionReference() {
        String dateStamp = getCurrentDateStamp();
        SecureRandom random = new SecureRandom();

        return "1-" + dateStamp + "-" + new BigInteger(16, random).toString(8).toUpperCase();
    }

    public static String getUploadFolderTobeCreatedBySubmissionTool(String userName) {
        String[] parts = userName.split("@");
        UUID uuid = UUID.randomUUID();
        return parts[0] + "_" + getCurrentTimestamp() + "_" + uuid;
    }

    private static String getCurrentTimestamp() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        StringBuilder timestamp = new StringBuilder();
        timestamp.append(year);
        timestamp.append(month < 10 ? "0" : "");
        timestamp.append(month);
        timestamp.append(day < 10 ? "0" : "");
        timestamp.append(day);
        timestamp.append("_");
        timestamp.append(hour < 10 ? "0" : "");
        timestamp.append(hour);
        timestamp.append(minute < 10 ? "0" : "");
        timestamp.append(minute);
        timestamp.append(second < 10 ? "0" : "");
        timestamp.append(second);

        return timestamp.toString();
    }


    private static String getCurrentDateStamp() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return year + ((month < 10) ? "0" : "") + month + ((day < 10) ? "0" : "") + day;
    }
}
