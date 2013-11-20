package uk.ac.ebi.pride.prider.submission.util;

import uk.ac.ebi.pride.prider.webservice.submission.model.DropBoxDetail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * DropBoxFactory is responsible for assigning the right FTP drop box for uploading
 *
 * @author Jose A. Dianes
 * @author Rui Wang
 * @version $Id$
 */
public class DropBoxManager {

    private final List<DropBoxDetail> dropBoxes = new ArrayList<DropBoxDetail>();
    private int dropBoxSelectionIndex = -1;

    public DropBoxManager(Collection<DropBoxDetail> dropBoxDetails) {
        this.dropBoxes.addAll(dropBoxDetails);
    }

    /**
     * Select ftp drop box
     * Note: round-robin selection of the drop box
     *
     * @return selected drop box directory
     */
    public DropBoxDetail selectFtpDropBox() {
        synchronized (this) {
            // deciding on the index of the drop box to be selected
            dropBoxSelectionIndex++;

            if (dropBoxSelectionIndex >= dropBoxes.size()) {
                dropBoxSelectionIndex = 0;
            }
        }

        return dropBoxes.get(dropBoxSelectionIndex);
    }

    /**
     * Get a list of drop boxes
     *
     * @return a full of list of drop boxes
     */
    public List<DropBoxDetail> getDropBoxes() {
        return Collections.unmodifiableList(dropBoxes);
    }
}
