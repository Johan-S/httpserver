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
package httpserver.sql;

import java.sql.*;


public abstract class ConnectionPool {
  
  public static void setUrl(String url) {
    ConnectionPool.url = url;
  }
  
  private static String url;
  private static String username;
  private static String password;
  private static Connection get() throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }
  public static void execute(String sql) {
      try(Connection conn = ConnectionPool.get();
              Statement stmt = conn.createStatement();) {
        stmt.execute(sql);
      } catch (SQLException e) {
        throw new SQLError(e);
      }
  }
  public static <T> T query(String sql, Callback<T> call) {
      try(Connection conn = ConnectionPool.get();
              Statement stmt = conn.createStatement();
              ResultSet res = stmt.executeQuery(sql);) {
        return call.call(res);
      } catch (SQLException e) {
        throw new SQLError(e);
      }
  }

    /**
     * @param aUsername the username to set
     */
    public static void setUsername(String aUsername) {
        username = aUsername;
    }

    /**
     * @param aPassword the password to set
     */
    public static void setPassword(String aPassword) {
        password = aPassword;
    }
  public static interface Callback<T> {
    public T call(ResultSet res) throws SQLException;
  }
}
