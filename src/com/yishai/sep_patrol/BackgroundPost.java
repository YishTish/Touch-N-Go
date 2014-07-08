package com.yishai.sep_patrol;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Log;

public class BackgroundPost extends AsyncTask<String, Void, String> {

	
	private String response = "";
	
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	@Override
	protected String doInBackground(String... params) {
		
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(Constants.GSHEET_URL);
		String serverResponse = "";
		
		
		List<NameValuePair> args =  new ArrayList<NameValuePair>();
		args.add(new BasicNameValuePair("Name" ,"Yishai"));
		args.add(new BasicNameValuePair("Location" ,"work"));
		args.add(new BasicNameValuePair("Comments" ,"Nothing special"));
		Log.e("got here","Got here");
		try {
			post.setEntity(new UrlEncodedFormEntity(args));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Log.e("Error", "Encoding error:", e);
			serverResponse = e.getMessage();
		}
		try {
			HttpResponse response = client.execute(post);
			Header[] headers = response.getAllHeaders();
			for(Header header : headers){
				Log.e(header.getName(), header.getValue());
			}
			serverResponse = Integer.toString(response.getStatusLine().getStatusCode());

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.e("Client Error","Client protocol error:", e);
			e.printStackTrace();
			serverResponse = e.getMessage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("IO Error","IO error:", e);
			e.printStackTrace();
			serverResponse = e.getMessage();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("Error", "General error:", e);
			e.printStackTrace();
			serverResponse = e.getMessage();
		}
		
		return serverResponse;
	}
	
	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		this.response = result;
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		this.response="";
		
	}

}
