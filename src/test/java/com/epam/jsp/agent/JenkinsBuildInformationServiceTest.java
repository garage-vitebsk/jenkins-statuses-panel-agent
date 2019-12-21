package com.epam.jsp.agent;

import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.helper.Range;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests to verify logic of the {@link JenkinsBuildInformationService}.
 */
public class JenkinsBuildInformationServiceTest {

    @Test
    void testGetBuildInformation() throws IOException {
        JenkinsHttpClient mockJenkinsHttpClient = mock(JenkinsHttpClient.class);
        BuildInformationService buildInformationService = new JenkinsBuildInformationService(
                mockJenkinsHttpClient);
        JobWithDetails mockJobWithDetails = mock(JobWithDetails.class);
        when(mockJobWithDetails.getName()).thenReturn("TEST");
        when(mockJenkinsHttpClient.get("", JobWithDetails.class)).thenReturn(mockJobWithDetails);
        when(mockJobWithDetails.getNextBuildNumber()).thenReturn(2);
        Build mockBuild = mock(Build.class);
        when(mockJobWithDetails.getAllBuilds(argThat(t -> "{1,}".equals(t.getRangeString()))))
                .thenReturn(Collections.singletonList(mockBuild));

        BuildInformation expectedBuildInformation = new BuildInformation();
        expectedBuildInformation.setJobName("TEST");
        expectedBuildInformation.setJobStatus(JobStatus.GREEN);
        List<BuildInformation> buildInformationList = buildInformationService.getBuildInformation();

        verify(mockJenkinsHttpClient).get("", JobWithDetails.class);
        verify(mockJobWithDetails).getNextBuildNumber();
        verify(mockJobWithDetails).getName();
        verify(mockJobWithDetails).getAllBuilds(any(Range.class));
        validateMockitoUsage();
        assertNotNull(buildInformationList);
        assertTrue(buildInformationList.contains(expectedBuildInformation));
    }

    /**
     * Checks cases: there is no connection to the server, there is no job on the server and other
     * cases with retrieving information from the server during first call.
     *
     * @throws IOException declared in interface of used objects.
     */
    @Test
    void testGetBuildInformationNoConnection() throws IOException {
        JenkinsHttpClient mockJenkinsHttpClient = mock(JenkinsHttpClient.class);
        BuildInformationService buildInformationService = new JenkinsBuildInformationService(
                mockJenkinsHttpClient);
        String exceptionMessage = "Exception from test";
        when(mockJenkinsHttpClient.get("", JobWithDetails.class))
                .thenThrow(new IOException(exceptionMessage));

        try {
            buildInformationService.getBuildInformation();
        } catch (RuntimeException exc) {
            Throwable throwable = exc.getCause();
            assertEquals(exceptionMessage, throwable.getMessage());
        }

        verify(mockJenkinsHttpClient).get("", JobWithDetails.class);
        validateMockitoUsage();
    }

    @Test
    void testGetBuildInformationNoBuilds() throws IOException {
        JenkinsHttpClient mockJenkinsHttpClient = mock(JenkinsHttpClient.class);
        BuildInformationService buildInformationService = new JenkinsBuildInformationService(
                mockJenkinsHttpClient);
        JobWithDetails mockJobWithDetails = mock(JobWithDetails.class);
        when(mockJenkinsHttpClient.get("", JobWithDetails.class)).thenReturn(mockJobWithDetails);
        when(mockJobWithDetails.getNextBuildNumber()).thenReturn(1);

        List<BuildInformation> buildInformationList = buildInformationService.getBuildInformation();

        verify(mockJenkinsHttpClient).get("", JobWithDetails.class);
        verify(mockJobWithDetails).getNextBuildNumber();
        validateMockitoUsage();
        assertNotNull(buildInformationList);
        assertTrue(buildInformationList.isEmpty());
    }
}
