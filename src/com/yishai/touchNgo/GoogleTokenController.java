package com.yishai.touchNgo;

import java.io.IOException;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class GoogleTokenController {
	
	private String accountName;
	private Activity activity;
	private String token;

	
	public GoogleTokenController(Activity activity) {
			this.activity = activity;
			token="";
	}
	
	//Currently not in use - needed for handling tokens to communicate with Google
	public String getAccountName() {
	    AccountManager mAccountManager = AccountManager.get(activity);
	    Account[] accounts = mAccountManager.getAccountsByType(
	    		GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
	    Log.e("Getting Account", "Number of accounts found: "+accounts.length);
	   if(accounts.length == 0){
		   Toast.makeText(activity, "This application requires a Google account. None is associated with your phone",Toast.LENGTH_LONG).show();
		   return null;
	   	}
	   accountName = accounts[0].name;
	    return  accounts[0].name;
	 }

	
	//Manage security tokens for communicating with Google
	public void fetchToken(){
			if(accountName==null || "".equals(accountName)){
				getAccountName();
			}
			
			//accountName = "yishai@simba-web.co.il";

			
			AsyncTask<String,Void, String> task = new AsyncTask<String, Void, String>() {

				@Override
				protected String doInBackground(String... account) {
					String scope="oauth2:https://www.googleapis.com/auth/drive.scripts";
					
					Log.e("Getting Token","checking for account "+accountName);
					try{
						try {
								token = GoogleAuthUtil.getToken(activity, accountName, scope);
								Log.i("Got token",token);
								return token;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Log.e("oauth process",e.getMessage());
							return null;
						}
					}
					catch(UserRecoverableAuthException e){
						Log.e("Getting Token", "user recoverable exception");
						Intent intent = e.getIntent();
						activity.startActivityForResult(intent,1);
				         return null;			
					}
					catch(GoogleAuthException e){
						Log.e("Getting Token", "GAuth Exception");
						Log.e("Token error",e.getMessage());
						return null;
					}
					catch(Exception e){
						Log.e("Getting Token", "General Exception: "+e.getMessage());
						return null;
					}
				}
				
				
				@Override
				protected void onPostExecute(String result) {
					// TODO Auto-generated method stub
					super.onPostExecute(result);
					Log.e("GToken postExecute","Got token: "+result);
					token = result;
				}
				
				
			}; 
			
			
			task.execute(accountName);
			
			
		}
		
		public String getToken() {
			if(token == null || "".equals(token)){
				Log.e("GTOken controller","creating new token");
				fetchToken();
			}
			else{
				Log.e("GTOken controller","I have a token: "+token);
			}
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public void setAccountName(String accountName) {
			this.accountName = accountName;
		}
	
		

}
