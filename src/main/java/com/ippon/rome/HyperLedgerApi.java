package com.ippon.rome;


import com.ippon.rome.util.ParameterStringBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class HyperLedgerApi {
    private String apiUrl;

    public static void main(String[] args) throws IOException, UnirestException {
        HyperLedgerApi api = new HyperLedgerApi("http://184.172.247.54:31090");
        System.out.println(api.getFilesSharedWithUser("testUser"));
        Unirest.shutdown();
    }

    public HyperLedgerApi(String apiUrl){
        this.apiUrl = apiUrl; //http://example.com
    }

    public HttpResponse<JsonNode> createUser(String userId) throws IOException {
        String url = apiUrl + "/api/User";
        String body = String.format("{'$class':'org.ippon.rome.User','userId':'%s'}",userId);
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post(url)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(body)
                    .asJson();
            return jsonResponse;
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }
    public HttpResponse<JsonNode> shareWithUser(String filePermissionId, String message, String recipientId, String ownerId) throws IOException {
        String url = apiUrl + "/api/FilePermission";
        String body = String.format("{'$class':'org.ippon.rome.FilePermission'," +
                        "'filePermissionId': '%s','encryptedReference': '%s'," +
                        "'sharedWith': '%s','owner': '%s'}",
                        filePermissionId,message,recipientId,ownerId);
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post(url)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(body)
                    .asJson();
            return jsonResponse;
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }
    public JsonNode getFilesSharedWithUser(String userId) throws IOException {
        String url = apiUrl + "/api/queries/getFilesSharedWithUser";
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get(url)
                    .queryString("sharedWith", userId)
                    .asJson();
            return jsonResponse.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void shutdown(){
        try {
            Unirest.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
