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

import java.io.*;
import static httpserver.Http.*;
import java.util.*;
import util.Strings;


public class Response {

  public Response() {
    this(Status.SUCCESS);
  }

  public Response(Status status, String... headers) {
    this.status = status;
    this.headers = new ArrayList<>(Arrays.asList(headers));
  }

  void send(OutputStream out) throws IOException {
    out.write(String.format("%s %d %s\r\n", DEFAULT_VERSION, status.id, status.msg).getBytes());
    for (String s : headers) {
      out.write(s.getBytes());
      out.write(LN);
    }
    out.write(LN);
  }

  public final Response addHeader(String headerName, String headerValue) {
    headers.add(headerName + ": " + headerValue);
    return this;
  }

  public final Response setCookie(String key, String value, int durationSeconds) {
    Calendar end = Calendar.getInstance();
    end.add(Calendar.SECOND, durationSeconds);
    headers.add("Set-Cookie: " + key + '=' + value + "; Path=/; Expires=" + Strings.fromDate(end.getTime()));
    return this;
  }

  protected Status status;
  protected List<String> headers = new ArrayList<>();
  static final String DEFAULT_VERSION = "HTTP/1.1";
  static final byte[] LN = new byte[]{'\r', '\n'};
}
