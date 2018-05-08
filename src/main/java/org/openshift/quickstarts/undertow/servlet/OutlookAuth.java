package org.openshift.quickstarts.undertow.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import org.openshift.quickstarts.undertow.servlet.OutlookAuth.RoomLists.Room;
import org.openshift.quickstarts.undertow.servlet.OutlookAuth.RoomLists.TimeSlot;

public class OutlookAuth {

    public static enum TIME_PERIOD {
        today,
        week,
        forthnight,
        month
    }

    String authUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
    String tokenUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/token";

    String apiBase = "https://graph.microsoft.com/";
    String roomListsUrl = apiBase + "beta/me/findroomlists";
    String roomsListUrl = apiBase + "beta/me/findrooms";
    String messagesUrl = apiBase + "v1.0/me/messages?$select=subject,sender,receivedDateTime";

    String redirect = "https://openjdk-app-ddd.1d35.starter-us-east-1.openshiftapps.com/tokenized";
    String clientId;
    String clientSecret;
    String responseType = "code";
    String grantType = "authorization_code";
    String scope = "openid"
            + "+Calendars.Read"
            + "+Contacts.Read"
            + "+Device.Read"
            + "+Files.Read"
            + "+Mail.Read"
            + "+People.Read"
            + "+profile"
            + "+Tasks.Read"
            + "+User.Read";
    //
    String code;

    public static List<String> permissions = new ArrayList<String>() {
        {
            for (String s : new String[]{
                "Agreement.Read.All", // A
                "Agreement.ReadWrite.All", // A
                "AgreementAcceptance.Read", // A
                "AgreementAcceptance.Read.All", // A
                "Calendars.Read",
                "Calendars.Read.Shared",
                "Calendars.ReadWrite", // D
                "Calendars.ReadWrite.Shared",
                "Contacts.Read",
                "Contacts.Read.Shared",
                "Contacts.ReadWrite", // D
                "Contacts.ReadWrite.Shared",
                "Files.Read",
                "Files.Read.All",
                "Files.Read.Selected",
                "Files.ReadWrite",
                "Files.ReadWrite.All", // D
                "Files.ReadWrite.AppFolder",
                "Files.ReadWrite.Selected",
                "Mail.Read",
                "Mail.Read.Shared",
                "Mail.ReadWrite", // D
                "Mail.ReadWrite.Shared",
                "Mail.Send",
                "Mail.Sehd.Shared",
                "MailboxSettings.ReadWrite", // D
                "User.Read",
                "User.ReadWrite", // D
                "User.ReadBasic.All", // D
                "Notes.Create",
                "Notes.Read",
                "Notes.Read.All",
                "Notes.ReadWrite",
                "Notes.ReadWrite.All", // D
                "SecurityEvents.Read.All", // A
                "SecurityEvents.ReadWrite.All", // A
                "Sites.Read.All",
                "Sites.ReadWrite.All", // D
                "Sites.Manage.All",
                "Sites.FullControl.All",
                "Tasks.Read",
                "Tasks.Read.Shared",
                "Tasks.ReadWrite", // D
                "Tasks.ReadWrite.Shared",
                "Device.Read",
                "Device.Command",
                "Directory.AccessAsUser.All", // A,D
                "Directory.Read.All", // A
                "Directory.ReadWrite.All", // A,D
                "Group.Read.All", // A
                "Group.ReadWrite.All", // A,D
                "User.Read.All", // A
                "User.ReadWrite.All", // A, D
                "People.Read", // D
                "People.Read.All", // A
                "IdentityRiskEvent.Read.All", // A,D,P
                "DeviceManagementServiceConfig.Read.All", // A,D,P
                "DeviceManagementServiceConfig.ReadWrite.All", // A,D,P
                "DeviceManagementConfiguration.Read.All", // A,D,P
                "DeviceManagementConfiguration.ReadWrite.All", // A,D,P
                "DeviceManagementApps.Read.All", // A,D,P
                "DeviceManagementApps.ReadWrite.All", // A,D,P
                "DeviceManagementRBAC.Read.All", // A,D,P
                "DeviceManagementRBAC.ReadWrite.All", // A,D,P
                "DeviceManagementManagedDevices.Read.All", // A,D,P
                "DeviceManagementManagedDevices.ReadWrite.All", // A,D,P
                "DeviceManagementManagedDevices.PriviledgedOperations.All", // A,D,P
                "Reports.Read.All", // A,D,P
                "IdentityProvider.Read.All", // A,P
                "IdentityProvider.ReadWrite.All", // A,P
                "EduRoster.ReadBasic", // A,P
                "EduAssignments.ReadBasic", // A,P
                "EduAssignments.Read", // A,P
                "EduAssignments.ReadWriteBasic", // A,P
                "EduAssignments.ReadWrite", // A,P
                "EduAdministration.Read", // A,P
                "EduAdministration.ReadWrite", // A,P
                "Bookings.Read.All", // P
                "BookingsAppointment.ReadWrite.All", // P
                "Bookings.ReadWrite.All", // P
                "Bookings.Manage.All", // P
                "UserActivity.ReadWrite.CreatedByApp",
                "Financials.ReadWrite.All"
            }) {
                if (s != null && !s.trim().isEmpty()) {
                    add(s);
                }
            }
        }
    };

    public static Collection<String> adminPermissions = new HashSet<String>() {
        {
            for (String s : new String[]{
                "Agreement.Read.All", // A
                "Agreement.ReadWrite.All", // A
                "AgreementAcceptance.Read", // A
                "AgreementAcceptance.Read.All", // A
                "SecurityEvents.Read.All", // A
                "SecurityEvents.ReadWrite.All", // A
                "Directory.AccessAsUser.All", // A,D
                "Directory.Read.All", // A
                "Directory.ReadWrite.All", // A,D
                "Group.Read.All", // A
                "Group.ReadWrite.All", // A,D
                "User.Read.All", // A
                "User.ReadWrite.All", // A, D
                "People.Read.All", // A
                "IdentityRiskEvent.Read.All", // A,D,P
                "DeviceManagementServiceConfig.Read.All", // A,D,P
                "DeviceManagementServiceConfig.ReadWrite.All", // A,D,P
                "DeviceManagementConfiguration.Read.All", // A,D,P
                "DeviceManagementConfiguration.ReadWrite.All", // A,D,P
                "DeviceManagementApps.Read.All", // A,D,P
                "DeviceManagementApps.ReadWrite.All", // A,D,P
                "DeviceManagementRBAC.Read.All", // A,D,P
                "DeviceManagementRBAC.ReadWrite.All", // A,D,P
                "DeviceManagementManagedDevices.Read.All", // A,D,P
                "DeviceManagementManagedDevices.ReadWrite.All", // A,D,P
                "DeviceManagementManagedDevices.PriviledgedOperations.All", // A,D,P
                "Reports.Read.All", // A,D,P
                "IdentityProvider.Read.All", // A,P
                "IdentityProvider.ReadWrite.All", // A,P
                "EduRoster.ReadBasic", // A,P
                "EduAssignments.ReadBasic", // A,P
                "EduAssignments.Read", // A,P
                "EduAssignments.ReadWriteBasic", // A,P
                "EduAssignments.ReadWrite", // A,P
                "EduAdministration.Read", // A,P
                "EduAdministration.ReadWrite", // A,P
                "Bookings.Read.All", // P
                "BookingsAppointment.ReadWrite.All", // P
                "Bookings.ReadWrite.All", // P
                "Bookings.Manage.All" // P
            }) {
                if (s != null && !s.trim().isEmpty()) {
                    add(s);
                }
            }
        }
    };

