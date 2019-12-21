package com.epam.jsp.agent;

import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.helper.Range;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link BuildInformationService} to connect and to get build information from Jenkins.
 */
public class JenkinsBuildInformationService implements BuildInformationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsBuildInformationService.class);


    private JenkinsHttpClient jenkinsHttpClient;

    public JenkinsBuildInformationService(JenkinsHttpClient jenkinsHttpClient) {
        this.jenkinsHttpClient = jenkinsHttpClient;
    }

    @Override
    public List<BuildInformation> getBuildInformation() {
        List<BuildInformation> buildInformationList = Collections.emptyList();
        JobWithDetails jobWithDetails;
        try {
            jobWithDetails = jenkinsHttpClient.get("", JobWithDetails.class);
        } catch (IOException e) {
            LOGGER.error("Can't process information from server", e);
            throw new RuntimeException(e);
        }

        if (null != jobWithDetails) {
            int nextBuildNumber = jobWithDetails.getNextBuildNumber();
            if (nextBuildNumber > 1) {
                try {
                    String jobName = jobWithDetails.getName();
                    List<Build> builds = jobWithDetails.getAllBuilds(Range.build().from(nextBuildNumber - 1).build());
                    BuildInformation buildInformation = new BuildInformation();
                    buildInformation.setJobName(jobName);
                    buildInformation.setJobStatus(JobStatus.GREEN);
                    buildInformationList = new ArrayList<>(5);
                    for (int i = 0; i < 5 && i < (nextBuildNumber - 1); i++) {
                        buildInformationList.add(buildInformation);
                    }
                } catch (IOException e) {
                    LOGGER.error("Can't get build for range [{}, ...]", nextBuildNumber - 1, e);
                    throw new RuntimeException(e);
                }
            }
        }

        return buildInformationList;
    }
}
