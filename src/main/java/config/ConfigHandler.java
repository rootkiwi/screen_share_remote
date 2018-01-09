/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package config;

public class ConfigHandler {

    private ConfigHandler() {
    }

    public static ConfigFileCreator getCreator() {
        return new SimpleConfigFileCreator();
    }

    public static ConfigFileParser getParser() {
        return new SimpleConfigFileParser();
    }

}
