package com.epam.jsp.agent;

import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.helper.Range;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests to verify logic of the {@link JenkinsBuildInformationService}.
 */
public class JenkinsBuildInformationServiceTest {

    public static final String JOB_NAME = "test";
    public static final String JOB_PATH = "job/test/";

    @Test
    void testGetBuildInformation() throws IOException {
        JenkinsHttpClient mockJenkinsHttpClient = mock(JenkinsHttpClient.class);
        BuildInformationService buildInformationService = new JenkinsBuildInformationService(
                mockJenkinsHttpClient, JOB_NAME);
        JobWithDetails mockJobWithDetails = mock(JobWithDetails.class);
        when(mockJobWithDetails.getName()).thenReturn("TEST");
        when(mockJenkinsHttpClient.get(JOB_PATH, JobWithDetails.class)).thenReturn(mockJobWithDetails);
        when(mockJobWithDetails.getNextBuildNumber()).thenReturn(2);
        Build mockBuild = mock(Build.class);
        when(mockJobWithDetails.getAllBuilds(argThat(
                        t -> (Range.CURLY_BRACKET_OPEN + "0," + Range.CURLY_BRACKET_CLOSE)
                                .equals(t.getRangeString()))))
                .thenReturn(Collections.singletonList(mockBuild));
        BuildWithDetails mockBuildWithDetails = mock(BuildWithDetails.class);
        when(mockBuild.details()).thenReturn(mockBuildWithDetails);
        when(mockBuildWithDetails.getResult()).thenReturn(BuildResult.SUCCESS);

        BuildInformation expectedBuildInformation = new BuildInformation();
        expectedBuildInformation.setJobName("TEST");
        expectedBuildInformation.setJobStatus(JobStatus.GREEN);
        List<BuildInformation> buildInformationList = buildInformationService.getBuildInformation();

        verify(mockJenkinsHttpClient).get(JOB_PATH, JobWithDetails.class);
        verify(mockJobWithDetails).getNextBuildNumber();
        verify(mockJobWithDetails).getName();
        verify(mockJobWithDetails).getAllBuilds(argThat(
                t -> (Range.CURLY_BRACKET_OPEN + "0," + Range.CURLY_BRACKET_CLOSE).equals(t.getRangeString())));
        verify(mockBuild).details();
        verify(mockBuildWithDetails).getResult();
        validateMockitoUsage();
        assertNotNull(buildInformationList);
        assertEquals(1, buildInformationList.size());
        assertNotNull(buildInformationList.get(0));
        assertTrue(buildInformationList.contains(expectedBuildInformation));
    }

    @Test
    void testGetBuildInformationForUnstableJob() throws IOException {
        JenkinsHttpClient mockJenkinsHttpClient = mock(JenkinsHttpClient.class);
        BuildInformationService buildInformationService = new JenkinsBuildInformationService(
                mockJenkinsHttpClient, JOB_NAME);
        JobWithDetails mockJobWithDetails = mock(JobWithDetails.class);
        when(mockJobWithDetails.getName()).thenReturn("TEST");
        when(mockJenkinsHttpClient.get(JOB_PATH, JobWithDetails.class)).thenReturn(mockJobWithDetails);
        when(mockJobWithDetails.getNextBuildNumber()).thenReturn(2);
        Build mockBuild = mock(Build.class);
        when(mockJobWithDetails.getAllBuilds(argThat(
                t -> (Range.CURLY_BRACKET_OPEN + "0," + Range.CURLY_BRACKET_CLOSE)
                        .equals(t.getRangeString()))))
                .thenReturn(Collections.singletonList(mockBuild));
        BuildWithDetails mockBuildWithDetails = mock(BuildWithDetails.class);
        when(mockBuild.details()).thenReturn(mockBuildWithDetails);
        when(mockBuildWithDetails.getResult()).thenReturn(BuildResult.UNSTABLE);

        BuildInformation expectedBuildInformation = new BuildInformation();
        expectedBuildInformation.setJobName("TEST");
        expectedBuildInformation.setJobStatus(JobStatus.YELLOW);
        List<BuildInformation> buildInformationList = buildInformationService.getBuildInformation();

        verify(mockJenkinsHttpClient).get(JOB_PATH, JobWithDetails.class);
        verify(mockJobWithDetails).getNextBuildNumber();
        verify(mockJobWithDetails).getName();
        verify(mockJobWithDetails).getAllBuilds(argThat(
                t -> (Range.CURLY_BRACKET_OPEN + "0," + Range.CURLY_BRACKET_CLOSE).equals(t.getRangeString())));
        verify(mockBuild).details();
        verify(mockBuildWithDetails).getResult();
        validateMockitoUsage();
        assertNotNull(buildInformationList);
        assertEquals(1, buildInformationList.size());
        assertNotNull(buildInformationList.get(0));
        assertTrue(buildInformationList.contains(expectedBuildInformation));
    }