    public Collection<String> selectedScope = new ArrayList<String>() {
        {
            for (String s : new String[]{
                "Calendars.Read.Shared", // D
                "Calendars.ReadWrite", // D
                "Contacts.ReadWrite", // D
                "Files.ReadWrite.All", // D
                "Mail.ReadWrite", // D
                "MailboxSettings.ReadWrite", // D
                "User.ReadWrite", // D
                "User.ReadBasic.All", // D
                "Notes.ReadWrite.All", // D
                "Sites.ReadWrite.All", // D
                "Tasks.ReadWrite", // D
                "Directory.AccessAsUser.All", // A,D
                "Directory.ReadWrite.All", // A,D
                "Group.ReadWrite.All", // A,D
                "User.ReadWrite.All", // A, D
                "People.Read", // D
                "IdentityRiskEvent.Read.All", // A,D,P
                "DeviceManagementServiceConfig.Read.All", // A,D,P
                "DeviceManagementServiceConfig.ReadWrite.All", // A,D,P
                "DeviceManagementConfiguration.Read.All", // A,D,P
                "DeviceManagementConfiguration.ReadWrite.All", // A,D,P
                "DeviceManagementApps.Read.All", // A,D,P
                "DeviceManagementApps.ReadWrite.All", // A,D,P
                "DeviceManagementRBAC.Read.All", // A,D,P
                "DeviceManagementRBAC.ReadWrite.All", // A,D,P
                "DeviceManagementManagedDevices.Read.All", // A,D,P
                "DeviceManagementManagedDevices.ReadWrite.All", // A,D,P
                "DeviceManagementManagedDevices.PriviledgedOperations.All", // A,D,P
                "Reports.Read.All", // A,D,P
                "Bookings.Read.All" // P
            }) {
                if (s != null && !s.trim().isEmpty()) {
                    add(s);
                }
            }
        }
    };

    public OutlookAuth(String clientId) {
        this.clientId = clientId;
        scope = "openid";
        for (String s : selectedScope) {
            if (!adminPermissions.contains(s)) {
                scope += "+" + s;
            }
        }
        System.out.println("OPS: " + scope);
    }

    public OutlookAuth(String clientId, String redirectUri, String responseType, String scope) {
        redirect = redirectUri;
        this.clientId = clientId;
        this.responseType = responseType;
        this.scope = scope;
    }

    public URL getAuthURL() throws IOException {
        URL url = new URL(
                authUrl
                + "?client_id=" + URLEncoder.encode(clientId, "UTF-8")
                + "&redirect_uri=" + URLEncoder.encode(redirect, "UTF-8")
                + "&response_type=" + responseType
                + "&scope=" + scope
        );
        return url;
    }

    public boolean doAuth() throws IOException {
        URL url = getAuthURL();

        System.out.println("doAuth: " + url);
        Map<String, Object> data = doGet(url);

        Map<String, List<String>> headers = (Map<String, List<String>>) data.get("headers");
        String content = "";
        if (data.get("content") instanceof byte[]) {
            content = new String((byte[]) data.get("content"), "ISO-8859-1");
        }
        if (headers != null && headers.get("Location") != null) {
            URL ucode = new URL(headers.get("Location").get(0));
            String q = ucode.getQuery();
            return true;
        } else {
            System.out.println(content);
        }

        return false;
    }

    public Map<String, Object> requestToken(String code) throws IOException {
        Map<String, Object> r = new LinkedHashMap<String, Object>();

        URL url = new URL(tokenUrl);
        StringBuilder body = new StringBuilder();
        body.append("grant_type=");
        body.append(grantType);
        body.append("&code=");
        body.append(code);
        body.append("&redirect_uri=");
        body.append(URLEncoder.encode(redirect, "UTF-8"));
        body.append("&client_id=");
        body.append(URLEncoder.encode(clientId, "UTF-8"));
        body.append("&client_secret=");
        body.append(URLEncoder.encode(clientSecret, "UTF-8"));

        r.put("RequestURL", url);
        try {
            int csIdx = body.indexOf(clientSecret);
            r.put("RequestBody[" + csIdx + "/" + clientSecret.length() + "]", body.substring(0, csIdx) + "<CLIENT_SECRET>" + body.substring(csIdx + clientSecret.length()));
        } catch (Throwable th) {
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();
            conn.getOutputStream().write(body.toString().getBytes());
            conn.getOutputStream().close();

            r.put("headers", conn.getHeaderFields());
            r.put("code", conn.getResponseCode());
            r.put("message", conn.getResponseMessage());
            if (conn.getContentType() != null) {
                String ct = conn.getContentType();
                String ce = conn.getContentEncoding();
                r.put("contentLength", conn.getContentLength());
                r.put("contentEncoding", ce);
                r.put("contentType", ct);

                Object obj = conn.getContent();
                if (obj instanceof InputStream) {
                    InputStream is = (InputStream) obj;
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int c = 0;
                    while ((c = is.read(buf)) != -1) {
                        os.write(buf, 0, c);
                    }
                    obj = os.toByteArray();
                    is.close();
                }

                if (ct != null && (ct.toLowerCase().contains("text") || ct.toLowerCase().contains("json")) && obj instanceof byte[]) {
                    obj = new String((byte[]) obj, (ce != null) ? ce : "ISO-8859-1");

                    Map map = xJSON.read((String) obj);

                    r.put("token_type", map.get("token_type"));
                    r.put("token", map.get("access_token"));
                }

                r.put("content", obj);
            }
        } catch (Throwable th) {
            StringWriter sw = new StringWriter();
            th.printStackTrace(new PrintWriter(sw));
            r.put("Exception", sw.getBuffer().toString());
        }

        return r;
    }

