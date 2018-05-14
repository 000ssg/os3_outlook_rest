/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openshift.quickstarts.undertow.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author sesidoro
 */
public class xJSON {

    public static String readText(int EOT, PushbackReader rdr) throws IOException {
        StringBuilder sb = new StringBuilder(1024);
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
                        case 'u':
                            // unicode
                            char[] uch = new char[4];
                            rdr.read(uch);
                            sb.append((char) Integer.parseInt(new String(uch), 16));
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
        StringBuilder sb = new StringBuilder(1024);
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
                case '\t':
                case '\n':
                case '\r':
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
        Map m = new LinkedHashMap(20);

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
                    if (key != null || !expectSeparator) {
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
        List l = new ArrayList(20);

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
                        throw new IOException("Unexpected vzlues separator in list.");
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

    public static <T> T read(Reader rdr) throws IOException {
        return read((rdr instanceof PushbackReader) ? (PushbackReader) rdr : new PushbackReader(rdr, 5));
    }

    public static String write(Object obj) throws IOException {
        StringWriter wr = new StringWriter();
        write(obj, wr);
        return wr.toString();
    }

    public static void write(Object obj, OutputStream os) throws IOException {
        Writer wr = new OutputStreamWriter(os, "UTF-8");
        write(obj, wr);
        wr.close();
    }

    public static void write(Object obj, Writer writer) throws IOException {
        if (obj == null) {
            writer.write("null");
        } else if (obj instanceof Boolean) {
            writer.write("" + obj);
        } else if (obj instanceof Number) {
            writer.write("" + obj);
        } else if (obj instanceof String) {
            writer.write('"');
            for(char ch:((String)obj).toCharArray()) {
                switch(ch){
                    case '\\':
                    case '/':
                    case '"':
                    case '\t':
                    case '\n':
                    case '\r':
                    case '\f':
                    case '\b':
                        writer.write('\\');
                        writer.write(ch);
                        break;
                    default:
                        writer.write(ch);
                }
            }
            writer.write('"');
        } else if (obj instanceof byte[]) {
            writer.write('"' + Base64.getEncoder().encodeToString((byte[]) obj) + '"');
        } else if (obj instanceof Collection) {
            writer.write('[');
            boolean first = true;
            for (Object item : (Collection) obj) {
                if (first) {
                    first = false;
                } else {
                    writer.write(",");
                }
                write(item, writer);
            }
            writer.write(']');
        } else if (obj.getClass().isArray()) {
            writer.write('[');
            for (int i = 0; i < Array.getLength(obj); i++) {
                if (i > 0) {
                    writer.write(',');
                }
                write(Array.get(obj, i), writer);
            }
            writer.write(']');
        } else if (obj instanceof Map) {
            writer.write('{');
            boolean first = true;
            for (Entry<Object, Object> e : ((Map<Object, Object>) obj).entrySet()) {
                if (first) {
                    first = false;
                } else {
                    writer.write(',');
                }
                write(e.getKey(), writer);
                writer.write(':');
                write(e.getValue(), writer);
            }
            writer.write('}');
        } else {
            Refl ref = refl.get();
            if (ref != null) {
                writer.write('{');
                boolean first = true;
                Collection<String> ns = ref.names(obj);
                if (ns != null) {
                    for (String n : ns) {
                        if (first) {
                            first = false;
                        } else {
                            writer.write(',');
                        }
                        write(n, writer);
                        writer.write(':');
                        write(ref.value(obj, n), writer);
                    }
                }
                writer.write('}');
            } else {
                throw new IOException("Non-primitive value: '" + obj.getClass().getName() + "' need adapter.");
            }
        }
    }

    static ThreadLocal<Refl> refl = new ThreadLocal<Refl>() {
        @Override
        protected Refl initialValue() {
            return new ReflImpl();
        }
    };

    public static interface Refl {

        void clear();

        Collection<String> names(Object obj);

        <T> T value(Object obj, String name);
    }

    public static class ReflImpl implements Refl {

        Map<Class, Map<String, Object[]>> cache = new LinkedHashMap<Class, Map<String, Object[]>>();

        @Override
        public void clear() {
            cache.clear();
        }

        @Override
        public Collection<String> names(Object obj) {
            if (obj == null) {
                return Collections.emptyList();
            }
            Class cl = obj.getClass();
            Map<String, Object[]> ns = cache.get(cl);
            if (ns == null) {
                init(cl);
                ns = cache.get(cl);
            }
            return ns.keySet();
        }

        @Override
        public <T> T value(Object obj, String name) {
            if (obj == null) {
                return null;
            }
            Class cl = obj.getClass();
            Map<String, Object[]> ns = cache.get(cl);
            if (ns == null) {
                init(cl);
                ns = cache.get(cl);
            }
            Object[] acc = ns.get(name);
            if (acc[0] instanceof Field) {
                try {
                    return (T) ((Field) acc[0]).get(obj);
                } catch (Throwable th) {
                }
            } else if (acc[0] instanceof Method) {
                try {
                    return (T) ((Method) acc[0]).invoke(obj);
                } catch (Throwable th) {
                }
            }
            return null;
        }

        void init(Class cl) {
            if (cl == null) {
                return;
            }
            Map<String, Object[]> accs = new HashMap<String, Object[]>();
            cache.put(cl, accs);
            try {
                for (Field f : cl.getFields()) {
                    if (Modifier.isStatic(f.getModifiers()) || !Modifier.isPublic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) {
                        continue;
                    }
                    accs.put(f.getName(), new Object[]{f, f});
                }
                // gets..
                for (Method f : cl.getMethods()) {
                    if (Modifier.isStatic(f.getModifiers()) || !Modifier.isPublic(f.getModifiers())) {
                        continue;
                    }
                    if (f.getParameterCount() > 0) {
                        continue;
                    }
                    String n = f.getName();
                    if (n.startsWith("is") || n.startsWith("get")) {
                        if (f.getName().equals("getClass")) {
                            continue;
                        }
                        if (n.startsWith("is")) {
                            n = n.substring(2);
                        } else {
                            n = n.substring(3);
                        }
                        n = n.substring(0, 1).toLowerCase() + n.substring(1);
                        if (!accs.containsKey(n)) {
                            accs.put(n, new Object[]{f, null});
                        }
                    }
                }
                // sets..
                for (Method f : cl.getMethods()) {
                    if (Modifier.isStatic(f.getModifiers()) || !Modifier.isPublic(f.getModifiers())) {
                        continue;
                    }
                    if (f.getParameterCount() != 1 || f.getReturnType() == null) {
                        continue;
                    }
                    String n = f.getName();
                    if (n.startsWith("set")) {
                        n = n.substring(3);
                        n = n.substring(0, 1).toLowerCase() + n.substring(1);
                        Object[] acc = accs.get(n);
                        if (acc == null) {
                            accs.put(n, new Object[]{null, f});
                        } else {
                            if (acc[1] == null) {
                                acc[1] = f;
                            }
                        }
                    }
                }
            } catch (Throwable th) {
            }
        }
    }
}