    @Test
    void testGetBuildInformationForFailedJob() throws IOException {
        JenkinsHttpClient mockJenkinsHttpClient = mock(JenkinsHttpClient.class);
        BuildInformationService buildInformationService = new JenkinsBuildInformationService(
                mockJenkinsHttpClient, JOB_NAME);
        JobWithDetails mockJobWithDetails = mock(JobWithDetails.class);
        when(mockJobWithDetails.getName()).thenReturn("TEST");
        when(mockJenkinsHttpClient.get(JOB_PATH, JobWithDetails.class)).thenReturn(mockJobWithDetails);
        when(mockJobWithDetails.getNextBuildNumber()).thenReturn(2);
        Build mockBuild = mock(Build.class);
        when(mockJobWithDetails.getAllBuilds(argThat(
                t -> (Range.CURLY_BRACKET_OPEN + "0," + Range.CURLY_BRACKET_CLOSE)
                        .equals(t.getRangeString()))))
                .thenReturn(Collections.singletonList(mockBuild));
        BuildWithDetails mockBuildWithDetails = mock(BuildWithDetails.class);
        when(mockBuild.details()).thenReturn(mockBuildWithDetails);
        when(mockBuildWithDetails.getResult()).thenReturn(BuildResult.FAILURE);

        BuildInformation expectedBuildInformation = new BuildInformation();
        expectedBuildInformation.setJobName("TEST");
        expectedBuildInformation.setJobStatus(JobStatus.RED);
        List<BuildInformation> buildInformationList = buildInformationService.getBuildInformation();

        verify(mockJenkinsHttpClient).get(JOB_PATH, JobWithDetails.class);
        verify(mockJobWithDetails).getNextBuildNumber();
        verify(mockJobWithDetails).getName();
        verify(mockJobWithDetails).getAllBuilds(argThat(
                t -> (Range.CURLY_BRACKET_OPEN + "0," + Range.CURLY_BRACKET_CLOSE).equals(t.getRangeString())));
        verify(mockBuild).details();
        verify(mockBuildWithDetails).getResult();
        validateMockitoUsage();
        assertNotNull(buildInformationList);
        assertEquals(1, buildInformationList.size());
        assertNotNull(buildInformationList.get(0));
        assertTrue(buildInformationList.contains(expectedBuildInformation));
    }

    @Test
    void testGetBuildInformationFor4Builds() throws IOException {
        JenkinsHttpClient mockJenkinsHttpClient = mock(JenkinsHttpClient.class);
        BuildInformationService buildInformationService = new JenkinsBuildInformationService(
                mockJenkinsHttpClient, JOB_NAME);
        JobWithDetails mockJobWithDetails = mock(JobWithDetails.class);
        when(mockJobWithDetails.getName()).thenReturn("TEST");
        when(mockJenkinsHttpClient.get(JOB_PATH, JobWithDetails.class)).thenReturn(mockJobWithDetails);
        when(mockJobWithDetails.getNextBuildNumber()).thenReturn(5);
        Build mockBuild = mock(Build.class);
        List<Build> buildList = createExpectedList(mockBuild, 4);
        when(mockJobWithDetails.getAllBuilds(argThat(
                t -> (Range.CURLY_BRACKET_OPEN + "0," + Range.CURLY_BRACKET_CLOSE)
                        .equals(t.getRangeString()))))
                .thenReturn(buildList);
        BuildWithDetails mockBuildWithDetails = mock(BuildWithDetails.class);
        when(mockBuild.details()).thenReturn(mockBuildWithDetails);
        when(mockBuildWithDetails.getResult()).thenReturn(BuildResult.SUCCESS);

        BuildInformation expectedBuildInformation = new BuildInformation();
        expectedBuildInformation.setJobName("TEST");
        expectedBuildInformation.setJobStatus(JobStatus.GREEN);
        List<BuildInformation> buildInformationList = buildInformationService.getBuildInformation();

        verify(mockJenkinsHttpClient).get(JOB_PATH, JobWithDetails.class);
        verify(mockJobWithDetails).getNextBuildNumber();
        verify(mockJobWithDetails).getName();
        verify(mockJobWithDetails).getAllBuilds(argThat(
                t -> (Range.CURLY_BRACKET_OPEN + "0," + Range.CURLY_BRACKET_CLOSE).equals(t.getRangeString())));
        verify(mockBuild).details();
        verify(mockBuildWithDetails).getResult();
        validateMockitoUsage();
        assertNotNull(buildInformationList);
        assertEquals(4, buildInformationList.size());
        assertNotNull(buildInformationList.get(0));
        assertTrue(buildInformationList.contains(expectedBuildInformation));
        assertNotNull(buildInformationList.get(1));
        assertNotNull(buildInformationList.get(2));
        assertNotNull(buildInformationList.get(3));
    }

