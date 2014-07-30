package com.yishai.sep_patrol;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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
			
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(Constants.GSHEET_URL);
			
			JSONObject serverResponse=new JSONObject();
			
			
			try{
				try {
					Log.e("Sending checkin","json = "+json.toString());
					post.setEntity(new StringEntity(json.toString()));
					post.setHeader("Accept","application/json");
					post.setHeader("Content-type","application/json");
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
