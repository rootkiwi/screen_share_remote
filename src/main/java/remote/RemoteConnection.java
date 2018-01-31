/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package remote;

import embedded.EmbeddedWebServer;
import embedded.WebServerProvider;
import h264.FrameQueueFiller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static remote.ConnectionHelper.byteArrayToInt;
import static remote.ConnectionHelper.readAll;
import static remote.RemoteMessageTypes.FIRST_NEW_CONNECTION;
import static remote.RemoteMessageTypes.NEW_CONNECTION;
import static remote.RemoteMessageTypes.ZERO_CONNECTIONS;

public class RemoteConnection implements FrameQueueFiller {

    private List<BlockingQueue<ByteBuffer>> connectionQueues = new ArrayList<>();
    private InputStream in;
    private OutputStream out;
    private RemoteConnectionCallback callback;
    private EmbeddedWebServer webServer;
    private boolean cancel = false;

    RemoteConnection(InputStream in, OutputStream out, RemoteConnectionCallback callback) {
        this.in = in;
        this.out = out;
        this.callback = callback;
        webServer = WebServerProvider.getEmbeddedWebserver(this);
    }

    @Override
    public synchronized void addQueue(BlockingQueue<ByteBuffer> queue) {
        if (connectionQueues.size() == 0) {
            writeMessage(FIRST_NEW_CONNECTION);
        } else {
            writeMessage(NEW_CONNECTION);
        }
        connectionQueues.add(queue);
        notifyAll();
    }

    @Override
    public synchronized void removeQueue(BlockingQueue<ByteBuffer> queue) {
        if (connectionQueues.size() == 1) {
            writeMessage(ZERO_CONNECTIONS);
        }
        connectionQueues.remove(queue);
    }

    private void writeMessage(int message) {
        try {
            out.write(message);
        } catch (IOException e) {
            System.err.println("error writing on socket: " + e.getMessage());
            cancel = true;
        }
    }

    void start(int webPort, String pageTitle) {
        webServer.start(webPort, pageTitle);
        new Thread(this::work).start();
    }

    private void work() {
        byte[] frame;
        ByteBuffer buf;
        while (!cancel) {
            try {
                int size = byteArrayToInt(readAll(4, in));
                frame = readAll(size, in);
                buf = ByteBuffer.wrap(frame);
                synchronized (this) {
                    for (BlockingQueue<ByteBuffer> networkQueue : connectionQueues) {
                        ByteBuffer duplicate = buf.duplicate();
                        boolean queueIsFull = !networkQueue.offer(duplicate);
                        if (queueIsFull) {
                            networkQueue.clear();
                            networkQueue.add(duplicate);
                        }
                    }
                }
            } catch (Exception e) {
                break;
            }
        }
        webServer.stop();
        callback.disconnected();
    }

    void cancel() {
        cancel = true;
    }

}