    private List<Build> createExpectedList(Build mockBuild, int size) {
        List<Build> expectedListOfBuilds = new ArrayList<>(size);
        expectedListOfBuilds.add(mockBuild);
        IntStream.range(1, size).forEach(i -> {
            Build build = mock(Build.class);
            BuildWithDetails buildWithDetails = mock(BuildWithDetails.class);
            try {
                when(build.details()).thenReturn(buildWithDetails);
            } catch (IOException e) {
                LoggerFactory.getLogger(this.getClass()).error("Test exception", e);
            }
            when(buildWithDetails.getResult()).thenReturn(BuildResult.UNKNOWN);
            expectedListOfBuilds.add(build);
        });
        return expectedListOfBuilds;
    }

    @Test
    void testGetBuildInformationFor5Builds() throws IOException {
        JenkinsHttpClient mockJenkinsHttpClient = mock(JenkinsHttpClient.class);
        BuildInformationService buildInformationService = new JenkinsBuildInformationService(
                mockJenkinsHttpClient, JOB_NAME);
        JobWithDetails mockJobWithDetails = mock(JobWithDetails.class);
        when(mockJobWithDetails.getName()).thenReturn("TEST");
        when(mockJenkinsHttpClient.get(JOB_PATH, JobWithDetails.class)).thenReturn(mockJobWithDetails);
        when(mockJobWithDetails.getNextBuildNumber()).thenReturn(6);
        Build mockBuild = mock(Build.class);
        List<Build> buildList = createExpectedList(mockBuild, 5);
        when(mockJobWithDetails.getAllBuilds(argThat(
                t -> (Range.CURLY_BRACKET_OPEN + "0," + Range.CURLY_BRACKET_CLOSE)
                        .equals(t.getRangeString()))))
                .thenReturn(buildList);
        BuildWithDetails mockBuildWithDetails = mock(BuildWithDetails.class);
        when(mockBuild.details()).thenReturn(mockBuildWithDetails);
        when(mockBuildWithDetails.getResult()).thenReturn(BuildResult.SUCCESS);

        BuildInformation expectedBuildInformation = new BuildInformation();
        expectedBuildInformation.setJobName("TEST");
        expectedBuildInformation.setJobStatus(JobStatus.GREEN);
        List<BuildInformation> buildInformationList = buildInformationService.getBuildInformation();

        verify(mockJenkinsHttpClient).get(JOB_PATH, JobWithDetails.class);
        verify(mockJobWithDetails).getNextBuildNumber();
        verify(mockJobWithDetails).getName();
        verify(mockJobWithDetails).getAllBuilds(argThat(
                t -> (Range.CURLY_BRACKET_OPEN + "0," + Range.CURLY_BRACKET_CLOSE).equals(t.getRangeString())));
        verify(mockBuild).details();
        verify(mockBuildWithDetails).getResult();
        validateMockitoUsage();
        assertNotNull(buildInformationList);
        assertEquals(5, buildInformationList.size());
        assertNotNull(buildInformationList.get(0));
        assertTrue(buildInformationList.contains(expectedBuildInformation));
        assertNotNull(buildInformationList.get(1));
        assertNotNull(buildInformationList.get(2));
        assertNotNull(buildInformationList.get(3));
        assertNotNull(buildInformationList.get(4));
    }

