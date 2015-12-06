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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class RequestTest {
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    String reqString = 
            "GET /mini.jpg HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Connection: keep-alive\r\n" +
            "Cache-Control: max-age=0\r\n" +
            "Accept: image/webp,image/*,*/*;q=0.8\r\n" +
            "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36\r\n" +
            "Referer: https://localhost/\r\n" +
            "Accept-Encoding: gzip, deflate, sdch\r\n" +
            "Accept-Language: en-US,en;q=0.8,sv;q=0.6\r\n" +
            "\r\n";
    @Test
    public void testParsing() throws IOException {
        Request req = new Request(new ByteArrayInputStream(reqString.getBytes()));
        assertEquals(req.path, "/mini.jpg");
        assertEquals(req.method, Http.Method.GET);
        assertEquals(req.protocol, "HTTP/1.1");
    }
    
    @Test(expected=IOException.class)
    public void testSize() throws IOException {
      
      Request req = new Request(new InputStream(){
        int n = 10000;
        @Override
        public int read() throws IOException {
          if (n == 0)
            throw new Error("should limit header size;");
          n--;
          return 0;
        }
      });
      exception.expect(IOException.class);
    }
    
}
