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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class CheckInLocation extends Activity {

	TextView nameTV;
	TextView locationTV;
	TextView commentsTV;
	
	String token;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
		
		nameTV = (TextView)findViewById(R.id.NameTB);
		locationTV = (TextView)findViewById(R.id.LocationTB);
		commentsTV = (TextView)findViewById(R.id.CommentsTB);


		Button button = (Button)findViewById(R.id.SubmitButton);
		button.setOnClickListener(submitCheckIn);
		
		
	}
	
	private OnClickListener submitCheckIn = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String[] accountNames = getAccountNames();
			for(String account : accountNames){
				Log.e("Checking GPlay accounts", "Found account: "+account);
			}
			//getTokenInAsyncTask(accountNames[0]);
			//Log.i("button clicked","end of event");
			/*
			BackgroundPost postScript = new BackgroundPost();
			postScript.execute(Constants.GSHEET_URL);
			int i = 0;
			while(i++ < 5){
				try {
					Thread.sleep(1000);
					String response = postScript.getResponse();
					if (!"".equals(response)){
						displayPostResponse(response);
						break;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Log.e("Posting to GSHeet",e.getMessage());
				}
				
				//HttpPost post = new HttpPost("https://www.google.com");
				
				}
			if(i==5){
				displayPostResponse("No response has been received from the server");
			}
			*/
			String response = checkIn(token);
			Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
			
	};
	
	};
	public void displayPostResponse(String response){
		Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();

	}
	
	
	private String[] getAccountNames() {
	    AccountManager mAccountManager = AccountManager.get(getApplicationContext());
	    
	    Account[] accounts = mAccountManager.getAccountsByType(
	    		GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
	    
	    
	    String[] names = new String[accounts.length];
	    for (int i = 0; i < names.length; i++) {
	        names[i] = accounts[i].name;
	    }
	    return names;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode==RESULT_CANCELED){
			Log.e("External activity returned","returned after external activity was cancelled.");
			return;
		}
		if(requestCode==1){
			String[] accounts = getAccountNames();
			Log.i("External activity returned","Returned from getting auth token");
			getTokenInAsyncTask(accounts[0]);
		}
	}
	
	private void getTokenInAsyncTask(String account){
		AsyncTask<String,Void, Object> task = new AsyncTask<String, Void, Object>() {

			
			@Override
			protected Void doInBackground(String... account) {
				// TODO Auto-generated method stub
				manageToken(account[0]);
				return null;
			}
			
			
		}; 
		task.execute(account);
	}
	
	private String checkIn(final String token){
		
		String response = "";
		AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>(){
			
			@Override
			protected String doInBackground(String... params) {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(Constants.GSHEET_URL);
				HttpGet get = new HttpGet(Constants.GSHEET_URL);
				try {
					client.execute(get);
					Log.e("123","333");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Log.e("after GET", "111");
				String serverResponse="";
				
				List<NameValuePair> args =  new ArrayList<NameValuePair>();
				args.add(new BasicNameValuePair("Name" ,nameTV.getText().toString()));
				args.add(new BasicNameValuePair("Location" ,locationTV.getText().toString()));
				args.add(new BasicNameValuePair("Comments" ,commentsTV.getText().toString()));
				
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
					Log.e("handling Post",response.getStatusLine().toString());
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
				Log.e("checking-in",serverResponse);
				return serverResponse;
			}
		};
		task.execute("",null,response);
		return response;
	}


	protected void manageToken(String account) {
		String scope="oauth2:https://www.googleapis.com/auth/drive.scripts";
		
		Log.e("getting token","checking for account "+account);
		try{
			try {
					token = GoogleAuthUtil.getToken(getApplicationContext(), account, scope);
					Log.i("Got token",token);
					return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("oauth process",e.getMessage());
				return;
			}
		}
		catch(UserRecoverableAuthException e){
			Intent intent = e.getIntent();
			this.startActivityForResult(intent,1);
	         return;			
		}
		catch(GoogleAuthException e){
			Log.e("oauth process", e.getMessage());
			return;
		}
	}

	
}
