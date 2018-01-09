/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package embedded;

import h264.FrameQueueFiller;

public class WebServerProvider {

    public static EmbeddedWebServer getEmbeddedWebserver(FrameQueueFiller queueFiller) {
        return new JettyEmbeddedWebServer(queueFiller);
    }

}
