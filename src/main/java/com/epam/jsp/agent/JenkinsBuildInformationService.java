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
                    List<Build> builds = jobWithDetails.getAllBuilds(Range.build().to(5).build());
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
                                    buildInformation.setJobStatus(JobStatus.SUCCESS);
                                    break;
                                case FAILURE:
                                    buildInformation.setJobStatus(JobStatus.FAILED);
                                    break;
                                case UNSTABLE:
                                case CANCELLED:
                                case ABORTED:
                                    buildInformation.setJobStatus(JobStatus.WARNING);
                                    break;
                                case BUILDING:
                                case REBUILDING:
                                    buildInformation.setJobStatus(JobStatus.IN_PROGRESS);
                                    break;
                                case UNKNOWN:
                                case NOT_BUILT:
                                default:
                                    buildInformation.setJobStatus(JobStatus.NO_INFORMATION);
                                    break;
                            }
                        } else {
                            LOGGER.warn("No build information by some reason.");
                        }
                        buildInformationList.add(buildInformation);
                    }
                } catch (NullPointerException exc) {
                    LOGGER.warn("No build information");
                } catch (IOException e) {
                    LOGGER.error("Can't get build for range [{}, ...]", nextBuildNumber - 1, e);
                    throw new RuntimeException(e);
                }
            }
        }

        return buildInformationList;
    }
}
