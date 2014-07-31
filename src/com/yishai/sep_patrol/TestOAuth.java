package com.yishai.sep_patrol;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import android.util.Log;

public class TestOAuth extends ProcessCheckIn{

	
	public TestOAuth() {
		super(new ArrayList<String>());
		token = "";
	}

	final static String LOGTAG = "TestOAuth";
	private String token;
	
	public void setToken(String t){
		token = t;
	}
	
	@Override
	protected String doInBackground(String... params) {
		HttpClient client = new DefaultHttpClient();
		//HttpGet get = new HttpGet(Constants.TEMP_GSHEET);
		//HttpParams getParams = new BasicHttpParams();
		
		//getParams.setParameter("access_token", token);
		//get.setParams(getParams);
		//Log.e(LOGTAG,get.getRequestLine().getUri());
		String serverResponse="";
		try {
			 URL url = new URL(Constants.TEMP_GSHEET +"?access_token="+ token);
			 HttpURLConnection con = (HttpURLConnection) url.openConnection();
			InputStream is = con.getInputStream();
			int responseCode = con.getResponseCode();
			//HttpResponse response = client.execute(get);
			//int responseCode = response.getStatusLine().getStatusCode();
			if(responseCode==200){
				StringBuilder sb = new StringBuilder();
				byte[] line = new byte[1024];
				while(is.read(line, 0, line.length)>=0){
					sb.append(line);
				}
				serverResponse = sb.toString();
				if(serverResponse.length() == 0){
					serverResponse = "No response";
				}
			//	HttpEntity responseEntity = response.getEntity();
			//	if(responseEntity.getContentLength()==0){
			//		serverResponse  = "Request returned empty";
			  //	}
				/*else{
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
				}*/
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