    public RoomLists roomsLists(String token) throws IOException {
        RoomLists roomLists = new RoomLists();
        URL url = new URL(roomListsUrl);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json, text/plain, */*");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("Accept-Language", "en-US;en;q=0.9");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        //conn.setRequestProperty("X-AnchorMailbox", "sergey.sidorov@digia.com");

        Object obj = null;
        try {
            conn.connect();

            obj = conn.getContent();
            if (obj instanceof InputStream) {
                if (obj instanceof InputStream) {
                    String ce = conn.getContentEncoding();
                    InputStream is = (InputStream) obj;
                    if (ce.contains("gzip")) {
                        is = new GZIPInputStream(is);
                    }
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int c = 0;
                    while ((c = is.read(buf)) != -1) {
                        os.write(buf, 0, c);
                    }
                    obj = os.toByteArray();
                    is.close();
                }
            }
            if (obj instanceof byte[]) {
                String ct = conn.getContentType();
                String ce = conn.getContentEncoding();
                String[] cts = ct.split(";");
                for (String s : cts) {
                    if (s.startsWith("charset")) {
                        ce = s.substring(s.indexOf("=") + 1);
                    }
                }
                obj = new String((byte[]) obj, (ce != null) ? ce : "ISO-8859-1");
                try {
                    Map m = xJSON.read((String) obj);
                    roomLists.context = (String) m.get("@odata.context");
                    for (Map<String, String> rm : ((List<Map<String, String>>) m.get("value"))) {
                        roomLists.lists.put(rm.get("name"), rm.get("address"));
                    }
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();

            Map<String, Object> r = new LinkedHashMap<String, Object>();

            r.put("headers", conn.getHeaderFields());
            r.put("code", conn.getResponseCode());
            r.put("message", conn.getResponseMessage());
            if (conn.getContentType() != null) {
                String ct = conn.getContentType();
                String ce = conn.getContentEncoding();
                r.put("contentLength", conn.getContentLength());
                r.put("contentEncoding", ce);
                r.put("contentType", ct);

                try {
                    obj = conn.getContent();
                    if (obj instanceof InputStream) {
                        InputStream is = (InputStream) obj;
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int c = 0;
                        while ((c = is.read(buf)) != -1) {
                            os.write(buf, 0, c);
                        }
                        obj = os.toByteArray();
                        is.close();
                    }
                } catch (Throwable th1) {
                }

                try {
                    InputStream is = conn.getErrorStream();
                    if (is != null) {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int c = 0;
                        while ((c = is.read(buf)) != -1) {
                            os.write(buf, 0, c);
                        }
                        obj = os.toByteArray();
                        is.close();
                    }
                } catch (Throwable th1) {
                }

                if (ct != null && (ct.toLowerCase().contains("text") || ct.toLowerCase().contains("json")) && obj instanceof byte[]) {
                    obj = new String((byte[]) obj, (ce != null) ? ce : "ISO-8859-1");
                }

                r.put("content", obj);
                roomLists.errorObject = r;
            }

        }

        return roomLists;
    }

    public RoomLists fetchRooms(final String token, final TIME_PERIOD period) throws IOException {
        final RoomLists r = roomsLists(token);
        r.roomListsNS = System.nanoTime();
        r.roomListsC = r.lists.size();
        for (Entry<String, String> re : r.lists.entrySet()) {
            List<Room> rooms = roomsList(token, re.getValue());
            r.rooms.put(re.getKey(), rooms);
            if (rooms != null) {
                r.roomsListC += rooms.size();
            }
        }
        r.roomsListNS = System.nanoTime();

        if (period != null) {
            r.fetching = true;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (period != null) {
                            int days = 0;
                            switch (period) {
                                case today:
                                    days = 1;
                                    break;
                                case week:
                                    days = 7;
                                    break;
                                case forthnight:
                                    days = 14;
                                    break;
                                case month:
                                    days = 31;
                                    break;
                            }
                            for (Entry<String, List<Room>> re : r.rooms.entrySet()) {
                                if (re.getValue() != null) {
                                    for (Room room : re.getValue()) {
                                        List<TimeSlot> trs = eventsListDays(token, room, days);
                                        if (trs != null) {
                                            room.allocations.addAll(trs);
                                            r.timeSlotsC += trs.size();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException ioex) {
                    } finally {
                        r.timeSlotsNS = System.nanoTime();
                        r.fetching = false;
                    }
                }
            };
            Thread th = new Thread(run);
            th.setDaemon(true);
            th.start();
        }
        return r;
    }

    public List<Room> roomsList(String token, String roomList) throws IOException {
        List<Room> roomsList = new ArrayList<Room>();
        URL url = new URL(roomsListUrl + ((roomList != null) ? "(roomlist='" + roomList + "')" : ""));

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json, text/plain, */*");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("Accept-Language", "en-US;en;q=0.9");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        //conn.setRequestProperty("X-AnchorMailbox", "sergey.sidorov@digia.com");

        Object obj = null;
        try {
            conn.connect();

            obj = conn.getContent();
            if (obj instanceof InputStream) {
                if (obj instanceof InputStream) {
                    String ce = conn.getContentEncoding();
                    InputStream is = (InputStream) obj;
                    if (ce.contains("gzip")) {
                        is = new GZIPInputStream(is);
                    }
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int c = 0;
                    while ((c = is.read(buf)) != -1) {
                        os.write(buf, 0, c);
                    }
                    obj = os.toByteArray();
                    is.close();
                }
            }
            if (obj instanceof byte[]) {
                String ct = conn.getContentType();
                String ce = conn.getContentEncoding();
                String[] cts = ct.split(";");
                for (String s : cts) {
                    if (s.startsWith("charset")) {
                        ce = s.substring(s.indexOf("=") + 1);
                    }
                }
                obj = new String((byte[]) obj, (ce != null) ? ce : "ISO-8859-1");
                try {
                    Map m = xJSON.read((String) obj);
                    String context = (String) m.get("@odata.context");
                    for (Map<String, String> rm : ((List<Map<String, String>>) m.get("value"))) {
                        roomsList.add(new Room(rm.get("name"), rm.get("address")));
                    }
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();

            Map<String, Object> r = new LinkedHashMap<String, Object>();

            r.put("headers", conn.getHeaderFields());
            r.put("code", conn.getResponseCode());
            r.put("message", conn.getResponseMessage());
            if (conn.getContentType() != null) {
                String ct = conn.getContentType();
                String ce = conn.getContentEncoding();
                r.put("contentLength", conn.getContentLength());
                r.put("contentEncoding", ce);
                r.put("contentType", ct);

                try {
                    obj = conn.getContent();
                    if (obj instanceof InputStream) {
                        InputStream is = (InputStream) obj;
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int c = 0;
                        while ((c = is.read(buf)) != -1) {
                            os.write(buf, 0, c);
                        }
                        obj = os.toByteArray();
                        is.close();
                    }
                } catch (Throwable th1) {
                }

                try {
                    InputStream is = conn.getErrorStream();
                    if (is != null) {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int c = 0;
                        while ((c = is.read(buf)) != -1) {
                            os.write(buf, 0, c);
                        }
                        obj = os.toByteArray();
                        is.close();
                    }
                } catch (Throwable th1) {
                }

                if (ct != null && (ct.toLowerCase().contains("text") || ct.toLowerCase().contains("json")) && obj instanceof byte[]) {
                    obj = new String((byte[]) obj, (ce != null) ? ce : "ISO-8859-1");
                }

                r.put("content", obj);
            }

        }

        return roomsList;
    }

    public List<TimeSlot> eventsList(String token, Room room, long start, long end, String next) throws IOException {
        List<TimeSlot> timeSlots = new ArrayList<TimeSlot>();

        String urlS = null;
        if (next != null) {
            urlS = next;
            next = null;
        } else {
            if (start == 0 && end == 0) {
                urlS = apiBase + "v1.0/" + "users/" + room.address + "/events";
            } else {
                // ?startdatetime=2018-05-07T11:09:50.784Z&enddatetime=2018-05-14T11:09:50.784Z
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                urlS = apiBase + "v1.0/" + "users/" + room.address + "/calendarview?startdatetime=" + df.format(new Date(start)) + "&enddatetime=" + df.format(new Date(end));
            }
        }

        URL url = new URL(urlS);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json, text/plain, */*");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("Accept-Language", "en-US;en;q=0.9");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        //conn.setRequestProperty("X-AnchorMailbox", "sergey.sidorov@digia.com");

        Object obj = null;
        try {
            conn.connect();

            obj = conn.getContent();
            if (obj instanceof InputStream) {
                if (obj instanceof InputStream) {
                    String ce = conn.getContentEncoding();
                    InputStream is = (InputStream) obj;
                    if (ce.contains("gzip")) {
                        is = new GZIPInputStream(is);
                    }
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int c = 0;
                    while ((c = is.read(buf)) != -1) {
                        os.write(buf, 0, c);
                    }
                    obj = os.toByteArray();
                    is.close();
                }
            }
            if (obj instanceof byte[]) {
                String ct = conn.getContentType();
                String ce = conn.getContentEncoding();
                String[] cts = ct.split(";");
                for (String s : cts) {
                    if (s.startsWith("charset")) {
                        ce = s.substring(s.indexOf("=") + 1);
                    }
                }
                obj = new String((byte[]) obj, (ce != null) ? ce : "ISO-8859-1");
                try {
                    Map m = xJSON.read((String) obj);
                    String context = (String) m.get("@odata.context");
                    String nextLink = (String) m.get("@odata.nextLink");
                    for (Map<String, Map> rm : ((List<Map<String, Map>>) m.get("value"))) {
                        DateFormat df = getDTF((String) ((Map) rm.get("start")).get("timeZone"));
                        Date from = df.parse((String) ((Map) rm.get("start")).get("dateTime"));
                        Date to = df.parse((String) ((Map) rm.get("end")).get("dateTime"));
                        List<Map> attendees = ((List<Map>) rm.get("attendees"));
                        int req = 0;
                        if (attendees != null) {
                            for (Map ma : attendees) {
                                if ("required".equals(ma.get("type"))) {
                                    req++;
                                }
                            }
                        }

                        TimeSlot ts = new TimeSlot(from.getTime(), to.getTime());
                        ts.attendees = (attendees != null) ? attendees.size() : 0;
                        ts.mandatoryAttendees = req;
                        timeSlots.add(ts);
                    }
                    if (nextLink != null) {
                        List<TimeSlot> nextTSs = eventsList(token, room, start, end, nextLink);
                        if (nextTSs != null) {
                            timeSlots.addAll(nextTSs);
                        }
                    }
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();

            Map<String, Object> r = new LinkedHashMap<String, Object>();

            r.put("headers", conn.getHeaderFields());
            r.put("code", conn.getResponseCode());
            r.put("message", conn.getResponseMessage());
            if (conn.getContentType() != null) {
                String ct = conn.getContentType();
                String ce = conn.getContentEncoding();
                r.put("contentLength", conn.getContentLength());
                r.put("contentEncoding", ce);
                r.put("contentType", ct);

                try {
                    obj = conn.getContent();
                    if (obj instanceof InputStream) {
                        InputStream is = (InputStream) obj;
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int c = 0;
                        while ((c = is.read(buf)) != -1) {
                            os.write(buf, 0, c);
                        }
                        obj = os.toByteArray();
                        is.close();
                    }
                } catch (Throwable th1) {
                }

                try {
                    InputStream is = conn.getErrorStream();
                    if (is != null) {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int c = 0;
                        while ((c = is.read(buf)) != -1) {
                            os.write(buf, 0, c);
                        }
                        obj = os.toByteArray();
                        is.close();
                    }
                } catch (Throwable th1) {
                }

                if (ct != null && (ct.toLowerCase().contains("text") || ct.toLowerCase().contains("json")) && obj instanceof byte[]) {
                    obj = new String((byte[]) obj, (ce != null) ? ce : "ISO-8859-1");
                }

                r.put("content", obj);
            }

        }

        return timeSlots;
    }

    public String messages(String token) throws IOException {
        URL url = new URL(messagesUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json, text/plain, */*");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("Accept-Language", "en-US;en;q=0.9");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Connection", "keep-alive");
        //conn.setRequestProperty("X-AnchorMailbox", "sergey.sidorov@digia.com");

        Object obj = null;
        try {
            conn.connect();

            obj = conn.getContent();
            if (obj instanceof InputStream) {
                String ct = conn.getContentType();
                String ce = conn.getContentEncoding();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int c = 0;
                while ((c = ((InputStream) obj).read()) != -1) {
                    baos.write(buf, 0, c);
                }
                obj = new String(baos.toByteArray(), (ce != null) ? ce : "ISO-8859-1");
            } else if (obj instanceof byte[]) {
                String ct = conn.getContentType();
                String ce = conn.getContentEncoding();
                obj = new String((byte[]) obj, (ce != null) ? ce : "ISO-8859-1");
            }
        } catch (Throwable th) {
            th.printStackTrace();

            Map<String, Object> r = new LinkedHashMap<String, Object>();

            r.put("headers", conn.getHeaderFields());
            r.put("code", conn.getResponseCode());
            r.put("message", conn.getResponseMessage());
            if (conn.getContentType() != null) {
                String ct = conn.getContentType();
                String ce = conn.getContentEncoding();
                r.put("contentLength", conn.getContentLength());
                r.put("contentEncoding", ce);
                r.put("contentType", ct);

                try {
                    obj = conn.getContent();
                    if (obj instanceof InputStream) {
                        InputStream is = (InputStream) obj;
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int c = 0;
                        while ((c = is.read(buf)) != -1) {
                            os.write(buf, 0, c);
                        }
                        obj = os.toByteArray();
                        is.close();
                    }
                } catch (Throwable th1) {
                }

                try {
                    InputStream is = conn.getErrorStream();
                    if (is != null) {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int c = 0;
                        while ((c = is.read(buf)) != -1) {
                            os.write(buf, 0, c);
                        }
                        obj = os.toByteArray();
                        is.close();
                    }
                } catch (Throwable th1) {
                }

                if (ct != null && (ct.toLowerCase().contains("text") || ct.toLowerCase().contains("json")) && obj instanceof byte[]) {
                    obj = new String((byte[]) obj, (ce != null) ? ce : "ISO-8859-1");
                }

                r.put("content", obj);
            }

        }

        return "" + obj;
    }

    public List<TimeSlot> eventsListDays(String token, Room room, int days) throws IOException {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        long start = c.getTimeInMillis();

        if (days > 1) {
            c.add(Calendar.DAY_OF_YEAR, days - 1);
        }
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);

        long end = c.getTimeInMillis();

        return eventsList(token, room, start, end, null);
    }

