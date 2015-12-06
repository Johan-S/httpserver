/*
 * The MIT License
 *
 * Copyright 2015 Johan Strååt.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package httpserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import javax.net.ssl.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;
import static java.lang.String.format;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class Server {

  private static final Logger log = Logger.getLogger(Server.class.getName());

  private ServerSocket server;

  private ExecutorService pool;
  
  private Server(int port, boolean ssl) throws IOException {
    server = ssl ? (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port)
            : ServerSocketFactory.getDefault().createServerSocket(port);

  }

  private volatile boolean running = false;
  private ServerLoop loop;
  
  public void start(RequestHandler rh) {
    log.info(String.format("Starting server at '%s'.", server.getInetAddress().toString()));
    pool = Executors.newFixedThreadPool(1000, run -> new Thread(null, run, "", 1 << 12));
    loop = new ServerLoop(rh);
    running = true;
    loop.start();
  }

  public void stop() {
    try {
      if (running) {
        log.info(String.format("Stopping server at '%s'.", server.getInetAddress().toString()));
        pool.shutdown();
        pool = null;
        running = false;
        server.close();
      }
    } catch (IOException e) {
      log.warning(e.toString());
    }
  }
  static final int DEFAULT_PORT = 80;
  static final int DEFAULT_SECURE_PORT = 443;

  public static Server create(int port) throws IOException {
    return new Server(port, false);
  }

  public static Server createSecure(int port) throws IOException {
    return new Server(port, true);
  }

  public static Server create() throws IOException {
    return new Server(DEFAULT_PORT, false);
  }

  public static Server createSecure() throws IOException {
    return new Server(DEFAULT_SECURE_PORT, true);
  }

  private class ServerLoop extends Thread {

    RequestHandler rh;

    ServerLoop(RequestHandler rh) {
      this.rh = rh;
    }

    @Override
    public void run() {
      while (running) {
        try {
          pool.submit(new RequestThread(server.accept(), rh));
        } catch (IOException ex) {
          if (!ex.getMessage().equals("socket closed")) {
            log.severe(ex.toString());
          }
        }
      }
    }
  }

  private class RequestThread extends Thread {

    private final RequestHandler rh;
    private final Socket sock;

    RequestThread(Socket s, RequestHandler rh) {
      this.rh = rh;
      this.sock = s;
    }

    @Override
    public void run() {
      String requestString = "*****no request found.*****";
      InetAddress con = null;
      try (Socket s = sock) {
        con = s.getInetAddress();
        Request rq = new Request(s.getInputStream());
        requestString = rq.toString();
        Response resp = rh.handle(rq);
        resp.send(sock.getOutputStream());
        log.log(Level.FINE,format("[%s] served successfully. %s\nRequest:\n%s\n\n", con.getHostName(), rq.path, requestString));
      } catch (SocketException e) {
        if (con != null) {
          log.warning(format("[%s] %s!Request:\n%s\n\n", con.getHostAddress(), e.toString(), requestString));
        } else {
          log.warning(format("[no connection] %s!\nRequest:\n%s\n\n", e.toString(), requestString));
        }
      } catch (IOException e) {
        if (con != null) {
          log.severe(format("[%s] %s!\nRequest:\n%s\n\n", con.getHostAddress(), e.toString(), requestString));
        } else {
          log.severe(format("[no connection] %s!\nRequest:\n%s\n\n", e.toString(), requestString));
        }
      } catch (Exception | Error e) {
        log.log(Level.SEVERE,requestString ,e);
      }
    }
  }

}
