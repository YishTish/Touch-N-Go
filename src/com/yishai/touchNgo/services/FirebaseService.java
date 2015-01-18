package com.yishai.touchNgo.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.yishai.touchNgo.AsyncResponseHandler;
import com.yishai.touchNgo.Constants;
import com.yishai.touchNgo.model.User;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mrfonedo on 12/30/14.
 * This class is dedicated for sending data to Firebase.
 * The constructor will send the path in which the service is supposed to save and response handler,
 * Which is unique for each module.
 * At this point we also send a token as a variable. Might be better to keep the token under Constants.
 */
public class FirebaseService {


    private String path;
    private String token;
    private Context context;
    AsyncResponseHandler responseHandler;
    Firebase firebaseRef;
    JSONObject data;



    public static final String LOGCAT = "FirebaseService";



    public FirebaseService (Context context, AsyncResponseHandler asyncHandler){
        this.context = context;
        this.responseHandler = asyncHandler;
        Firebase.setAndroidContext(context);
         firebaseRef = new Firebase(Constants.FIREBASE_INSTANCE);
    }

    /*
    public FirebaseService(String iPath, AsyncResponseHandler iResponseHandler, Context context){
            path = iPath;
            responseHandler = iResponseHandler;
            this.context = context;
    }*/

    public void register(User user){

        final User tempUser = user;
        firebaseRef.createUser(user.getEmail(), user.getPassword(), new Firebase.ResultHandler(){


            @Override
            public void onSuccess() {
                Log.i(LOGCAT, "Registered to FireBase successfully");
                JSONObject response = new JSONObject();
                try{
                    response.put("stage","registration");
                    response.put("user",tempUser.toJson());
                }
                catch(JSONException je){
                    Log.e(LOGCAT,"Failure to respond from regisration"+ je.getMessage());
                }
                responseHandler.processFinish(response);
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                Log.i(LOGCAT, "Failed to register to Firebase:" + firebaseError.getMessage());
                JSONObject response = new JSONObject();
                try{
                    response.put("stage","registration");
                    response.put("status","failure");
                    response.put("reason",firebaseError.getMessage());
                }
                catch(JSONException je){
                    Log.e(LOGCAT, "Failed to create failing resposne: "+je.getMessage());
                }
                responseHandler.processFinish(response);
            }
        });
    }

    public void login(User user){
        firebaseRef.authWithPassword(user.getEmail(), user.getPassword(), new Firebase.AuthResultHandler() {

            @Override
            public void onAuthenticated(AuthData authData) {
                Log.i(LOGCAT, "Logged in successfully");

            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.e(LOGCAT, "Failed to login to Firebase: " + firebaseError.getMessage());
            }
        });
    }

    public void push(String tree, String key, Object data){
        Firebase tempRef = firebaseRef.child(tree);
        Map<String, Object> dataToPush = new HashMap<>();
        if("".equals(key)) {
            tempRef.push().setValue(data, completionListener);
        }
        else{
            dataToPush.put(key, data);
            tempRef.setValue(dataToPush, completionListener);
        }
    }

    //Firebase listener for data publishing
    private Firebase.CompletionListener completionListener = new Firebase.CompletionListener(){

            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                try{
                    JSONObject response = new JSONObject();
                    response.put("stage", "push_data");
                    if(firebaseError==null){
                        response.put("status","success");
                        response.put("key",firebase.getKey());
                    }
                    else{
                        response.put("status","failure");
                        response.put("reason",firebaseError);
                    }
                    responseHandler.processFinish(response);
                }
                catch(JSONException je){
                    Log.e(LOGCAT, "Failed to add user data to Firebase: "+je.getMessage());
                }
            }
        };


/*
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
    */

}
