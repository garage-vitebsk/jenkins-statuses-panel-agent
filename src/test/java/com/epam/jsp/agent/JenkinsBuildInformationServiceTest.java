package com.epam.jsp.agent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests to verify logic of the {@link JenkinsBuildInformationService}.
 */
public class JenkinsBuildInformationServiceTest {

    @Test
    public void testGetBuildInformation() {
        BuildInformationService buildInformationService = new JenkinsBuildInformationService();
        BuildInformation buildInformation = buildInformationService.getBuildInformation();
        Assertions.assertNotNull(buildInformation);
        Assertions.assertEquals("TEST", buildInformation.getJobName());
        Assertions.assertEquals("GREEN", buildInformation.getJobStatus());
    }
}