    public static long today(TimeZone tz, Boolean start) {
        if (tz == null) {
            tz = TimeZone.getDefault();
        }
        Calendar c = Calendar.getInstance(tz);
        if (start != null && start) {
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
        } else if (start != null && !start) {
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            c.set(Calendar.MILLISECOND, 999);
        }
        return c.getTimeInMillis();
    }

    public static long roundTimeHour(TimeZone tz, long time, boolean down) {
        if (tz == null) {
            tz = TimeZone.getDefault();
        }
        Calendar c = Calendar.getInstance(tz);
        c.setTimeInMillis(time);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if (!down) {
            c.add(Calendar.HOUR_OF_DAY, 1);
        }
        return c.getTimeInMillis();
    }

    ////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////// utilities
    ////////////////////////////////////////////////////////////////////////////
    public Map<String, Object> doGet(URL url, Object... params) throws IOException {
        Map<String, Object> r = new LinkedHashMap<String, Object>();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.connect();

        r.put("headers", conn.getHeaderFields());
        r.put("code", conn.getResponseCode());
        r.put("message", conn.getResponseMessage());
        if (conn.getContentType() != null) {
            r.put("contentLength", conn.getContentLength());
            r.put("contentEncoding", conn.getContentEncoding());
            r.put("contentType", conn.getContentType());

            Object obj = conn.getContent();
            if (obj instanceof InputStream) {
                InputStream is = (InputStream) obj;
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int c = 0;
                while ((c = is.read(buf)) != -1) {
                    os.write(buf, 0, c);
                }
                obj = os.toByteArray();
                is.close();
            }
            r.put("content", obj);
        }

        return r;
    }

////////////////////////////////////////////////////////////////////////////////
    static Map<String, DateFormat> dtfs = new HashMap<String, DateFormat>();

