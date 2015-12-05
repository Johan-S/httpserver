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

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import static httpserver.Http.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import util.HttpHeaders;
import util.io.CappedInputStream;
import util.io.Streams;

/**
 *
 * @author Johan Strååt
 */
public class Request {
  public final int MAX_HEADER_SIZE = 1 << 13;

  public final String path, protocol, host;
  public final Map<String, String> cookies = new HashMap<>(), headers;
  public final List<String> params = new ArrayList<>();
  public final Method method;
  public final byte[] data;

  private void parseCookies(String val) {
    int cur = 0;
    int mid = 0;
    for (int i = 0; i < val.length(); ++i) {
      switch (val.charAt(i)) {
        case '=': {
          mid = i;
        }
        break;
        case ';': {
          cookies.put(val.substring(cur, mid), val.substring(mid + 1, i));
          cur = i + 2;
          mid = cur;
        }
        break;
      }
    }
    if (cur != mid) {
      cookies.put(val.substring(cur, mid), val.substring(mid + 1));
    }
  }

  Request(InputStream req) throws IOException {
    InputStream s = new CappedInputStream(req, MAX_HEADER_SIZE);
    String protocolLine = Streams.readWindowsLine(s);
    String[] protocols = protocolLine.split(" ");
    if (protocols.length != 3) {
      throw new IOException(String.format("Invalid protocol header: \"%s\"", protocolLine));
    }
    try {
      method = Method.valueOf(protocols[0]);
    } catch (IllegalArgumentException e) {
      throw new IOException(String.format("Invalid method: %s.", protocols[0]));
    }
    path = protocols[1];
    protocol = protocols[2];
    headers = HttpHeaders.parseHeaders(s);
    host = headers.get("Host");
    
    String length = headers.get("Content-Length");
    data = new byte[length == null? 0: Integer.parseInt(length)];
    {
      int tot = 0;
      while (tot < data.length) {
        int c = req.read(data, tot, data.length - tot);
        if (c == -1)
          throw new IOException("Unexpected end of stream.");
        tot += c;
      }
        
    }
    
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(method.toString()).append(" ").append(protocol).append(" ").append(path).append("\r\n");
    for (Entry<String, String> e : headers.entrySet())
      sb.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
    sb.append("\r\n");
    if (data.length < 1000)
      sb.append(new String(data));
    return sb.toString();
  }
}
