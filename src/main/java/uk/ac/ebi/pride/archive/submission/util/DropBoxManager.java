package uk.ac.ebi.pride.archive.submission.util;


import uk.ac.ebi.pride.archive.submission.model.submission.DropBoxDetail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * DropBoxFactory is responsible for assigning the right drop box for uploading
 *
 * Currently, there can be either FTP dropboxes or Aspera dropboxes
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
     * Select drop box
     * Note: round-robin selection of the drop box
     *
     * @return selected drop box directory
     */
    public DropBoxDetail selectDropBox() {
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
