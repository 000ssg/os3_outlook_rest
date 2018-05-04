package org.openshift.quickstarts.undertow.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OutlookAuth {

    String authUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
    String tokenUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
    String roomsListUrl = "https://outlook.office.com/api/beta/me/findroomlists";

    String redirect = "https://openjdk-app-ddd.1d35.starter-us-east-1.openshiftapps.com/tokenized";
    String clientId;
    String clientSecret;
    String responseType = "code";
    String grantType = "authorization_code";
    String scope = "openid+User.Read";
    //
    String code;

    public OutlookAuth(String clientId) {
        this.clientId = clientId;
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

                    String rm = (String) obj;
                    int idx = rm.indexOf("\"access_token\"");
                    if (idx != -1) {
                        idx = rm.indexOf(":", idx);
                    }
                    if (idx != -1) {
                        String at = rm.substring(idx + 2);
                        idx = at.indexOf("\"");
                        at = at.substring(0, idx);
                        r.put("token", at);
                    }

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
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        //conn.setRequestProperty("X-AnchorMailbox","sergey.sidorov@digia.com");

        conn.connect();

        Object obj = conn.getContent();
        if (obj instanceof byte[]) {
            String ct = conn.getContentType();
            String ce = conn.getContentEncoding();
            obj = new String((byte[]) obj, (ce != null) ? ce : "ISO-8859-1");
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
        OutlookAuth oa = new OutlookAuth("86b0f61c-2e69-41df-bdbe-49ebce3f7795");
        //oa.clientId="86b0f61c-2e69-41df-bdbe-49ebce3f7795";
        //oa.clientSecret="wGPTTH123+}@ojfukoJK03=";
        String token="eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFEWDhHQ2k2SnM2U0s4MlRzRDJQYjdyY2ZpSEVtbUFkb2xfWXd4Ni03Y1VuZi1NQ255d0lqMDl2cU9SMWhxc1hHS1EzQ2wwcHpDVGdVeTE1VzhFeG5aNkd2T19kaHFLeklTYmlacFFkTTl1bnlBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoiaUJqTDFSY3F6aGl5NGZweEl4ZFpxb2hNMllrIiwia2lkIjoiaUJqTDFSY3F6aGl5NGZweEl4ZFpxb2hNMllrIn0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC82ZmNlNGJiOC0zNTAxLTQxYzktYWZjYy1kYjBmYjUxYzdlM2QvIiwiaWF0IjoxNTI1NDI5NTE4LCJuYmYiOjE1MjU0Mjk1MTgsImV4cCI6MTUyNTQzMzQxOCwiYWNyIjoiMSIsImFpbyI6IkFTUUEyLzhIQUFBQTR2NStrR2xmUE9HbG43eGFrczZ6c2FyYnhOa1kzZXFYMGhIdlRnUWZJd1E9IiwiYW1yIjpbInB3ZCJdLCJhcHBfZGlzcGxheW5hbWUiOiJ0ZXN0IG91dGxvb2sgcmVzdCIsImFwcGlkIjoiODZiMGY2MWMtMmU2OS00MWRmLWJkYmUtNDllYmNlM2Y3Nzk1IiwiYXBwaWRhY3IiOiIxIiwiZmFtaWx5X25hbWUiOiJTaWRvcm92IiwiZ2l2ZW5fbmFtZSI6IlNlcmdleSIsImlwYWRkciI6IjkxLjIxNy4yNDguMTEiLCJuYW1lIjoiU2lkb3JvdiBTZXJnZXkiLCJvaWQiOiJiMTEzMTM5NS1mOTUwLTQxYmItYmQ1Zi05NjlhYjJhZDM2ZjciLCJvbnByZW1fc2lkIjoiUy0xLTUtMjEtMjQzMDY3MTQ2Mi0yODUyOTcxNTUxLTI3OTYwMTEwNTUtMjE0NTIiLCJwbGF0ZiI6IjMiLCJwdWlkIjoiMTAwMzAwMDA4OTMxQjUzMSIsInNjcCI6Ik1haWwuUmVhZCIsInNpZ25pbl9zdGF0ZSI6WyJpbmtub3dubnR3ayIsImttc2kiXSwic3ViIjoiYmdObElSOWVoYnpqMWlPU05FZkVXRjJJZnFoeWgzQ242UXhjZ3otalp6OCIsInRpZCI6IjZmY2U0YmI4LTM1MDEtNDFjOS1hZmNjLWRiMGZiNTFjN2UzZCIsInVuaXF1ZV9uYW1lIjoic2VyZ2V5LnNpZG9yb3ZAZGlnaWEuY29tIiwidXBuIjoic2VyZ2V5LnNpZG9yb3ZAZGlnaWEuY29tIiwidXRpIjoibG1KLVFkZHFlRWUya09RRWduNEVBQSIsInZlciI6IjEuMCJ9.DQLUzOT82P_RZ5n9OV81pLC4Xl9njiy6quxa45CN3hjD6hl1TblrWZvXPF7GpK0vMgeWwaNXANhUWZoih746DVAFvRJHdicUBDSdoBKx61STLZOn-XVL99_9kBAreDKGS1SmldnjAIz-Fl5LbRGl3py5v-Hz_t-nj6BdmqUXtHgXAQOLhoydRDbBaw-nuwIuB5RiJ4OkRwi_NmBBK5zeHIFCdHWxOgoszHds6catMq2v9IUstQhX31w2_t5tb3kLKzQtFPz2iltyJgKV3vqeeAUckY6fTZpmLnuzhNsHoJv-v-mtgdUyje-xW0xSU4VL88eltftbrPW8Zg58GZD8wQ\",\"id_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImlCakwxUmNxemhpeTRmcHhJeGRacW9oTTJZayJ9.eyJhdWQiOiI4NmIwZjYxYy0yZTY5LTQxZGYtYmRiZS00OWViY2UzZjc3OTUiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNmZjZTRiYjgtMzUwMS00MWM5LWFmY2MtZGIwZmI1MWM3ZTNkL3YyLjAiLCJpYXQiOjE1MjU0Mjk1MTgsIm5iZiI6MTUyNTQyOTUxOCwiZXhwIjoxNTI1NDMzNDE4LCJhaW8iOiJBVFFBeS84SEFBQUF5K2kvd3V1VnU3TStxcEphb29OUXF0a1NsYzFqTmNLZ21vL0t4RTB0STVqd05qVk1LdzI0ejdDMDk4aUxPS3I2Iiwic3ViIjoiTENJWDFvSGpyNllfS1hHRFppQXY1NlVFenpLakNNX043S1pPc2hfUTg2YyIsInRpZCI6IjZmY2U0YmI4LTM1MDEtNDFjOS1hZmNjLWRiMGZiNTFjN2UzZCIsInV0aSI6ImxtSi1RZGRxZUVlMmtPUUVnbjRFQUEiLCJ2ZXIiOiIyLjAifQ.QZCGoZXyd0uyx5D8edH0XJ0Jn4vgHkWMcNC7s_B28xzbaArImC1mYNDWxGrjgh3gc4o6rp_TnCYelLCRWhx9enxonCLgX-C2tgbhndiyUAkuvwVUZuBaNdacqE8OnDAYum1TNt4MGiN8DSnWjTutIgK0113Q8s-hchk_Pg9jBSs_7wzgJz3moapyOaJTBCDJyEZ9OKK_PRUESy8dXKHQsuA8YU5X9VH1yhu2PF0CISnPlw8bFrOsK2G8KlnCY5b4CI-D0nlHxjyuFaQOhMo1IUC_SKVMiwfy93mH5TcutEfEdDSP_Dic0z0_ozoguMbf_oDJOH0XRIB7dcGCg_gAFA";
        String rl=oa.roomsLists(token);
    }
}
