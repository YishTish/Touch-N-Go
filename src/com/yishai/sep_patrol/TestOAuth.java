package com.yishai.sep_patrol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class TestOAuth extends ProcessCheckIn{

	
	public TestOAuth() {
		super(new ArrayList<String>());
	}

	final static String LOGTAG = "TestOAuth"; 
	
	@Override
	protected String doInBackground(String... params) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(Constants.TEMP_GSHEET);
		
		String serverResponse="";
		try {
			HttpResponse response = client.execute(get);
			int responseCode = response.getStatusLine().getStatusCode();
			if(responseCode==200){
				HttpEntity responseEntity = response.getEntity();
				if(responseEntity.getContentLength()==0){
					serverResponse  = "Request returned empty";
				}
				else{
					StringBuilder sb  = new StringBuilder();
					try{
						BufferedReader br = new BufferedReader(new InputStreamReader(responseEntity.getContent()),65728);
						String line = null;
						while ((line = br.readLine()) != null){
							sb.append(line);
						}
						serverResponse = sb.toString();
					}
					catch(IOException ioe){
						Log.e(LOGTAG, "io exception when reading response: " +ioe.getMessage());
					}
					catch(Exception e){
						Log.e(LOGTAG, "exception when reading response: " +e.getMessage());
					}
				}
			}
			else{
				serverResponse =  "Request failed. Returned code: "+responseCode;
			}
		}
		catch (ClientProtocolException e) {
			Log.e("Client Error","Client protocol error: "+ e.getMessage());
			serverResponse = "Check-in failure, clientProtocol error";
		} catch (IOException e) {
			Log.e("IO Error","IO error: "+ e.getMessage());
			serverResponse = "Check-in failure. Input/Output error";
		}
		catch (Exception e) {
			Log.e("Error", "General error: "+ e.getMessage());
			e.printStackTrace();
			serverResponse = "Check-in failure";
		}
		return serverResponse;
	}
	
}
