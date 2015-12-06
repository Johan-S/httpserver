
package httpserver.sql;

import httpserver.JsonBuilder;
import httpserver.Response;
import httpserver.Responses;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLResponses {

  /**
   * Responds with the content of a sql query which returns a single value
   * @param query the query whose results we want to send
   * @return A response object which will send the data
   */
  public static Response directUniqueSQL(String query) {
    Map<String, Object> m = ConnectionPool.query(query, (ResultSet r) -> {
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
    return Responses.json(jb.toString());
  }

  /**
   * Responds with the content of a sql query
   * @param query the query whose results we want to send
   * @return A response object which will send the data
   */
  public static Response directSQL(String query) {
    List<Map<String, Object>> m = ConnectionPool.query(query, (ResultSet r) -> {
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
    return Responses.json(jb.toString());
  }

}
