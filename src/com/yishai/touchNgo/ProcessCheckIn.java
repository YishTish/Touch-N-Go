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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessCheckIn extends AsyncTask<String, Void, JSONObject> {
	
		
		AsyncResponseHandler delegate = null;
		JSONObject json = new JSONObject();
		
		public void setDelegate(AsyncResponseHandler delegate) {
			this.delegate = delegate;
		}
		
		public ProcessCheckIn(JSONObject params) {
			json = params;
		}

		@Override
		protected JSONObject doInBackground(String... params) {

			
			JSONObject lastResponse = null;
			try{
	            String token = "";
	            if(params[0]!=null && !"".equals(params[0])){
	                token = params[0];
	            }
	
			        try {
	                    Log.i("Sending checkin", "json = " + json.toString());
	
	                    JSONArray activities = json.getJSONArray("check-in");
	                    int activitiesLength = activities.length();
	                    Log.i("Check-in process","Activities length: "+activitiesLength);
	                    for (int i = 0; i < activitiesLength; i++) {
	                        JSONObject activity = (JSONObject) activities.get(i);
	                        if (activity != null) {
	                            JSONObject serverResponse = sendPackage(token, activity);
	                            String responseStatus = serverResponse.getString("status");
	                            if(responseStatus.equals("failure")){
		                    		return serverResponse;
		                    	}
		                    	else{
		                    		lastResponse = serverResponse;
		                    	}
	                        }
	                    }
	
	                } catch (JSONException je) {
	                    Log.e("Send check-in  JSON failure", je.getMessage().toString());
	                }
			    Log.i("Check-in process","last response: "+lastResponse.toString());
			    
			}
			catch(Exception e){
				Log.e("check-in process","General exception: "+e.getMessage());
			}
			return lastResponse;
            
         }

        private JSONObject sendPackage(String token,JSONObject packageContent){

        	Log.i("sendPackage", "json to send: "+packageContent);
            HttpClient client = new DefaultHttpClient();
            //HttpPost post = new HttpPost(Constants.GSHEET_URL);
            HttpPost post = new HttpPost("https://resplendent-fire-842.firebaseio.com/activities.json");
            post.addHeader("Authorization", "Bearer "+token);
            //Header header = post.addHeader(new HttpHead());

            JSONObject serverResponse=new JSONObject();


            try{
                try {
                    post.setEntity(new StringEntity(packageContent.toString()));

                    post.setHeader("Accept-Charset", HTTP.UTF_8);
                    post.setHeader(HTTP.CONTENT_TYPE,"application/json; charset=utf-8");
                    HttpResponse response = client.execute(post);
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
            return serverResponse;
        }
		
		@Override
		protected void onPostExecute(JSONObject result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		
		delegate.processFinish(result);
		}
}