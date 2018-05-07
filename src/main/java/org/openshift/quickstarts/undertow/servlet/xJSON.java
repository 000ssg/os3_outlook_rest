/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openshift.quickstarts.undertow.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 * @author sesidoro
 */
public class xJSON {

    public static String readText(int EOT, PushbackReader rdr) throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch = 0;
        while ((ch = rdr.read()) != EOT) {
            switch (ch) {
                case -1:
                    break;
                case '\\':
                    ch = rdr.read();
                    switch (ch) {
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        default:
                            sb.append((char) ch);
                            break;
                    }
                    break;
                default:
                    sb.append((char) ch);
            }
            if (ch == -1) {
                break;
            }
        }
        return sb.toString();
    }

    public static Object readLiteral(PushbackReader rdr) throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch = 0;
        boolean done = false;
        while ((ch = rdr.read()) != -1) {
            switch (ch) {
                case -1:
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                case '\b':
                case '\f':
                    skipWS(rdr);
                    done = true;
                    break;
                case ',':
                case '}':
                case ']':
                    rdr.unread(ch);
                    done = true;
                    break;
                default:
                    sb.append((char) ch);
            }
            if (ch == -1 || done) {
                break;
            }
        }
        String s = sb.toString();
        if ("true".equals(s)) {
            return Boolean.TRUE;
        } else if ("false".equals(s)) {
            return Boolean.FALSE;
        } else if ("null".equals(s)) {
            return null;
        } else if (s.contains(".") || s.toLowerCase().contains("e")) {
            return Double.parseDouble(s);
        } else {
            return Long.parseLong(s);
        }
    }

    public static void skipWS(PushbackReader rdr) throws IOException {
        int ch = 0;
        while ((ch = rdr.read()) != -1) {
            switch (ch) {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                case '\b':
                case '\f':
                    break;
                default:
                    rdr.unread(ch);
                    return;
            }
        }
    }

    public static Map readMap(PushbackReader rdr) throws IOException {
        Map m = new LinkedHashMap();

        int ch = 0;
        String s = null;
        String key = null;
        boolean expectSeparator = false;
        skipWS(rdr);
        while ((ch = rdr.read()) != '}') {
            switch (ch) {
                case -1:
                    throw new IOException("Unexpected EOF: object is not closed with '}'.");
                case ':':
                    if (key == null) {
                        throw new IOException("Unexpected name-value separator: " + (char) ch);
                    }
                    break;
                case ',':
                    if (key != null) {
                        throw new IOException("Unexpected values separator: " + (char) ch + ", undefined name is '" + key + "'.");
                    }
                    expectSeparator = false;
                    break;
                case '"':
                case '\'':
                    s = readText(ch, rdr);
                    if (key == null) {
                        key = s;
                    } else {
                        m.put(key, s);
                        key = null;
                        expectSeparator = true;
                    }
                    break;
                default:
                    rdr.unread(ch);
                     {
                        Object o = read(rdr);
                        if (key != null) {
                            m.put(key, o);
                            key = null;
                            expectSeparator = true;
                        } else {
                            throw new IOException("Missing name, got " + o);
                        }
                    }
                    break;
            }
            if (ch == -1) {
                break;
            }
            skipWS(rdr);
        }

        return m;
    }

    public static List readList(PushbackReader rdr) throws IOException {
        List l = new ArrayList();

        int ch = 0;
        boolean expectSeparator = false;
        skipWS(rdr);
        while ((ch = rdr.read()) != ']') {
            switch (ch) {
                case -1:
                    throw new IOException("Unexpected EOF: collection is not closed with ']'.");
                case ',':
                    if (expectSeparator) {
                        expectSeparator = false;
                    } else {
                        throw new IOException();
                    }
                    break;
                default:
                    rdr.unread(ch);
                    l.add(read(rdr));
                    expectSeparator = true;
            }
            skipWS(rdr);
        }

        return l;
    }

    public static <T> T read(PushbackReader rdr) throws IOException {
        Object r = null;
        int ch = 0;
        skipWS(rdr);
        while ((ch = rdr.read()) != -1) {
            switch (ch) {
                case -1:
                    throw new IOException("Unexpected EOF");
                case '"':
                case '\'':
                    r = readText(ch, rdr);
                    break;
                case '{':
                    r = readMap(rdr);
                    break;
                case '[':
                    r = readList(rdr);
                    break;
                default:
                    rdr.unread(ch);
                    r = readLiteral(rdr);
                    break;
            }
            skipWS(rdr);
            break;
        }
        return (T) r;
    }

    public static <T> T read(String text) throws IOException {
        return read(new PushbackReader(new StringReader(text)));
    }

    public static <T> T read(InputStream is, String encoding) throws IOException {
        return read(new PushbackReader(new InputStreamReader(is, encoding)));
    }
}
