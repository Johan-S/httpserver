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

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public enum MimeType {
    JPG("image/jpg", "jpg"),
    PNG("image/png", "png"),
    ICO("image/x-icon", "ico"),
    CSS("text/css", "css"),
    JS("application/js", "js"),
    HTML("text/html", "html"),
    ;
    private static final Logger log = Logger.getLogger(MimeType.class.getName());
    
    public final String name, ext;
    MimeType(String name, String extension) {
        this.name = name;
        this.ext = extension;
    }
    
    public static MimeType fromFileName(String name) {
        int p = name.length();
        while (p > 0 && name.charAt(p-1) != '.')
            p--;
        MimeType res = index.get(name.substring(p));
        if (res == null) {
            log.log(Level.FINE, "Unrecognized filetype: {0}", name);
            return null;
        }
        return res;
    }
    
    private final static HashMap<String, MimeType> index = new HashMap<>();
    static {
        for (MimeType t : MimeType.values())
            index.put(t.ext, t);
    }
}
