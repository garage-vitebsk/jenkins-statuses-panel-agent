package com.epam.jsp.agent;

import lombok.Data;

/**
 * DTO to hold information about builds.
 */
@Data
public class BuildInformation {
    private String jobName;
    private String jobStatus;
}
