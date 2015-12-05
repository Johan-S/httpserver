package httpserver;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Johan Strååt
 */
public class JsonBuilder {
    
    public static String json(Object o) {
      JsonBuilder b = new JsonBuilder();
      b.append(o);
      return b.toString();
    }
    public JsonBuilder start() {
      append('{');
      return this;
    }
    public JsonBuilder end() {
      append('}');
      return this;
    }
    private JsonBuilder append(char val) {
        sb.append(val);
        return this;
    }
    
    public JsonBuilder append(Object val) {
        if (val instanceof String)
            sb.append('"').append(val).append('"');
        else if (val instanceof List)
            append((List)val);
        else if(val instanceof Map)
            append((Map)val);
        else
            sb.append(val);
        return this;
    }
    public JsonBuilder append(Object key, Object val) {
        append(key.toString());
        sb.append(':');
        append(val);
        return this;
    }
    public JsonBuilder append(List val) {
        sb.append('[');
        boolean added = false;
        for (Object o : val) {
            if (added)
                sb.append(',');
            append(o);
            added = true;
        }
        sb.append(']');
        return this;
    }
    public JsonBuilder append(Map<Object, Object> val) {
        sb.append('{');
        boolean added = false;
        for (Map.Entry o : val.entrySet()) {
            if (added)
                sb.append(',');
            append(o.getKey(), o.getValue());
            added = true;
        }
        sb.append('}');
        return this;
    }
    
    @Override
    public String toString() {
        return sb.toString();
    }
    
    private StringBuilder sb = new StringBuilder();
}
