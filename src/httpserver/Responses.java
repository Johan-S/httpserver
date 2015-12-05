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

import httpserver.Http.Status;
import java.io.*;
import java.nio.file.Files;
import java.sql.ResultSetMetaData;
import java.time.Instant;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import java.util.*;
import java.util.logging.Logger;
import util.Strings;

/**
 *
 * @author Johan Strååt
 */
public class Responses {

  private static final Logger log = Logger.getLogger(Responses.class.getName());
  
  public static final Response notFound() {
      return new Response(Status.NOT_FOUND);
  }
  public static final Response notModified() {
      return new Response(Status.NOT_MODIFIED);
  }
  public static final Response serverError() {
      return new Response(Status.SERVER_ERROR);
  }
  
  /**
   * Responds with the content of a file.
   * @param input the file to send
   * @param mimetype the type of the file
   * @param cacheDate the date of the cached version
   * @return A response object which will send the data
   */
  public static Response respond(File input, String mimetype, Date cacheDate) {
    if (!input.isFile()) {
      return notFound();
    }
    try {
      long l = input.lastModified();
      if (cacheDate.getTime() == Instant.ofEpochMilli(l).getLong(INSTANT_SECONDS)*1000)
        return notModified();
      Response res = new FileResponse(input, mimetype);
      return res;
    } catch (IOException e) {
      return serverError();
    }
  }
  
  /**
   * Responds with the content of a file, the type will be deduced from its
   * filename.
   * @param input the file to send
   * @param cacheDate the date of the cached version
   * @return A response object which will send the data
   */
  public static Response respond(File input, Date cacheDate) {
    if (!input.isFile()) {
      return notFound();
    }
    try {
      long l = input.lastModified();
      if (cacheDate.getTime() == Instant.ofEpochMilli(l).getLong(INSTANT_SECONDS)*1000)
        return notModified();
      Response res = new FileResponse(input);
      return res;
    } catch (IOException e) {
      return serverError();
    }
  }
  
  
  /**
   * Responds with the content of a file, the type will be deduced from its
   * filename.
   * @param input the file to send
   * @return A response object which will send the data
   */
  public static Response respond(File input) {
    if (!input.isFile()) {
      return notFound();
    }
    try {
      Response res = new FileResponse(input);
      return res;
    } catch (IOException e) {
      return serverError();
    }
  }

  /**
   * Responds with a plain string.
   * @param input Will be converted to a string using its toString method.
   * @return A response object which will send the data
   */
  public static Response respond(Object input) {
    return new RawResponse(input.toString(), "text/plain");
  }

  /**
   * Responds with a permanent redirect directive.
   * @param url the url the client should be redirected to
   * @return A response object which will send the data
   */
  public static Response redirect(String url) {
    return new Response(Status.MOVED_PERMANENTLY, "Location: " + url);
  }

  /**
   * Responds with a temporary redirect directive.
   * @param url the url the client should be redirected to
   * @return A response object which will send the data
   */
  public static Response redirect_temporarily(String url) {
    return new Response(Status.MOVED_TEMPORARILY, "Location: " + url);
  }

  /**
   * Responds with a json string.
   * @param json An object which is either a json String or whose toString
   * method returns a json String.
   * @return A response object which will send the data
   */
  public static Response json(Object json) {
    return new RawResponse(json.toString(), "application/json");
  }

  /**
   * Responds by just setting a cookie.
   * @param name The name of the cookie
   * @param value The value of the cookie
   * @param durationSeconds The number of seconds before the cookie becomes invalid
   * @return A response object which will send the data
   */
  public static Response cookie(String name, String value, int durationSeconds) {
    Response r = new Response();
    Calendar end = Calendar.getInstance();
    end.add(Calendar.SECOND, durationSeconds);
    r.headers.add("Set-Cookie: " + name + '=' + value + "; Path=/; Expires=" + Strings.fromDate(end.getTime()));
    return r;
  }

  /**
   * Responds with the content of a sql query
   * @param query the query whose results we want to send
   * @return A response object which will send the data
   */
  public static Response directSQL(String query) {
    List<Map<String, Object>> m = ConnectionPool.query(query, r -> {
      List<Map<String, Object>> res = new ArrayList<>();
      ResultSetMetaData meta = r.getMetaData();
      int n = meta.getColumnCount();
      String[] names = new String[n];
      for (int i = 0; i < n; ++i) {
        names[i] = meta.getColumnName(i + 1);
      }
      while (r.next()) {
        Map<String, Object> pr = new HashMap<>();
        res.add(pr);
        for (int i = 0; i < n; ++i) {
          pr.put(names[i], r.getObject(i + 1));
        }
      }
      return res;
    });
    JsonBuilder jb = new JsonBuilder();
    jb.append(m);
    return json(jb.toString());
  }

  /**
   * Responds with the content of a sql query which returns a single value
   * @param query the query whose results we want to send
   * @return A response object which will send the data
   */
  public static Response directUniqueSQL(String query) {
    Map<String, Object> m = ConnectionPool.query(query, r -> {
      Map<String, Object> res = new HashMap<>();
      ResultSetMetaData meta = r.getMetaData();
      int n = meta.getColumnCount();
      while (r.next()) {
        for (int i = 1; i <= n; ++i) {
          res.put(meta.getColumnLabel(i), r.getObject(i));
        }
      }
      return res;
    });
    JsonBuilder jb = new JsonBuilder();
    jb.append(m);
    return json(jb.toString());

  }

  static class FileResponse extends Response {

    File f;

    FileResponse(File f) throws IOException {
      this.f = f;
      MimeType type = MimeType.fromFileName(f.getName());
      if (type != null) {
        headers.add("Content-Type: " + type.name);
      }
      headers.add("Content-Length: " + Files.size(f.toPath()));
      headers.add("Last-Modified: " + Strings.fromDate(new Date(f.lastModified())));
      headers.add("Cache-Control: Public");
    }
    FileResponse(File f, String type) throws IOException {
      this.f = f;
      if (type != null) {
        headers.add("Content-Type: " + type);
      }
      headers.add("Content-Length: " + Files.size(f.toPath()));
      headers.add("Last-Modified: " + Strings.fromDate(new Date(f.lastModified())));
      headers.add("Cache-Control: Public");
    }

    @Override
    void send(OutputStream out) throws IOException {
      super.send(out);
      try (InputStream in = new FileInputStream(f)) {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) > 0) {
          out.write(buffer, 0, read);
        }
      }
    }

    static boolean checkFilePath(String path) {
      if (path == null || path.length() == 0 || path.charAt(0) != '/') {
        return false;
      }
      for (int i = 1; i < path.length(); ++i) {
        if (path.charAt(i) == '.' && path.charAt(i + 1) == '.') {
          return false;
        }
      }
      return true;
    }
  }

  static class RawResponse extends Response {

    byte[] data;

    RawResponse(byte[] data, String type) {
      this.data = data;
      headers.add("Content-Type: " + type);
      headers.add("Content-Length: " + data.length);
    }

    RawResponse(String data, String type) {
      this(data.getBytes(), type);
    }

    @Override
    void send(OutputStream out) throws IOException {
      super.send(out);
      out.write(data);
    }
  }

  public static File safeFile(String base, String path) {
    if (path == null || path.length() == 0 || path.charAt(0) != '/') {
      return null;
    }
    for (int i = 1; i < path.length(); ++i) {
      if (path.charAt(i) == '.' && path.charAt(i + 1) == '.') {
        return null;
      }
    }
    return new File(base + path);
  }
}
