/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package remote;

import config.ConfigHolder;

public interface RemoteConnectionHandler {

    void start(ConfigHolder config);
    void stop();

}
