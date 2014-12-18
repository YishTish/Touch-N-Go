package com.yishai.touchNgo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebViewClient;

public class ProcessCheckIn extends AsyncTask<String, Void, JSONObject> {
	
		
		HandleAsyncResponse delegate = null;
		JSONObject json = new JSONObject();
		
		public void setDelegate(HandleAsyncResponse delegate) {
			this.delegate = delegate;
		}
		
		public ProcessCheckIn(JSONObject params) {
			json = params;
		}
		
	/*
		private String jsonToString() {
			
			String string = json.toString();
			return string;
		}
	*/
		@Override
		protected JSONObject doInBackground(String... params) {

            String token = "";
            if(params[0]!=null && !"".equals(params[0])){
                token = params[0];
            }

			HttpClient client = new DefaultHttpClient();
			//HttpPost post = new HttpPost(Constants.GSHEET_URL);
            HttpPost post = new HttpPost("https://resplendent-fire-842.firebaseio.com/activities.json");
            post.addHeader("Authorization", "Bearer "+token);
            //Header header = post.addHeader(new HttpHead());
			
			JSONObject serverResponse=new JSONObject();
			
			
			try{
                try {
					Log.e("Sending checkin","json = "+json.toString());
					//post.setEntity(new StringEntity(json.toString()));
                    post.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
					//post.setHeader("Accept","application/json");
                    post.setHeader("Accept-Charset", HTTP.UTF_8);
					post.setHeader(HTTP.CONTENT_TYPE,"application/json; charset=utf-8");
                    HttpResponse response = client.execute(post);
					int responseCode = response.getStatusLine().getStatusCode();
					serverResponse.put("code", responseCode);
					if(responseCode==200){
						InputStream input = response.getEntity().getContent();
						InputStreamReader incoming = new InputStreamReader(input);
						BufferedReader reader = new BufferedReader(incoming);
						StringBuilder strB = new StringBuilder();
						String line;
						while((line = reader.readLine()) != null){
							strB.append(line);
						}
						serverResponse.put("text",  strB.toString());
					}
					else
						serverResponse.put("text", "Check-in failure.");
				} catch (ClientProtocolException e) {
					Log.e("Client Error","Client protocol error: "+ e.getMessage());
					serverResponse.put("text","Check-in failure, clientProtocol error");
				} catch (IOException e) {
					Log.e("IO Error","IO error: "+ e.getMessage());
					serverResponse.put("text","Check-in failure. Input/Output error");
				}
				catch (Exception e) {
					Log.e("Error", "General error: "+ e.getMessage());
					serverResponse.put("text","Check-in failure - General exception");
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