package uk.ac.ebi.pride.archive.submission.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.pride.archive.submission.model.project.ProjectDetailList;
import uk.ac.ebi.pride.archive.submission.service.UserService;

import java.security.Principal;

/**
 * @author Rui Wang
 * @version $Id$
 */
@Controller
@RequestMapping("/resubmission")
public class ResubmissionController {

    private static final Logger logger = LoggerFactory.getLogger(ResubmissionController.class);

    @Autowired
    private UserService userService;

    /**
     * Request for private project accessions of a user
     */
    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ProjectDetailList getUserDetail(Principal user) {
        logger.info("New -resubmission- request for user:" + user.getName());
        return userService.getUserProjectDetails(user.getName());
    }

}
