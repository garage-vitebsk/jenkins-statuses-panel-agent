package com.epam.jsp.agent;

/**
 * Implementation of {@link BuildInformationService} to connect and to get build information from Jenkins.
 */
public class JenkinsBuildInformationService implements BuildInformationService {
    @Override
    public BuildInformation getBuildInformation() {
        BuildInformation buildInformation = new BuildInformation();
        buildInformation.setJobName("TEST");
        buildInformation.setJobStatus("GREEN");
        return buildInformation;
    }
}
