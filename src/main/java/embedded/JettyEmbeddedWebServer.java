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
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

class JettyEmbeddedWebServer implements EmbeddedWebServer {

    private Server webServer;
    private FrameQueueFiller queueFiller;

    JettyEmbeddedWebServer(FrameQueueFiller queueFiller) {
        this.queueFiller = queueFiller;
    }

    @Override
    public boolean start(int port, String pageTitle) {
        byte[] indexHtmlCustomTitleBytes;
        try (InputStream indexHtml = getClass().getResourceAsStream("/web/dynamic/index.html")) {
            List<String> lines = new BufferedReader(new InputStreamReader(
                    indexHtml,
                    StandardCharsets.UTF_8)).lines().collect(Collectors.toList()
            );
            StringBuilder indexHtmlBuilder = new StringBuilder();
            lines.set(4, "<title>"
                    + pageTitle.replace("<", "&lt;").replace(">", "&gt;")
                    + "</title>");
            lines.forEach(line -> indexHtmlBuilder.append(String.format("%s%n", line)));
            indexHtmlCustomTitleBytes = indexHtmlBuilder.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("error loading index.html: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return startWebServer(port, indexHtmlCustomTitleBytes);
    }

    @Override
    public void stop() {
        WebSocketHandler.cancelAllConnections();
        try {
            webServer.stop();
        } catch (Exception e) {}
    }

    private boolean startWebServer(int port, byte[] indexHtmlCustomTitleBytes) {
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

        ResourceHandler cssResourceHandler = new ResourceHandler();
        cssResourceHandler.setDirectoriesListed(false);
        cssResourceHandler.setResourceBase(getClass().getClassLoader().getResource("web/static/css").toExternalForm());
        ContextHandler cssStaticFilesContext = new ContextHandler();
        cssStaticFilesContext.setContextPath("/css/*");
        cssStaticFilesContext.setHandler(cssResourceHandler);

        ResourceHandler jsResourceHandler = new ResourceHandler();
        jsResourceHandler.setDirectoriesListed(false);
        jsResourceHandler.setResourceBase(getClass().getClassLoader().getResource("web/static/js").toExternalForm());
        ContextHandler jsStaticFilesContext = new ContextHandler();
        jsStaticFilesContext.setContextPath("/js/*");
        jsStaticFilesContext.setHandler(jsResourceHandler);

        ServletContextHandler indexServletContext = new ServletContextHandler();
        indexServletContext.addServlet(new ServletHolder(new IndexHtmlServlet(indexHtmlCustomTitleBytes)), "/*");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{cssStaticFilesContext, jsStaticFilesContext, webSocketContext, indexServletContext});
        webServer.setHandler(handlers);

        try {
            webServer.start();
            return true;
        } catch (Exception e) {
            System.err.println("could not start embedded webserver: " + e.getMessage());
            return false;
        }
    }

    private class IndexHtmlServlet extends HttpServlet {
        private byte[] indexHtmlCustomTitleBytes;

        private IndexHtmlServlet(byte[] indexHtmlCustomTitleBytes) {
            this.indexHtmlCustomTitleBytes = indexHtmlCustomTitleBytes;
        }

        @Override
        protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
            String requestURI = request.getRequestURI();
            if (requestURI.equals("/") || requestURI.equals("/index.html")) {
                response.setContentType("text/html;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                OutputStream out = response.getOutputStream();
                out.write(indexHtmlCustomTitleBytes);
                out.close();
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

}
