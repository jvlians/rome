package com.ippon.rome;


import com.ippon.rome.util.ParameterStringBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class HyperLedgerApi {
    private String apiUrl;

    public HyperLedgerApi(String apiUrl){
        this.apiUrl = apiUrl; //http://example.com
    }

    public void createUser(String userId) throws IOException {
        URL url = new URL(apiUrl + "/api/User");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("data", String.format("{'$class':'org.ippon.rome.User','userId':'%s'}",userId));

        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
        out.flush();
        out.close();
        con.disconnect();
    }
    public void shareWithUser(String filePermissionId, String message, String recipientId, String ownerId) throws IOException {
        URL url = new URL(apiUrl + "/api/FilePermission");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("data", String.format("{'$class':'org.ippon.rome.FilePermission'," +
                        "'filePermissionId': '%s','encryptedReference': '%s'," +
                        "'sharedWith': '%s','owner': '%s'}",
                        filePermissionId,message,recipientId,ownerId));

        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
        out.flush();
        out.close();
        con.disconnect();
    }
    public String getFilesSharedWithUser(String userId) throws IOException {
        URL url = new URL(apiUrl + "/api/queries/getFilesSharedWithUser");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        return content.toString();
    }
}
