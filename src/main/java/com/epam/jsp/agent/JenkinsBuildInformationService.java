package com.epam.jsp.agent;

import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.helper.Range;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
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
    private final String jobPath;


    private JenkinsHttpClient jenkinsHttpClient;

    public JenkinsBuildInformationService(JenkinsHttpClient jenkinsHttpClient, String jobName) {
        this.jenkinsHttpClient = jenkinsHttpClient;
        this.jobPath = "job/" + jobName + "/";
    }

    @Override
    public List<BuildInformation> getBuildInformation() {
        List<BuildInformation> buildInformationList = Collections.emptyList();
        JobWithDetails jobWithDetails;
        try {
            jobWithDetails = jenkinsHttpClient.get(jobPath, JobWithDetails.class);
        } catch (IOException e) {
            LOGGER.error("Can't process information from server", e);
            throw new RuntimeException(e);
        }

        if (null != jobWithDetails) {
            int nextBuildNumber = jobWithDetails.getNextBuildNumber();
            if (nextBuildNumber > 1) {
                try {
                    String jobName = jobWithDetails.getName();
                    int startIndex = nextBuildNumber > 6 ? nextBuildNumber - 6 : 0;
                    List<Build> builds = jobWithDetails.getAllBuilds(Range.build().from(startIndex).build());
                    int countBuilds = (6 < nextBuildNumber) ? 5 : nextBuildNumber - 1;
                    buildInformationList = new ArrayList<>(countBuilds);
                    for (int i = 0; (i < 5 && i < countBuilds); i++) {
                        BuildInformation buildInformation = new BuildInformation();
                        buildInformation.setJobName(jobName);
                        Build build = builds.get(i);
                        if (null != build) {
                            BuildWithDetails buildWithDetails = build.details();
                            switch (buildWithDetails.getResult()) {
                                case SUCCESS:
                                    buildInformation.setJobStatus(JobStatus.GREEN);
                                    break;
                                case FAILURE:
                                    buildInformation.setJobStatus(JobStatus.RED);
                                    break;
                                case UNSTABLE:
                                    buildInformation.setJobStatus(JobStatus.YELLOW);
                                    break;
                                default:
                                    buildInformation.setJobStatus(JobStatus.BLACK);
                                    break;
                            }
                        } else {
                            LOGGER.warn("No build information by some reason.");
                        }
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
