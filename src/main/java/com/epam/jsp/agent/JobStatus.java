package com.epam.jsp.agent;

/**
 * Enum of job statuses.
 */
public enum JobStatus {
    /**
     * Job was failed.
     */
    RED,
    /**
     * Job was unstable.
     */
    YELLOW,
    /**
     * No information about build.
     */
    BLACK,
    /**
     * Job was successfully built.
     */
    GREEN
}
