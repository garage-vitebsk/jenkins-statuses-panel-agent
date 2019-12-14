package com.epam.jsp.agent;

/**
 * Allows to integrate with different build information providers.
 */
public interface BuildInformationService {

    /**
     * Gets build information.
     *
     * @return build information.
     */
    BuildInformation getBuildInformation();
}
