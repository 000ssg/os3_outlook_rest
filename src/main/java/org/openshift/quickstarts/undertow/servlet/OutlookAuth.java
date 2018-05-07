package org.openshift.quickstarts.undertow.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OutlookAuth {
    
    String authUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
    String tokenUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
    
    String apiBase = "https://graph.microsoft.com/";
    String roomsListUrl = apiBase + "beta/me/findroomlists";
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
    
    List<String> permissions = new ArrayList<String>() {
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
    
    Collection<String> adminPermissions = new HashSet<String>() {
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
                "Reports.Read.All" // A,D,P
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
    
    public String roomsLists(String token) throws IOException {
        URL url = new URL(roomsListUrl);
        
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
            if (obj instanceof byte[]) {
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
    
    public static class DummyHttp {
        
    }
    
    public static void main(String[] args) throws Exception {
        String ttt = "{\"token_type\":\"Bearer\",\"scope\":\"Mail.Read User.Read\",\"expires_in\":3599,\"ext_expires_in\":0,\"access_token\":\"eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFEWDhHQ2k2SnM2U0s4MlRzRDJQYjdyOHhpR2tiQUIxZ2F2cHhPZjEtdDVkZG9CWHJtdGV6WVl5Y2gwTTFkeS1DQ1QtLVEwbmRmSF9QZjIyNk9iaXB4RmE2N0FNRlgxSmRJSzl5YllnQmRnVUNBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoiaUJqTDFSY3F6aGl5NGZweEl4ZFpxb2hNMllrIiwia2lkIjoiaUJqTDFSY3F6aGl5NGZweEl4ZFpxb2hNMllrIn0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC82ZmNlNGJiOC0zNTAxLTQxYzktYWZjYy1kYjBmYjUxYzdlM2QvIiwiaWF0IjoxNTI1NDMzODA5LCJuYmYiOjE1MjU0MzM4MDksImV4cCI6MTUyNTQzNzcwOSwiYWNyIjoiMSIsImFpbyI6IlkyZGdZSkI1RUIvQTBjdWxkWDdaT1o2WDBuci9FNUp1dEVhMS9GaHVyMUN5bWZHVDRsY0EiLCJhbXIiOlsicHdkIl0sImFwcF9kaXNwbGF5bmFtZSI6InRlc3Qgb3V0bG9vayByZXN0IiwiYXBwaWQiOiI4NmIwZjYxYy0yZTY5LTQxZGYtYmRiZS00OWViY2UzZjc3OTUiLCJhcHBpZGFjciI6IjEiLCJmYW1pbHlfbmFtZSI6IlNpZG9yb3YiLCJnaXZlbl9uYW1lIjoiU2VyZ2V5IiwiaXBhZGRyIjoiOTEuMjE3LjI0OC4xMSIsIm5hbWUiOiJTaWRvcm92IFNlcmdleSIsIm9pZCI6ImIxMTMxMzk1LWY5NTAtNDFiYi1iZDVmLTk2OWFiMmFkMzZmNyIsIm9ucHJlbV9zaWQiOiJTLTEtNS0yMS0yNDMwNjcxNDYyLTI4NTI5NzE1NTEtMjc5NjAxMTA1NS0yMTQ1MiIsInBsYXRmIjoiMyIsInB1aWQiOiIxMDAzMDAwMDg5MzFCNTMxIiwic2NwIjoiTWFpbC5SZWFkIFVzZXIuUmVhZCIsInNpZ25pbl9zdGF0ZSI6WyJpbmtub3dubnR3ayIsImttc2kiXSwic3ViIjoiYmdObElSOWVoYnpqMWlPU05FZkVXRjJJZnFoeWgzQ242UXhjZ3otalp6OCIsInRpZCI6IjZmY2U0YmI4LTM1MDEtNDFjOS1hZmNjLWRiMGZiNTFjN2UzZCIsInVuaXF1ZV9uYW1lIjoic2VyZ2V5LnNpZG9yb3ZAZGlnaWEuY29tIiwidXBuIjoic2VyZ2V5LnNpZG9yb3ZAZGlnaWEuY29tIiwidXRpIjoid01WaDduUW0xVTZiQTNRbEZGWUZBQSIsInZlciI6IjEuMCJ9.O2fKeeVKNCEqE9aJ-ODy8OH5chHGHv9gTnffx0bAyyqJEXFGgmZS11x_a0ahpWbS-Ro6dqKvI4m1iKFUN1cDoqByN8UbcRYsdgNG5rbZeM9sQUNXWWetQr_bxRMz-QL61II_cYywYBAM3SyYUgBmr6PUm_gGqPnmO9CR--mEhwpJDGG1_3-ZVthaBAQf-fqrVf4BoKVrGDpNs3CcCqmHdVQU8payV6E4l8T6jtY0i5fwEQlrxn7SwR_URB21yq8zu6pVgDJBOOCblkgof3H_cthJfIOREHBPKqebUVHt_1W0qCLFNNNpv4rd_k-0XoS4ctiVSX2fzPpucWXuzWleUw\",\"id_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImlCakwxUmNxemhpeTRmcHhJeGRacW9oTTJZayJ9.eyJhdWQiOiI4NmIwZjYxYy0yZTY5LTQxZGYtYmRiZS00OWViY2UzZjc3OTUiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNmZjZTRiYjgtMzUwMS00MWM5LWFmY2MtZGIwZmI1MWM3ZTNkL3YyLjAiLCJpYXQiOjE1MjU0MzM4MDksIm5iZiI6MTUyNTQzMzgwOSwiZXhwIjoxNTI1NDM3NzA5LCJhaW8iOiJBVFFBeS84SEFBQUE4QUg1MFNLR0RWb1BBQ01WMXVTeWNxd2NXY1VsY2pFTVRNVDk3cFA4Vytnd05CUXRKeGw2cUtvRENjSFdmMVY3Iiwic3ViIjoiTENJWDFvSGpyNllfS1hHRFppQXY1NlVFenpLakNNX043S1pPc2hfUTg2YyIsInRpZCI6IjZmY2U0YmI4LTM1MDEtNDFjOS1hZmNjLWRiMGZiNTFjN2UzZCIsInV0aSI6IndNVmg3blFtMVU2YkEzUWxGRllGQUEiLCJ2ZXIiOiIyLjAifQ.X39q_gP2rCvm27Bn4u29_5YIc8Pd9Ecjz7zIkNXHDgseJN5UVKe1XP1U9ZVrRnuRfDxRRIbiRUkVPESBTXS-2q77UPG6jE6q762z8Pi0srD3UpiGJg4AneStkvXONb5j-ueVW0HuMZLsucTSm3Ht5nTRLWlkF5MTe-59ZcMt4PORx71H9s3IFunqsLQ_Uzja7-aSjSzGMAuUivfa0K_LgcHtEVBKUnoswTBB-lXMNP4T4bcR2Oy2CCAWgqheaNlQqgvIkm7__MDlKufH_FqgQJWwmDG2LCq-eEGSzIttteBm78W-Xc5PjPQF1WZCDv5Kmyj_kZkmsD1PS8Q-7j9K9g\"}";
        Map m = xJSON.read(ttt);
        
        String tt = (String) m.get("token_type");
        String at = (String) m.get("access_token");
        String it = (String) m.get("id_token");
        
        OutlookAuth oa = new OutlookAuth("86b0f61c-2e69-41df-bdbe-49ebce3f7795");
        
        at = "eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFEWDhHQ2k2SnM2U0s4MlRzRDJQYjdyV09fOHJCeC15eC1wcGRRbjdyZUUwOUJVLTBESXJfajNQZFZLUHFVeW1nSXhGM2V3T3V4VjVBU2pZeFgwcTRJRWdwNXdqbE9vUkFZWUJ3RjRQbDhNYmlBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoiaUJqTDFSY3F6aGl5NGZweEl4ZFpxb2hNMllrIiwia2lkIjoiaUJqTDFSY3F6aGl5NGZweEl4ZFpxb2hNMllrIn0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC82ZmNlNGJiOC0zNTAxLTQxYzktYWZjYy1kYjBmYjUxYzdlM2QvIiwiaWF0IjoxNTI1Njc4Mjc0LCJuYmYiOjE1MjU2NzgyNzQsImV4cCI6MTUyNTY4MjE3NCwiYWNyIjoiMSIsImFpbyI6IkFTUUEyLzhIQUFBQWcwa1g0elN4MWNudWQ4QjJxT3R3V3pnU2NlZnIwY2VGZHFyTFE4OEV1Unc9IiwiYW1yIjpbInB3ZCJdLCJhcHBfZGlzcGxheW5hbWUiOiJ0ZXN0IG91dGxvb2sgcmVzdCIsImFwcGlkIjoiODZiMGY2MWMtMmU2OS00MWRmLWJkYmUtNDllYmNlM2Y3Nzk1IiwiYXBwaWRhY3IiOiIxIiwiZmFtaWx5X25hbWUiOiJTaWRvcm92IiwiZ2l2ZW5fbmFtZSI6IlNlcmdleSIsImlwYWRkciI6IjkxLjIxNy4yNDguMTEiLCJuYW1lIjoiU2lkb3JvdiBTZXJnZXkiLCJvaWQiOiJiMTEzMTM5NS1mOTUwLTQxYmItYmQ1Zi05NjlhYjJhZDM2ZjciLCJvbnByZW1fc2lkIjoiUy0xLTUtMjEtMjQzMDY3MTQ2Mi0yODUyOTcxNTUxLTI3OTYwMTEwNTUtMjE0NTIiLCJwbGF0ZiI6IjMiLCJwdWlkIjoiMTAwMzAwMDA4OTMxQjUzMSIsInNjcCI6IkNhbGVuZGFycy5SZWFkIENvbnRhY3RzLlJlYWQgRGV2aWNlLlJlYWQgRmlsZXMuUmVhZCBNYWlsLlJlYWQgUGVvcGxlLlJlYWQgVGFza3MuUmVhZCBVc2VyLlJlYWQiLCJzaWduaW5fc3RhdGUiOlsiaW5rbm93bm50d2siLCJrbXNpIl0sInN1YiI6ImJnTmxJUjllaGJ6ajFpT1NORWZFV0YySWZxaHloM0NuNlF4Y2d6LWpaejgiLCJ0aWQiOiI2ZmNlNGJiOC0zNTAxLTQxYzktYWZjYy1kYjBmYjUxYzdlM2QiLCJ1bmlxdWVfbmFtZSI6InNlcmdleS5zaWRvcm92QGRpZ2lhLmNvbSIsInVwbiI6InNlcmdleS5zaWRvcm92QGRpZ2lhLmNvbSIsInV0aSI6IkpsMFcyRFhMRFVDVWFTY21WR0FQQUEiLCJ2ZXIiOiIxLjAifQ.moPXJ3943Y6LhAFptxkdYWSp9PO-hS8Eu2COUgINMWP73yL08Cr3QfWtreQQOncKVNTOPuEgWnCJhKAaBan8Hef1aRkT6P9C2IbQkvOhvgsiKVxmlUwhYFjR7q5ZplO4Mlsmr-l4Yasefe_KDAHh4qqHZ0p76MaYSUMaHe_EwMUmMTFJ_HZ9Xi54MMnNj5qn3pXtaHc90IeD7fPGif9yLQAdd6mVTH11ZrynhUYKwqOlsXqcJf5MRZktGUmXXdDln14FuYpntJaW_7KvlERQso7-UEwFqoQnO-TEBzFAHUgfiN8tEDWcHUm0t4ud9qIds7d9iSSbjiIH8zA1Zq1DGg";
        it = at;
        
        for (String[] token2 : new String[][]{{"access", at}, {"id", it}}) {
            System.out.println("TOKEN: " + token2[0] + " -> " + token2[1]);
            try {
                String rl = oa.roomsLists(token2[1]);
                System.out.println("Response:\n" + rl);
            } catch (Throwable th) {
            }
        }
    }
}
