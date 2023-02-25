package uk.ac.ebi.pride.archive.submission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.pride.archive.submission.config.SubmissionApiConfig;

import java.util.Arrays;

@Service
@Slf4j
public class ValidationService {

    private SubmissionApiConfig submissionApiConfig;

    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;

    public ValidationService(SubmissionApiConfig submissionApiConfig, @Qualifier("proxyRestTemplate") RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.submissionApiConfig = submissionApiConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void validateTicket(String ticketId) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.ALL));
        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload = objectMapper.writeValueAsString(submissionApiConfig.getCredentials());
        HttpEntity<MultiValueMap<String, String>> loginRequestEntity = new HttpEntity(payload, headers);
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(submissionApiConfig.getLoginUrl(), loginRequestEntity, String.class);
        if (loginResponse.getStatusCode() == HttpStatus.OK) {
            String token = loginResponse.getBody();
            headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.ALL));
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<MultiValueMap<String, String>> validationRequestEntity = new HttpEntity(headers);
            String validationAndSubmissionUrl = submissionApiConfig.getValidationAndSubmissionUrl() + "?verifyCommand=false&ticket=" + ticketId;

            ResponseEntity<String> response = restTemplate.postForEntity(validationAndSubmissionUrl, validationRequestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully sent validationAndSubmission request to submission-api for ticketId : " +
                        ticketId + " - " + response.getBody().replaceAll("\n", " ### "));
            } else {
                log.error("Failed to send validationAndSubmission request to submission-api for ticketId : " + ticketId + " : " + validationAndSubmissionUrl);
            }
        } else {
            log.error("Failed login to submission-api : " + loginResponse.getStatusCode() + " " + loginResponse.getBody());
        }

    }
}