    public static DateFormat getDTF(String timeZone) {
        DateFormat df = dtfs.get(timeZone);
        if (df == null) {
            df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            df.setTimeZone(TimeZone.getTimeZone(timeZone));
            dtfs.put(timeZone, df);
        }
        return df;
    }

////////////////////////////////////////////////////////////////////////////////    
    public static class RoomLists implements Serializable {

        public String context;
        public Map<String, String> lists = new LinkedHashMap<String, String>();
        public Map<String, List<Room>> rooms = new LinkedHashMap<String, List<Room>>();
        //
        public String error;
        public Object errorObject;
        // timing
        long createdNS = System.nanoTime();
        long roomListsNS = 0;
        long roomsListNS = 0;
        long timeSlotsNS = 0;
        long roomListsC = 0;
        long roomsListC = 0;
        long timeSlotsC = 0;

        public transient boolean fetching = false;

        public static class Room implements Serializable {

            public String name;
            public String address;
            public List<TimeSlot> allocations = new ArrayList<TimeSlot>();

            public Room() {
            }

            public Room(String name, String address) {
                this.name = name;
                this.address = address;
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("Room{" + "name=" + name + ", address=" + address + ", allocations=" + allocations.size());
                for (TimeSlot ts : allocations) {
                    sb.append("\n  " + ts);
                }
                sb.append('}');
                return sb.toString();
            }

        }

        static DateFormat tsDTF = new SimpleDateFormat("yyyy-MM-dd HH:mm z");

