package com.epam.jsp.agent;

/**
 * Enum of job statuses.
 */
public enum JobStatus {
    /**
     * Job was failed.
     */
    FAILED,
    /**
     * Job was unstable.
     */
    WARNING,
    /**
     * No information about build.
     */
    NO_INFORMATION,
    /**
     * Job was successfully built.
     */
    SUCCESS,
    /**
     * Build is in progress
     */
    IN_PROGRESS
}
