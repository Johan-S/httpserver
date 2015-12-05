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
import java.security.SecureRandom;
import util.Security;
import util.Strings;
/**
 *
 * @author Johan Strååt
 */
public class Session {
  public final String sessionId;
  public final long accountId;
  public Session(long personId) {
    this.accountId = personId;
    this.sessionId = create(personId);
  }
  
  static private String create(long personId) {
    String sessionId;
    SecureRandom r = Security.getRandom();
    int tries = 3;
    do {
      sessionId = Strings.toHex(r.generateSeed(15));
      try {
        String sql = String.format("insert into session (id, account_id) values ('%s', %d);", sessionId, personId);        System.out.println(sql);
        ConnectionPool.execute(sql);
        return sessionId;
      } catch (SQLError ex) {
        if (tries-- == 0)
          throw ex;
      }
    } while(true);
  }
  
  
}
