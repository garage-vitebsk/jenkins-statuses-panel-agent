package com.epam.jsp.agent;

import java.util.List;

/**
 * Allows to integrate with different build information providers.
 */
public interface BuildInformationService {

    /**
     * Gets build information for last 5 builds.
     *
     * @return list of build information.
     */
    List<BuildInformation> getBuildInformation();
}