        static {
            tsDTF.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        public static class TimeSlot implements Serializable {

            public long from;
            public long to;
            public long created;
            public long modified;
            public int attendees;
            public int mandatoryAttendees;

            public TimeSlot(long from, long to) {
                this.from = from;
                this.to = to;
            }

            @Override
            public String toString() {
                return "TimeSlot{" + "from=" + from + " (" + tsDTF.format(new Date(from)) + ")" + ", to=" + to + ", length=" + ((to - from) / 1000 / 60 / 60f) + "h" + ", attendees=" + attendees + ", mandatoryAttendees=" + mandatoryAttendees + '}';
            }

        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(1024);
            sb.append("RoomLists{");
            sb.append("context=" + context);
            sb.append(", lists=" + lists.size());
            sb.append(", rooms=" + rooms.size());
            if (error != null) {
                sb.append(", error=" + error);
            }
            if (errorObject != null) {
                sb.append(", errorObject=" + errorObject);
            }
            sb.append("\n  Timing:");
            sb.append("\n    room lists (" + roomListsC + ") " + (roomListsNS - createdNS) / 1000000f + "ms.");
            sb.append("\n    rooms      (" + roomsListC + ") " + (roomsListNS - roomListsNS) / 1000000f + "ms.");
            sb.append("\n    time slots (" + timeSlotsC + ") " + (timeSlotsNS - roomsListNS) / 1000000f + "ms.");
            if (!lists.isEmpty()) {
                sb.append("\nRoom lists:");
                for (Entry<String, String> entry : lists.entrySet()) {
                    sb.append("\n  " + entry.getKey() + " -> " + entry.getValue());
                }
            }
            if (!rooms.isEmpty()) {
                sb.append("\nRooms:");
                for (Entry<String, List<Room>> entry : rooms.entrySet()) {
                    List<Room> rs = entry.getValue();
                    sb.append("\n  " + entry.getKey() + " -> " + ((rs != null) ? rs.size() : "<none>"));
                    if (rs != null) {
                        for (Room room : rs) {
                            sb.append("\n    " + ("" + room).replace("\n", "\n    "));
                        }
                    }
                }
            }
            sb.append('}');
            return sb.toString();
        }

        public long[] timeSlotsRange() {
            long[] range = null;
            for (Entry<String, List<Room>> rooms : rooms.entrySet()) {
                for (Room r : rooms.getValue()) {
                    for (TimeSlot ts : r.allocations) {
                        if (range == null) {
                            range = new long[]{ts.from, ts.to};
                        } else {
                            range[0] = Math.min(range[0], ts.from);
                            range[1] = Math.max(range[1], ts.to);
                        }
                    }
                }
            }
            if (range == null) {
                range = new long[]{today(null, true), today(null, false)};
            }
            return range;
        }

        public String toASCII(TimeZone tz, long from, long to, long step) {
            StringBuilder sb = new StringBuilder(1024);

            if (tz == null) {
                tz = TimeZone.getDefault();
            }
            DateFormat df = new SimpleDateFormat("MM-dd");
            DateFormat tf = new SimpleDateFormat("HH:mm");
            tf.setTimeZone(tz);

            long[] time = new long[(int) ((to - from) / step) + 2];
            time[0] = from;
            for (int i = 1; i < time.length; i++) {
                time[i] = time[i - 1] + step;
            }

            sb.append("Room allocations in " + tz.getDisplayName());
            String scale = "";
            Calendar c = Calendar.getInstance(tz);
            if ((to - from) > 1000 * 60 * 60 * 24 * 1.2) {
                for (int i = 0; i < time.length - 1; i++) {
                    c.setTimeInMillis(time[i]);
                    if (c.get(Calendar.MINUTE) == 0
                            && c.get(Calendar.HOUR_OF_DAY) == 0) {
                        scale += df.format(c.getTime());
                        i += 4;
                    } else {
                        scale += '_';
                    }
                }
                scale += '\n';
            }
            for (int i = 0; i < time.length - 1; i++) {
                c.setTimeInMillis(time[i]);
                if (i % 12 == 0) {
                    //scale += '|';
                    scale += tf.format(c.getTime());
                    i += 4;
                } else {
                    scale += ' ';
                }
            }
            scale += '\n';
            for (int i = 0; i < time.length - 1; i++) {
                c.setTimeInMillis(time[i]);
                if (c.get(Calendar.MINUTE) == 0) {
                    scale += '|';
                } else {
                    scale += '_';
                }
            }

            for (Entry<String, List<Room>> rooms : rooms.entrySet()) {
                sb.append('\n');
                sb.append(rooms.getKey());
                sb.append('\n');
                sb.append(scale);
                for (Room r : rooms.getValue()) {
                    sb.append('\n');
                    for (int i = 0; i < time.length - 1; i++) {
                        boolean allocated = false;
                        for (TimeSlot ts : r.allocations) {
                            if (time[i] >= ts.from && time[i + 1] <= ts.to) {
                                allocated = true;
                                break;
                            }
                        }
                        sb.append((allocated) ? "*" : ".");
                    }
                    sb.append("  " + r.name + "  " + r.address);
                }
            }

            return sb.toString();
        }
    }

