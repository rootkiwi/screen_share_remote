/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package config;

import java.io.IOException;
import java.nio.file.Path;

public interface ConfigFileCreator {

    void createConfigFile(int port,
                          int webPort,
                          String passwordHash,
                          Base64KeyCertHolder holder,
                          Path configFile) throws IOException;

}
