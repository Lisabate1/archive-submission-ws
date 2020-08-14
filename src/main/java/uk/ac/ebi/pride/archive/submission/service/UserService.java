package uk.ac.ebi.pride.archive.submission.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.archive.repo.client.UserProfileRepoClient;
import uk.ac.ebi.pride.archive.repo.client.UserRepoClient;
import uk.ac.ebi.pride.archive.repo.models.project.ProjectSummary;
import uk.ac.ebi.pride.archive.repo.models.user.Credentials;
import uk.ac.ebi.pride.archive.repo.models.user.User;
import uk.ac.ebi.pride.archive.repo.models.user.UserSummary;
import uk.ac.ebi.pride.archive.submission.model.project.ProjectDetail;
import uk.ac.ebi.pride.archive.submission.model.project.ProjectDetailList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserProfileRepoClient userProfileRepoClient;

    private final UserRepoClient userRepoClient;

    public UserService(UserProfileRepoClient userProfileRepoClient, UserRepoClient userRepoClient) {
        this.userProfileRepoClient = userProfileRepoClient;
        this.userRepoClient = userRepoClient;
    }

    public UserSummary getUserDetails(Credentials credentials) throws Exception {
        String jwtToken = userProfileRepoClient.getAAPToken(credentials);
        return userProfileRepoClient.viewProfile(jwtToken);
    }


    public ProjectDetailList getUserProjectDetails(String userEmail) {
        ProjectDetailList projectDetailList = new ProjectDetailList();
        try {
            Optional<User> user = userRepoClient.findByEmail(userEmail);
            if (user.isPresent()) {
                Optional<List<ProjectSummary>> projectSummaries = userRepoClient.findAllProjectsById(user.get().getId());
                if (projectSummaries.isPresent()) {
                    List<ProjectDetail> projectDetails = projectSummaries.get().stream()
                            .filter(ps -> !ps.isPublicProject())
                            .map(ps -> new ProjectDetail(ps.getAccession()))
                            .collect(Collectors.toList());
                    projectDetailList.setProjectDetails(projectDetails);
                }
            }
        } catch (Exception exception) {
            logger.error("Error in fetching project details for user");
        }
        return projectDetailList;
    }
}
