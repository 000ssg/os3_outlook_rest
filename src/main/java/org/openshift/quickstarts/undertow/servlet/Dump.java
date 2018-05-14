/*
 * AS IS
 */
package org.openshift.quickstarts.undertow.servlet;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author 000ssg
 */
public class Dump {

    public static String dump(Object obj, boolean indented, boolean counted) {
        if (obj instanceof Map) {
            return dumpMap(obj, indented, counted);
        } else if (obj instanceof byte[]) {
            StringBuilder sb = new StringBuilder();
            byte[] bb = (byte[]) obj;
            if (counted) {
                sb.append("(" + bb.length + ") 0x");
            } else {
                sb.append("0x");
            }
            for (int i = 0; i < bb.length; i++) {
                if (i > 0) {
                    sb.append("");
                }
                sb.append(Integer.toHexString(0xFF & bb[i]));
            }
            sb.append("");
            return sb.toString();
        } else if (obj instanceof Collection || obj != null && obj.getClass().isArray()) {
            return dumpArray(obj, indented, counted);
        } else {
            return "" + obj;
        }
    }

    public static String dumpMap(Object obj, boolean indented, boolean counted) {
        try {
            StringBuilder sb = new StringBuilder();
            if (obj instanceof Map) {
                Map map = (Map) obj;
                sb.append("{");
                if (!map.isEmpty()) {
                    for (Object key : map.keySet()) {
                        sb.append("\n  " + key + ": ");
                        Object val = map.get(key);
                        if (val == null) {
                            sb.append("null");
                        } else if (val instanceof Collection || val.getClass().isArray()) {
                            sb.append(dump(val, indented & !(val instanceof byte[]), counted).replace("\n", "\n  "));
                        } else {
                            sb.append(val.toString().replace("\n", "\n  "));
                        }
                    }
                }
                sb.append("}");
            }
            return sb.toString();
        } finally {
        }
    }

    public static String dumpArray(Object obj, boolean indented, boolean counted) {
        try {
            StringBuilder sb = new StringBuilder();
            if (counted) {
                if (obj != null && obj.getClass().isArray()) {
                    sb.append("(" + Array.getLength(obj) + ") ");
                } else if (obj instanceof Collection) {
                    sb.append("(" + ((Collection) obj).size() + ") ");
                }
            }
            sb.append("[");
            if (obj != null && obj.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(obj); i++) {
                    if (i > 0) {
                        if (indented) {
                            sb.append(",\n  ");
                        } else {
                            sb.append(", ");
                        }
                    } else if (indented) {
                        sb.append("\n  ");
                    }
                    Object val = Array.get(obj, i);
                    sb.append(dump(val, indented & !(val instanceof byte[]), counted).replace("\n", "\n  "));
                }
            } else if (obj instanceof Collection) {
                boolean first = true;
                for (Object val : (Collection) obj) {
                    if (first) {
                        first = false;
                        if (indented) {
                            sb.append("\n  ");
                        }
                    } else if (indented) {
                        sb.append(",\n  ");
                    } else {
                        sb.append(", ");
                    }
                    sb.append(dump(val, indented & !(val instanceof byte[]), counted).replace("\n", "\n  "));
                }
            }
            if (indented) {
                sb.append("\n]");
            } else {
                sb.append("]");
            }
            return sb.toString();
        } finally {
        }
    }

    public static class DumpInputStream extends FilterInputStream {

        long pos = 0;

        public DumpInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int r = super.read();
            if (r != -1) {
                pos++;
            }
            System.out.print("[" + pos + "]\t" + r + "\t");
            switch (r) {
                case ' ':
                    System.out.println("<SPACE>");
                    break;
                case '\n':
                    System.out.println("<LF>");
                    break;
                case '\r':
                    System.out.println("<CR>");
                    break;
                case '\t':
                    System.out.println("<TAB>");
                    break;
                case '\f':
                    System.out.println("<FORMFEED>");
                    break;
                case '\b':
                    System.out.println("<BACKSPACE>");
                    break;
                default:
                    System.out.println("'" + (char) r + "'");
                    break;
            }
            return r;
        }
    }

    public static String findAllManifests() {
        StringBuilder sb = new StringBuilder();
        Set<URL> processed = new HashSet<URL>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        for (String manifestFile : new String[]{"META-INF/MANIFEST.MF", "META-INF\\MANIFEST.MF"}) {
            try {
                Enumeration<URL> urlsEnum = Dump.class.getClassLoader().getResources(manifestFile);
                if (urlsEnum != null) {
                    while (urlsEnum.hasMoreElements()) {
                        URL url = urlsEnum.nextElement();
                        if (processed.contains(url)) {
                            continue;
                        }
                        processed.add(url);
                        sb.append("[" + url + "]\n");
                        baos.reset();
                        try {
                            InputStream is = null;
                            try {
                                is = url.openStream();
                                int c = 0;
                                while ((c = is.read(buf)) != -1) {
                                    baos.write(buf, 0, c);
                                }
                                sb.append("\t" + baos.toString().replace("\n", "\n\t"));
                            } finally {
                                try {
                                    is.close();
                                } catch (Throwable th) {
                                }
                            }
                        } catch (Throwable th) {
                            sb.append("\tERROR:\t" + th + "\n");
                        }
                    }
                }
            } catch (Throwable th) {
                sb.append("Failed to get manfests: " + th);
            }
        }
        try {
            List<String> all = new ArrayList<String>(processed.size());
            for (URL url : processed) {
                all.add(url.toString());
            }
            Collections.sort(all);
            sb.append("\n\n[all manifest URLs]\n");
            for (String s : all) {
                sb.append("\t" + s + "\n");
            }
        } catch (Throwable th) {
        }
        return sb.toString();
    }

}
