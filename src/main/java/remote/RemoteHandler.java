/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package remote;

import config.ConfigHolder;
import crypto.PasswordHashFactory;
import crypto.TlsHelper;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static crypto.TlsHelper.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384;
import static crypto.TlsHelper.TLSv12;
import static remote.ConnectionHelper.byteArrayToInt;
import static remote.ConnectionHelper.readAll;

public class RemoteHandler implements RemoteConnectionHandler, RemoteConnectionCallback {

    private SSLServerSocket tlsServerSocket;
    private boolean stop = false;
    private RemoteConnection remoteConnection;
    private AtomicBoolean remoteConnected = new AtomicBoolean(false);

    public RemoteHandler() {
    }

    @Override
    public void start(ConfigHolder config) {
        try {
            tlsServerSocket = TlsHelper.getTlsServerSocket(config.certificate, config.privateKey);
            tlsServerSocket.setEnabledProtocols(TLSv12);
            tlsServerSocket.setEnabledCipherSuites(TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384);
            tlsServerSocket.setNeedClientAuth(false);
            tlsServerSocket.setUseClientMode(false);
            tlsServerSocket.bind(new InetSocketAddress(config.port), 1);
        } catch (Exception e) {
            throw new RuntimeException("failed to create and bind server socket");
        }

        while (!stop) {
            while (remoteConnected.get()) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (Exception e) {}
                }
            }
            SSLSocket tlsSocket;
            try {
                tlsSocket = (SSLSocket) tlsServerSocket.accept();
                tlsSocket.setSoTimeout(5000);
            } catch (IOException e) {
                System.err.println("error accepting new connection: " + e.getMessage());
                continue;
            }

            try {
                tlsSocket.startHandshake();
            } catch (IOException e) {
                System.err.println("tls handshake failed: " + e.getMessage());
                continue;
            }
            OutputStream out;
            InputStream in;
            try {
                out = tlsSocket.getOutputStream();
                in = tlsSocket.getInputStream();
                out.write("im_a_screen_share_remote_server_i_promise".getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.err.println("socket error: " + e.getMessage());
                continue;
            }

            try {
                int passwordSize = byteArrayToInt(readAll(4, in));
                byte[] passwordBytes = readAll(passwordSize, in);
                boolean passwordIsCorrect = PasswordHashFactory.getHasher().validatePassword(
                        config.passwordHash,
                        StandardCharsets.UTF_8.decode(ByteBuffer.wrap(passwordBytes)).array()
                );
                Arrays.fill(passwordBytes, (byte)0);
                if (passwordIsCorrect) {
                    out.write(1);
                    System.out.println("correct password by: " + tlsSocket.getInetAddress());
                } else {
                    out.write(0);
                    System.out.println("wrong password attempt from: " + tlsSocket.getInetAddress());
                    continue;
                }
            } catch (Exception e) {
                System.err.println("error verifying password: " + e.getMessage());
                continue;
            }

            try {
                tlsSocket.setSoTimeout(0);
            } catch (IOException e) {
                continue;
            }
            remoteConnected.set(true);
            remoteConnection = new RemoteConnection(in, out, this);
            remoteConnection.start(config.webPort);
        }
    }

    @Override
    public void stop() {
        stop = true;
        try {
            remoteConnection.cancel();
        } catch (Exception e) {}
        try {
            tlsServerSocket.close();
        } catch (Exception e) {}
    }

    @Override
    public void disconnected() {
        remoteConnected.set(false);
        synchronized (this) {
            notify();
        }
    }

}
