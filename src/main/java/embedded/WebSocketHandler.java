/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package embedded;

import h264.FrameQueueFiller;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@WebSocket
public class WebSocketHandler {

    private static Map<Session, WebSocketConnection> connections = new HashMap<>();

    private FrameQueueFiller queueFiller;

    WebSocketHandler(FrameQueueFiller queueFiller) {
        this.queueFiller = queueFiller;
    }

    @OnWebSocketConnect
    public void connected(Session session) {
        BlockingQueue<ByteBuffer> h264Frames = new ArrayBlockingQueue<>(80);
        WebSocketConnection wsc = new WebSocketConnection(session.getRemote(), h264Frames);
        wsc.start();
        queueFiller.addQueue(h264Frames);
        connections.put(session, wsc);
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        BlockingQueue<ByteBuffer> h264Frames = connections.get(session).getH264FramesQueue();
        queueFiller.removeQueue(h264Frames);
        connections.remove(session);
    }

    static void cancelAllConnections() {
        try {
            connections.values().forEach(WebSocketConnection::cancel);
        } catch (Exception e) {}
    }

}
