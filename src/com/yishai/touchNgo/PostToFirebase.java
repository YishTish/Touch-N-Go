package com.yishai.touchNgo;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by mrfonedo on 12/30/14.
 * This class is dedicated for sending data to Firebase.
 * The constructor will send the path in which the service is supposed to save and response handler,
 * Which is unique for each module.
 * At this point we also send a token as a variable. Might be better to keep the token under Constants.
 */
public class PostToFirebase extends AsyncTask<JSONObject, Void, JSONObject> {


    private String path;
    private String token;
    HandleAsyncResponse responseHandler;
    JSONObject data;

    public PostToFirebase(String iPath, HandleAsyncResponse iResponseHandler, String iToken){
            path = iPath;
            responseHandler = iResponseHandler;
            token = iToken;
    }

    @Override
    protected JSONObject doInBackground(JSONObject... params) {

        data = params[0];
        Log.i("sendPackage", "json to send: " + data.toString());
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("https://resplendent-fire-842.firebaseio.com/"+path+".json");
        post.addHeader("Authorization", "Bearer "+token);
        JSONObject serverResponse=new JSONObject();


        try{
            try {
                post.setEntity(new StringEntity(data.toString()));

                post.setHeader("Accept-Charset", HTTP.UTF_8);
                post.setHeader(HTTP.CONTENT_TYPE,"application/json; charset=utf-8");
                HttpResponse response = client.execute(post);
                Log.i("registraion data",response.getStatusLine().toString());
                int responseCode = response.getStatusLine().getStatusCode();
                serverResponse.put("code", responseCode);
                if(responseCode==200){
                    Log.i("sendPackage","Sent data. response code 200");
                    InputStream input = response.getEntity().getContent();
                    InputStreamReader incoming = new InputStreamReader(input);
                    BufferedReader reader = new BufferedReader(incoming);
                    StringBuilder strB = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null){
                        strB.append(line);
                    }
                    serverResponse.put("text",  strB.toString());
                    serverResponse.put("status", "success");
                    Log.i("sendPackage","serverResponse: "+strB.toString());
                }
                else{
                    Log.i("sendPAckage","failed to send package. Server response: "+responseCode);
                    serverResponse.put("text", "Check-in failure.");
                    serverResponse.put("status", "failure");
                }
            } catch (ClientProtocolException e) {
                Log.e("Client Error","Client protocol error: "+ e.getMessage());
                serverResponse.put("text","Check-in failure, clientProtocol error");
                serverResponse.put("status", "failure");
            } catch (IOException e) {
                Log.e("IO Error","IO error: "+ e.getMessage());
                serverResponse.put("text","Check-in failure. Input/Output error");
                serverResponse.put("status", "failure");
            }
            catch (Exception e) {
                Log.e("Error", "General error: "+ e.getMessage());
                serverResponse.put("text","Check-in failure - General exception");
                serverResponse.put("status", "failure");
            }
        }
        catch (JSONException je){
            Log.e("JSON Error", je.getMessage());
        }
        finally{
            Log.i("send Reg data",serverResponse.toString());
            return serverResponse;
        }
    }

    @Override
    protected void onPostExecute(JSONObject response) {
        if(response==null){
            Log.i("Finish registration","response is null");
        }
        responseHandler.processFinish(response);
    }
}
