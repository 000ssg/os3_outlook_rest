package org.openshift.quickstarts.undertow.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OutlookAuth {

	    String authUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
	    String tokenUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
	    String redirect = "https://openjdk-app-ddd.1d35.starter-us-east-1.openshiftapps.com/tokenized";
	    String clientId;
	    String responseType = "code";
	    String scope = "openid+Mail.Read";
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

	    public boolean doAuth() throws IOException {
	        URL url = new URL(
	                authUrl
	                + "?client_id=" + URLEncoder.encode(clientId, "UTF-8")
	                + "&redirect_uri=" + URLEncoder.encode(redirect, "UTF-8")
	                + "&response_type=" + responseType
	                + "&scope=" + scope
	        );

	        System.out.println("doAuth: "+url);
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
	        }else{
	            System.out.println(content);
	        }

	        return false;
	    }

	    public String requestToken() throws IOException {
	        return null;
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
	        OutlookAuth oa = new OutlookAuth("sergey.sidorov@digia.com");
	        if (oa.doAuth()) {
	            System.out.println("Outlook auth call succeeded");
	        } else {
	            System.out.println("Outlook auth call failed");
	        }
	    }
	}

