/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package embedded;

import h264.FrameQueueFiller;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

class JettyEmbeddedWebServer implements EmbeddedWebServer {

    private Server webServer;
    private FrameQueueFiller queueFiller;

    JettyEmbeddedWebServer(FrameQueueFiller queueFiller) {
        this.queueFiller = queueFiller;
    }

    @Override
    public boolean start(int port) {
        return startWebServer(port);
    }

    @Override
    public void stop() {
        WebSocketHandler.cancelAllConnections();
        try {
            webServer.stop();
        } catch (Exception e) {}
    }

    private boolean startWebServer(int port) {
        webServer = new Server();

        ServerConnector http = new ServerConnector(webServer);
        http.setPort(port);
        webServer.addConnector(http);

        org.eclipse.jetty.websocket.server.WebSocketHandler wsHandler = new org.eclipse.jetty.websocket.server.WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.getExtensionFactory().unregister("permessage-deflate");
                factory.setCreator((req, resp) -> new WebSocketHandler(queueFiller));
            }
        };
        ContextHandler webSocketContext = new ContextHandler();
        webSocketContext.setContextPath("/ws/");
        webSocketContext.setHandler(wsHandler);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setResourceBase(getClass().getClassLoader().getResource("public").toExternalForm());

        ContextHandler staticFilesContext = new ContextHandler();
        staticFilesContext.setContextPath("/");
        staticFilesContext.setHandler(resourceHandler);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{staticFilesContext, webSocketContext});
        webServer.setHandler(handlers);

        try {
            webServer.start();
            return true;
        } catch (Exception e) {
            System.err.println("could not start embedded webserver: " + e.getMessage());
            return false;
        }
    }

}