    @Test
    void testGetBuildInformationFor6Build() throws IOException {
        JenkinsHttpClient mockJenkinsHttpClient = mock(JenkinsHttpClient.class);
        BuildInformationService buildInformationService = new JenkinsBuildInformationService(
                mockJenkinsHttpClient, JOB_NAME);
        JobWithDetails mockJobWithDetails = mock(JobWithDetails.class);
        when(mockJobWithDetails.getName()).thenReturn("TEST");
        when(mockJenkinsHttpClient.get(JOB_PATH, JobWithDetails.class)).thenReturn(mockJobWithDetails);
        when(mockJobWithDetails.getNextBuildNumber()).thenReturn(7);
        Build mockBuild = mock(Build.class);
        List<Build> buildList = createExpectedList(mockBuild, 5);
        when(mockJobWithDetails.getAllBuilds(argThat(
                t -> (Range.CURLY_BRACKET_OPEN + "1," + Range.CURLY_BRACKET_CLOSE)
                        .equals(t.getRangeString()))))
                .thenReturn(buildList);
        BuildWithDetails mockBuildWithDetails = mock(BuildWithDetails.class);
        when(mockBuild.details()).thenReturn(mockBuildWithDetails);
        when(mockBuildWithDetails.getResult()).thenReturn(BuildResult.SUCCESS);

        BuildInformation expectedBuildInformation = new BuildInformation();
        expectedBuildInformation.setJobName("TEST");
        expectedBuildInformation.setJobStatus(JobStatus.GREEN);
        List<BuildInformation> buildInformationList = buildInformationService.getBuildInformation();

        verify(mockJenkinsHttpClient).get(JOB_PATH, JobWithDetails.class);
        verify(mockJobWithDetails).getNextBuildNumber();
        verify(mockJobWithDetails).getName();
        verify(mockJobWithDetails).getAllBuilds(argThat(
                t -> (Range.CURLY_BRACKET_OPEN + "1," + Range.CURLY_BRACKET_CLOSE).equals(t.getRangeString())));
        verify(mockBuild).details();
        verify(mockBuildWithDetails).getResult();
        validateMockitoUsage();
        assertNotNull(buildInformationList);
        assertEquals(5, buildInformationList.size());
        assertNotNull(buildInformationList.get(0));
        assertTrue(buildInformationList.contains(expectedBuildInformation));
        assertNotNull(buildInformationList.get(1));
        assertNotNull(buildInformationList.get(2));
        assertNotNull(buildInformationList.get(3));
        assertNotNull(buildInformationList.get(4));
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
                mockJenkinsHttpClient, JOB_NAME);
        String exceptionMessage = "Exception from test";
        when(mockJenkinsHttpClient.get(JOB_PATH, JobWithDetails.class))
                .thenThrow(new IOException(exceptionMessage));

        try {
            buildInformationService.getBuildInformation();
        } catch (RuntimeException exc) {
            Throwable throwable = exc.getCause();
            assertEquals(exceptionMessage, throwable.getMessage());
        }

        verify(mockJenkinsHttpClient).get(JOB_PATH, JobWithDetails.class);
        validateMockitoUsage();
    }

    @Test
    void testGetBuildInformationNoBuilds() throws IOException {
        JenkinsHttpClient mockJenkinsHttpClient = mock(JenkinsHttpClient.class);
        BuildInformationService buildInformationService = new JenkinsBuildInformationService(
                mockJenkinsHttpClient, JOB_NAME);
        JobWithDetails mockJobWithDetails = mock(JobWithDetails.class);
        when(mockJenkinsHttpClient.get(JOB_PATH, JobWithDetails.class)).thenReturn(mockJobWithDetails);
        when(mockJobWithDetails.getNextBuildNumber()).thenReturn(1);

        List<BuildInformation> buildInformationList = buildInformationService.getBuildInformation();

        verify(mockJenkinsHttpClient).get(JOB_PATH, JobWithDetails.class);
        verify(mockJobWithDetails).getNextBuildNumber();
        validateMockitoUsage();
        assertNotNull(buildInformationList);
        assertTrue(buildInformationList.isEmpty());
    }
}
