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

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import static httpserver.Http.Method;
import java.util.regex.Matcher;


public interface RequestHandler {

  public Response handle(Request req) throws SocketException;

  public static abstract class Resource implements RequestHandler {
    @Override
    public Response handle(Request req) throws SocketException {
      switch(req.method) {
        case GET:
          return Get(req);
        case POST:
          return Post(req);
        case PUT:
          return Put(req);
        case DELETE:
          return Delete(req);
        default:
          return Responses.notFound();
      }
    }
    public abstract Response Get(Request req);
    public abstract Response Post(Request req);
    public abstract Response Put(Request req);
    public abstract Response Delete(Request req);
    
  }
  
  public static class Aggregate implements RequestHandler {

    @Override
    public Response handle(Request req) throws SocketException {
      for (int i = 0; i < patterns.length; ++i) {
        if (req.method == methods[i]) {
          Matcher m = patterns[i].matcher(req.path);
          if (m.matches()) {
            int e = m.groupCount();
            for (int j = 1; j <= e; ++j)
              req.params.add(m.group(j));
            return handlers[i].handle(req);
          }
        }
      }
      return Responses.notFound();
    }
    private RequestHandler[] handlers;
    private Pattern[] patterns;
    private Method[] methods;

    private Aggregate() {

    }

    public static class Builder {

      private final List<RequestHandler> handlers = new ArrayList<>();
      private final List<Method> methods = new ArrayList<>();
      private final List<Pattern> patterns = new ArrayList<>();

      public void add(Method method, String regex, RequestHandler handler) {
        handlers.add(handler);
        methods.add(method);
        patterns.add(Pattern.compile(regex));
      }

      public Aggregate build() {
        Aggregate res = new Aggregate();
        res.handlers = handlers.toArray(new RequestHandler[handlers.size()]);
        res.methods = methods.toArray(new Method[methods.size()]);
        res.patterns = patterns.toArray(new Pattern[patterns.size()]);
        return res;
      }
    }
  }
}