    public static void main(String[] args) throws Exception {
        String ttt = "{\"token_type\":\"Bearer\",\"scope\":\"Mail.Read User.Read\",\"expires_in\":3599,\"ext_expires_in\":0,\"access_token\":\"eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFEWDhHQ2k2SnM2U0s4MlRzRDJQYjdyOHhpR2tiQUIxZ2F2cHhPZjEtdDVkZG9CWHJtdGV6WVl5Y2gwTTFkeS1DQ1QtLVEwbmRmSF9QZjIyNk9iaXB4RmE2N0FNRlgxSmRJSzl5YllnQmRnVUNBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoiaUJqTDFSY3F6aGl5NGZweEl4ZFpxb2hNMllrIiwia2lkIjoiaUJqTDFSY3F6aGl5NGZweEl4ZFpxb2hNMllrIn0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC82ZmNlNGJiOC0zNTAxLTQxYzktYWZjYy1kYjBmYjUxYzdlM2QvIiwiaWF0IjoxNTI1NDMzODA5LCJuYmYiOjE1MjU0MzM4MDksImV4cCI6MTUyNTQzNzcwOSwiYWNyIjoiMSIsImFpbyI6IlkyZGdZSkI1RUIvQTBjdWxkWDdaT1o2WDBuci9FNUp1dEVhMS9GaHVyMUN5bWZHVDRsY0EiLCJhbXIiOlsicHdkIl0sImFwcF9kaXNwbGF5bmFtZSI6InRlc3Qgb3V0bG9vayByZXN0IiwiYXBwaWQiOiI4NmIwZjYxYy0yZTY5LTQxZGYtYmRiZS00OWViY2UzZjc3OTUiLCJhcHBpZGFjciI6IjEiLCJmYW1pbHlfbmFtZSI6IlNpZG9yb3YiLCJnaXZlbl9uYW1lIjoiU2VyZ2V5IiwiaXBhZGRyIjoiOTEuMjE3LjI0OC4xMSIsIm5hbWUiOiJTaWRvcm92IFNlcmdleSIsIm9pZCI6ImIxMTMxMzk1LWY5NTAtNDFiYi1iZDVmLTk2OWFiMmFkMzZmNyIsIm9ucHJlbV9zaWQiOiJTLTEtNS0yMS0yNDMwNjcxNDYyLTI4NTI5NzE1NTEtMjc5NjAxMTA1NS0yMTQ1MiIsInBsYXRmIjoiMyIsInB1aWQiOiIxMDAzMDAwMDg5MzFCNTMxIiwic2NwIjoiTWFpbC5SZWFkIFVzZXIuUmVhZCIsInNpZ25pbl9zdGF0ZSI6WyJpbmtub3dubnR3ayIsImttc2kiXSwic3ViIjoiYmdObElSOWVoYnpqMWlPU05FZkVXRjJJZnFoeWgzQ242UXhjZ3otalp6OCIsInRpZCI6IjZmY2U0YmI4LTM1MDEtNDFjOS1hZmNjLWRiMGZiNTFjN2UzZCIsInVuaXF1ZV9uYW1lIjoic2VyZ2V5LnNpZG9yb3ZAZGlnaWEuY29tIiwidXBuIjoic2VyZ2V5LnNpZG9yb3ZAZGlnaWEuY29tIiwidXRpIjoid01WaDduUW0xVTZiQTNRbEZGWUZBQSIsInZlciI6IjEuMCJ9.O2fKeeVKNCEqE9aJ-ODy8OH5chHGHv9gTnffx0bAyyqJEXFGgmZS11x_a0ahpWbS-Ro6dqKvI4m1iKFUN1cDoqByN8UbcRYsdgNG5rbZeM9sQUNXWWetQr_bxRMz-QL61II_cYywYBAM3SyYUgBmr6PUm_gGqPnmO9CR--mEhwpJDGG1_3-ZVthaBAQf-fqrVf4BoKVrGDpNs3CcCqmHdVQU8payV6E4l8T6jtY0i5fwEQlrxn7SwR_URB21yq8zu6pVgDJBOOCblkgof3H_cthJfIOREHBPKqebUVHt_1W0qCLFNNNpv4rd_k-0XoS4ctiVSX2fzPpucWXuzWleUw\",\"id_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImlCakwxUmNxemhpeTRmcHhJeGRacW9oTTJZayJ9.eyJhdWQiOiI4NmIwZjYxYy0yZTY5LTQxZGYtYmRiZS00OWViY2UzZjc3OTUiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNmZjZTRiYjgtMzUwMS00MWM5LWFmY2MtZGIwZmI1MWM3ZTNkL3YyLjAiLCJpYXQiOjE1MjU0MzM4MDksIm5iZiI6MTUyNTQzMzgwOSwiZXhwIjoxNTI1NDM3NzA5LCJhaW8iOiJBVFFBeS84SEFBQUE4QUg1MFNLR0RWb1BBQ01WMXVTeWNxd2NXY1VsY2pFTVRNVDk3cFA4Vytnd05CUXRKeGw2cUtvRENjSFdmMVY3Iiwic3ViIjoiTENJWDFvSGpyNllfS1hHRFppQXY1NlVFenpLakNNX043S1pPc2hfUTg2YyIsInRpZCI6IjZmY2U0YmI4LTM1MDEtNDFjOS1hZmNjLWRiMGZiNTFjN2UzZCIsInV0aSI6IndNVmg3blFtMVU2YkEzUWxGRllGQUEiLCJ2ZXIiOiIyLjAifQ.X39q_gP2rCvm27Bn4u29_5YIc8Pd9Ecjz7zIkNXHDgseJN5UVKe1XP1U9ZVrRnuRfDxRRIbiRUkVPESBTXS-2q77UPG6jE6q762z8Pi0srD3UpiGJg4AneStkvXONb5j-ueVW0HuMZLsucTSm3Ht5nTRLWlkF5MTe-59ZcMt4PORx71H9s3IFunqsLQ_Uzja7-aSjSzGMAuUivfa0K_LgcHtEVBKUnoswTBB-lXMNP4T4bcR2Oy2CCAWgqheaNlQqgvIkm7__MDlKufH_FqgQJWwmDG2LCq-eEGSzIttteBm78W-Xc5PjPQF1WZCDv5Kmyj_kZkmsD1PS8Q-7j9K9g\"}";
        ttt = "{\"@odata.context\":\"https://graph.microsoft.com/beta/$metadata#Collection(microsoft.graph.emailAddress)\",\"value\":[{\"name\":\"Rooms-HKI-Atomitie2-FLR-A2\",\"address\":\"Rooms-HKI-Atomitie2-FLR-A2@digia.com\"},{\"name\":\"Rooms-HKI-Atomitie2-FLR-A3\",\"address\":\"Rooms-HKI-Atomitie2-FLR-A3@digia.com\"},{\"name\":\"Rooms-HKI-Atomitie2-FLR-A4\",\"address\":\"Rooms-HKI-Atomitie2-FLR-A4@digia.com\"},{\"name\":\"Rooms-HKI-Atomitie2-FLR-A5\",\"address\":\"Rooms-HKI-Atomitie2-FLR-A5@digia.com\"},{\"name\":\"Rooms-HKI-Atomitie2-FLR-A6\",\"address\":\"Rooms-HKI-Atomitie2-FLR-A6@digia.com\"},{\"name\":\"Rooms-HKI-Atomitie2-FLR-A7\",\"address\":\"Rooms-HKI-Atomitie2-FLR-A7@digia.com\"},{\"name\":\"Rooms-HKI-Atomitie2-FLR-B6\",\"address\":\"Rooms-HKI-Atomitie2-FLR-B6@digia.com\"},{\"name\":\"Rooms-HKI-Atomitie2-FLR-B7\",\"address\":\"Rooms-HKI-Atomitie2-FLR-B7@digia.com\"},{\"name\":\"Rooms-Jyvaskyla\",\"address\":\"Rooms-Jyvaskyla@digia.com\"},{\"name\":\"Rooms-Rauma\",\"address\":\"Rooms-Rauma@digia.com\"},{\"name\":\"Rooms-Tampere\",\"address\":\"Rooms-Tampere@digia.com\"},{\"name\":\"rooms-turku\",\"address\":\"rooms-turku@digia.com\"},{\"name\":\"Rooms-Vaasa\",\"address\":\"Rooms-Vaasa@digia.com\"}]}";
        Map m = xJSON.read(ttt);
        String ms = xJSON.write(m);

        String tt = (String) m.get("token_type");
        String at = (String) m.get("access_token");
        String it = (String) m.get("id_token");

        OutlookAuth oa = new OutlookAuth("86b0f61c-2e69-41df-bdbe-49ebce3f7795");

        at = "eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFEWDhHQ2k2SnM2U0s4MlRzRDJQYjdyTUZYWkxfZmNGa1phUEFrZnpzSFU3V01DcS15d05yYW5tQnFhNnVpMFlpV19fWnRtZXZCTmdaa24zUnQzYzR1RGdIelBSNmowQmNCVUtCcTFVRk9BNkNBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoiaUJqTDFSY3F6aGl5NGZweEl4ZFpxb2hNMllrIiwia2lkIjoiaUJqTDFSY3F6aGl5NGZweEl4ZFpxb2hNMllrIn0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC82ZmNlNGJiOC0zNTAxLTQxYzktYWZjYy1kYjBmYjUxYzdlM2QvIiwiaWF0IjoxNTI1NzU3OTgyLCJuYmYiOjE1MjU3NTc5ODIsImV4cCI6MTUyNTc2MTg4MiwiYWNyIjoiMSIsImFpbyI6IkFTUUEyLzhIQUFBQTZNV0swcXozU1NuQmZyNk0yYmZDQUFoYzFVS2xlZWdmejNLYmN4VVhqVnM9IiwiYW1yIjpbInB3ZCJdLCJhcHBfZGlzcGxheW5hbWUiOiJ0ZXN0IG91dGxvb2sgcmVzdCIsImFwcGlkIjoiODZiMGY2MWMtMmU2OS00MWRmLWJkYmUtNDllYmNlM2Y3Nzk1IiwiYXBwaWRhY3IiOiIxIiwiZmFtaWx5X25hbWUiOiJTaWRvcm92IiwiZ2l2ZW5fbmFtZSI6IlNlcmdleSIsImlwYWRkciI6IjkxLjIxNy4yNDguMTEiLCJuYW1lIjoiU2lkb3JvdiBTZXJnZXkiLCJvaWQiOiJiMTEzMTM5NS1mOTUwLTQxYmItYmQ1Zi05NjlhYjJhZDM2ZjciLCJvbnByZW1fc2lkIjoiUy0xLTUtMjEtMjQzMDY3MTQ2Mi0yODUyOTcxNTUxLTI3OTYwMTEwNTUtMjE0NTIiLCJwbGF0ZiI6IjMiLCJwdWlkIjoiMTAwMzAwMDA4OTMxQjUzMSIsInNjcCI6IkNhbGVuZGFycy5SZWFkIENhbGVuZGFycy5SZWFkLlNoYXJlZCBDYWxlbmRhcnMuUmVhZFdyaXRlIENvbnRhY3RzLlJlYWQgQ29udGFjdHMuUmVhZFdyaXRlIERldmljZS5SZWFkIEZpbGVzLlJlYWQgRmlsZXMuUmVhZFdyaXRlLkFsbCBNYWlsLlJlYWQgTWFpbC5SZWFkV3JpdGUgTWFpbGJveFNldHRpbmdzLlJlYWRXcml0ZSBOb3Rlcy5SZWFkV3JpdGUuQWxsIFBlb3BsZS5SZWFkIFNpdGVzLlJlYWRXcml0ZS5BbGwgVGFza3MuUmVhZCBUYXNrcy5SZWFkV3JpdGUgVXNlci5SZWFkIFVzZXIuUmVhZEJhc2ljLkFsbCBVc2VyLlJlYWRXcml0ZSIsInNpZ25pbl9zdGF0ZSI6WyJpbmtub3dubnR3ayIsImttc2kiXSwic3ViIjoiYmdObElSOWVoYnpqMWlPU05FZkVXRjJJZnFoeWgzQ242UXhjZ3otalp6OCIsInRpZCI6IjZmY2U0YmI4LTM1MDEtNDFjOS1hZmNjLWRiMGZiNTFjN2UzZCIsInVuaXF1ZV9uYW1lIjoic2VyZ2V5LnNpZG9yb3ZAZGlnaWEuY29tIiwidXBuIjoic2VyZ2V5LnNpZG9yb3ZAZGlnaWEuY29tIiwidXRpIjoiLWhOQW9hMXZtVXlyRExjYm9yWXZBQSIsInZlciI6IjEuMCJ9.VHizjBamgSb0BjPgY4qlAmtjfGErMGbxShCjiYaSB7L-SzfdqaSp73hSbaBijRTRCYIqSZ6g2yN8FSYDlQSSB5S_tJ8enEz_nEJ7kRkMeIp4Is3-0KfawChZAAUZHWKPaQ0hAevpYDPgTXTzEsiazp7g17BHO9or45KerdROe0HXAm7x6tZ-uaGPLH5zLKTztFM6PGmB1CDYXWBLCXYYxu7sIGRHmqYfG_FKrFQrtgezuM6bdWjdZU1Lw3UiwNqu0rtVQazVmYX7d_POoDxRa44vvDseWnV8BTtKjSd_mdyxs_zGoW5qKtvlYAqH9S3NhQ75yiPyBOUwhLzYgEeMQQ";
        it = at;

        for (String[] token2 : new String[][]{{"access", at}}) {
            System.out.println("TOKEN: " + token2[0] + " -> " + token2[1]);
            try {
                //RoomLists rl = oa.roomsLists(token2[1]);
                RoomLists rl = oa.fetchRooms(token2[1], TIME_PERIOD.today);
                System.out.println("Response:\n" + rl);
                List<TimeSlot> tss = oa.eventsListDays(
                        token2[1],
                        rl.rooms.get("Rooms-HKI-Atomitie2-FLR-A2").get(0),
                        1);
                List<TimeSlot> tss2 = oa.eventsListDays(
                        token2[1],
                        rl.rooms.get("Rooms-HKI-Atomitie2-FLR-A2").get(0),
                        7);
                System.out.println("Time slots(today, " + tss.size() + "):\n" + tss);
                System.out.println("Time slots(week+, " + tss2.size() + "):\n" + tss2);

                System.out.println("JSON: " + xJSON.write(rl));
                System.out.println((rl.fetching) ? "\nfetching timeslots " : "");
                while (rl.fetching) {
                    System.out.print(".");
                    System.out.flush();
                    Thread.sleep(1000);
                }
                System.out.println();
                long[] range = rl.timeSlotsRange();
                range[0] = roundTimeHour(null, range[0], true);
                range[1] = roundTimeHour(null, range[1], false);
                System.out.println("ASCII: " + rl.toASCII(null, range[0], range[1], 1000 * 60 * 15 * 2 / 6));
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }
}
