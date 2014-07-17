package com.yishai.sep_patrol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class ProcessCheckIn extends AsyncTask<String, Void, String> {
	
		
		HandleAsyncResponse delegate = null;
		List<String> params;
		
		public void setDelegate(HandleAsyncResponse delegate) {
			this.delegate = delegate;
		}
		
		public ProcessCheckIn(List<String> params) {
			this.params = params;
		}

		private List<NameValuePair> collectParams(){
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			if(params.get(0)!=null && !"".equals(params.get(0)))
				list.add(new BasicNameValuePair("Name" ,params.get(0)));
			if(params.get(1)!=null && !"".equals(params.get(1)))
				list.add(new BasicNameValuePair("Location" ,params.get(1)));
			if(params.get(2)!=null && !"".equals(params.get(2)))
				list.add(new BasicNameValuePair("Comments" ,params.get(2)));
			if(params.get(3)!=null && !"".equals(params.get(3)))
				list.add(new BasicNameValuePair("Timestamp" ,params.get(3)));
			return list;
		}
		
		@Override
		protected String doInBackground(String... params) {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(Constants.GSHEET_URL);
			
			String serverResponse="";
			List<NameValuePair> args =  collectParams();
			
			try {
				post.setEntity(new UrlEncodedFormEntity(args));
				HttpResponse response = client.execute(post);
				int responseCode = response.getStatusLine().getStatusCode();
				if(responseCode==200)
					serverResponse = "Check-in Succeed";
				else
					serverResponse = "Check-in failure. Server response code: "+responseCode;
			} catch (ClientProtocolException e) {
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
		
		@Override
		protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		delegate.processFinish(result);
		}
}
