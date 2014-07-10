package com.yishai.sep_patrol;




import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

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


public class CheckInLocation extends Activity implements HandleAsyncResponse {

	TextView nameTV;
	TextView locationTV;
	TextView commentsTV;
	
	String token;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		JSONObject userData = getUserRegistration();
		if(userData==null){
			Intent registrationIntent = new Intent(this, RegisterActivity.class);
			startActivity(registrationIntent);
		}
		
		setContentView(R.layout.activity_main);	
		
		nameTV = (TextView)findViewById(R.id.NameTB);
		locationTV = (TextView)findViewById(R.id.LocationTB);
		commentsTV = (TextView)findViewById(R.id.CommentsTB);


		Button button = (Button)findViewById(R.id.SubmitButton);
		button.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String[] accountNames = getAccountNames();
				for(String account : accountNames){
					Log.e("Checking GPlay accounts", "Found account: "+account);
				}
				
				checkIn(CheckInLocation.this);
				clearTB();
			}
		});
	}
	
	private void clearTB() {
		nameTV.setText("");
		locationTV.setText("");
		commentsTV.setText("");
		
	}
	
	//Code to run when Handling the response from the check-in Async process.
	@Override
	public void processFinish(String response) {
			//Clear the textboxes
			clearTB();
			Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();

	}
	
	//Currently not in use - needed for handling tokens to communicate with Google
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
	
	//Currently not in use - When retrieving a token from Google, an external intent is 
	//launched, and this method is activated when that intent is completed
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
	
	
	//Currently not in use - manage security tokens for communicating with Google
	private void getTokenInAsyncTask(String account){
		AsyncTask<String,Void, Object> task = new AsyncTask<String, Void, Object>() {

			
			@Override
			protected Void doInBackground(String... account) {
				manageToken(account[0]);
				return null;
			}
			
			
		}; 
		task.execute(account);
	}
	
	private void checkIn(HandleAsyncResponse handler){
		
		List<String> params = new ArrayList<String>();
		params.add(nameTV.getText().toString());
		params.add(locationTV.getText().toString());
		params.add(commentsTV.getText().toString());
		
		ProcessCheckIn task = new ProcessCheckIn(params);
		task.setDelegate(handler);
		task.execute();
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

	
	private JSONObject getUserRegistration(){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(Constants.USER_DATA_FILE));
			String line;
			StringBuilder text = new StringBuilder();
			while((line = br.readLine())!=null){
				text.append(line);
			}
			JSONObject json = new JSONObject(text.toString());
			return json;
		} catch (FileNotFoundException e) {
			Log.e("Reading user data","File doesn't exist");
		}
		catch (JSONException e1) {
			Log.e("Reading user data", "JSON failure");
		}
		catch ( IOException e1) {
			Log.e("Reading user data", "IO failure");
		}
		finally{
			try {
				if(null!=br)
					br.close();
			}
			catch(IOException e){
				Log.e("reading user data","Couldn't close user data file");
			}
			
		}
		return null;
	}
	
		
}