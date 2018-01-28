/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package embedded;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

class WebSocketConnection {

    private RemoteEndpoint remoteEndpoint;
    private BlockingQueue<ByteBuffer> h264Frames;
    private Thread workerThread;
    private boolean cancel = false;

    WebSocketConnection(RemoteEndpoint remoteEndpoint,
                        BlockingQueue<ByteBuffer> h264Frames) {
        this.remoteEndpoint = remoteEndpoint;
        this.h264Frames = h264Frames;
    }

    void start() {
        workerThread = new Thread(this::work);
        workerThread.start();
    }

    private void work() {
        ByteBuffer bb;
        while (!cancel) {
            try {
                bb = h264Frames.take();
                remoteEndpoint.sendBytes(bb);
            } catch (Exception e) {
                break;
            }
        }
    }

    BlockingQueue<ByteBuffer> getH264FramesQueue() {
        return h264Frames;
    }

    void cancel() {
        cancel = true;
        workerThread.interrupt();
    }

}
