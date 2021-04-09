package uk.ac.ebi.pride.archive.submission.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.pride.archive.repo.client.PrideRepoClientFactory;
import uk.ac.ebi.pride.archive.repo.client.TicketRepoClient;
import uk.ac.ebi.pride.archive.repo.client.UserProfileRepoClient;
import uk.ac.ebi.pride.archive.repo.client.UserRepoClient;

@Configuration
public class RepoConfig {

    private final PrideRepoClientFactory prideRepoClientFactory;

    public RepoConfig(@Value("${pride-repo.api.baseUrl}") String apiBaseUrl,
                      @Value("${pride-repo.api.keyName}") String apiKeyName,
                      @Value("${pride-repo.api.keyValue}") String apiKeyValue,
                      @Value("${app.name}") String appName) {
        this.prideRepoClientFactory = new PrideRepoClientFactory(apiBaseUrl, apiKeyName, apiKeyValue, appName);
    }

    @Bean
    public UserRepoClient getUserRepoClient() {
        return prideRepoClientFactory.getUserRepoClient();
    }

    @Bean
    public TicketRepoClient getTicketRepoClient() {
        return prideRepoClientFactory.getTicketRepoClient();
    }

    @Bean
    public UserProfileRepoClient getUserProfileRepoClient() {
        return prideRepoClientFactory.getUserProfileRepoClient();
    }

}

