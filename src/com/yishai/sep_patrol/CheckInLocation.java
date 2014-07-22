package com.yishai.sep_patrol;




import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.zxing.client.android.CaptureActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


//import com.google.
//import com.google.zxing

public class CheckInLocation extends Activity implements HandleAsyncResponse {

	private final int GOOGLE_ACTIVITY=1;
	private final int SCAN_ACTIVITY=2;
	
	
	TextView commentsTV;
	Button launchQrBtn;
	Button submitBtn;
	
	
	String userName="";
	String locationCode ="";
	String comments = "";
	//The LocationController will be used to get the time and position of the user.
	LocationController locationController;
	
	
	String token;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadSavedVariables(savedInstanceState);
		
		setContentView(R.layout.activity_main);

		TextView descriptionView = (TextView)findViewById(R.id.descView);
		descriptionView.setText("Hello, "+userName);
		
		commentsTV = (TextView)findViewById(R.id.CommentsTB);

		//Get user details, either from an existing file, or from registration process (which creates the file)
		manageUserData();

		launchQrBtn = (Button)findViewById(R.id.scnButton);
		launchQrBtn.setOnClickListener(new launchQrListener());
		
		
		submitBtn = (Button)findViewById(R.id.SubmitButton);
		//submitBtn.setVisibility(Button.INVISIBLE);
		submitBtn.setOnClickListener(new tempListener());
		
		commentsTV.setVisibility(TextView.INVISIBLE);
		
		LocationManager locManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		locationController = new LocationController(locManager);
	}
	
	private void loadSavedVariables(Bundle savedInstance){
		if(savedInstance==null)
			return;
		Log.e("loading saved variables","Starting load");
		String userName = savedInstance.getString("userName");
		Log.e("loading saved variables","received username");
		if(userName!=null && !"".equals(userName))
			this.userName = userName;
		Log.e("loading saved variables","loaded username");
		String locationCode = savedInstance.getString("locationCode");
		if(locationCode!=null && !"".equals(locationCode))
			this.locationCode = locationCode;
		Log.e("loading saved variables","loaded locationCode");
		String comments = savedInstance.getString("comments");
		if(comments!=null && !"".equals(comments))
			this.comments = comments;
		Log.e("loading saved variables","loaded comments");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(null != this.userName && !"".equals(userName)){
			TextView descriptionView = (TextView)findViewById(R.id.descView);
			descriptionView.setText("Hello "+userName);
		}
		//changeButtonVisibility();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.general_menu, menu);
		return true;
		//return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String title = item.getTitle().toString();
		if("Settings".equals(title)){
			registerUser();
		}
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}
	private class launchQrListener implements OnClickListener{
			@Override
			public void onClick(View v) {
				Toast.makeText(CheckInLocation.this,"Launching QR scanner", Toast.LENGTH_SHORT).show();
				Intent qrIntent = new Intent(getApplicationContext(),CaptureActivity.class);
				qrIntent.setAction("com.google.zxing.client.android.SCAN");
				qrIntent.putExtra("SAVE_HISTORY",false);
				startActivityForResult(qrIntent, 2);
				launchQrBtn.setText("Please wait");
				launchQrBtn.setEnabled(false);
			}
	}
	
	private class submitListener implements OnClickListener{
		
		@Override
		public void onClick(View v) {
			locationController.pingProvider();
			long currentTS = locationController.getTimestampSeconds();
			Log.i("Checking in","currentTS = "+currentTS);
			submitBtn.setText("sending check-in data...");
			submitBtn.setEnabled(false);
			List<String> params = new ArrayList<String>();
			params.add(CheckInLocation.this.userName);
			params.add(locationCode);
			params.add(commentsTV.getText().toString());
			params.add(Long.toString(currentTS));
			
			//@TODO: Save parameters to json document
			//@TODO: Send json document instead of raw variables
			ProcessCheckIn task = new ProcessCheckIn(params);
			task.setDelegate(CheckInLocation.this);
			task.execute();
			//checkIn(CheckInLocation.this);
		}
	}
	
private class tempListener implements OnClickListener{
		
		@Override
		public void onClick(View v) {
			//@TODO: Save parameters to json document
			//@TODO: Send json document instead of raw variables
			TestOAuth task = new TestOAuth();
			task.setDelegate(CheckInLocation.this);
			task.execute();
			//checkIn(CheckInLocation.this);
		}
	}
	
private class tempListener implements OnClickListener{
		
		@Override
		public void onClick(View v) {
			//@TODO: Save parameters to json document
			//@TODO: Send json document instead of raw variables
			TestOAuth task = new TestOAuth();
			task.setDelegate(CheckInLocation.this);
			task.execute();
			//checkIn(CheckInLocation.this);
		}
	}
	
	private boolean haveNetworkConnection() {
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;

	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getType() == ConnectivityManager.TYPE_WIFI)
	            if (ni.isConnected())
	                return true;
	        if (ni.getType() == ConnectivityManager.TYPE_MOBILE)
	            if (ni.isConnected())
	                return true;
	    }
	    return false;
	}
	
	private long getTimeStamp(){
		//@TODO: Get the time from the network. if no response from network, get the time
		//from the device clock. 
		//If returning time from device, return as negative number. Otherwise, positive.
		
		if(!haveNetworkConnection())
			return (-System.currentTimeMillis());
		
	        
	        try{
	            //Make the Http connection so we can retrieve the time
	            HttpClient httpclient = new DefaultHttpClient();
	            // I am using yahoos api to get the time
	            HttpResponse response = httpclient.execute(new
	            HttpGet("http://developer.yahooapis.com/TimeService/V1/getTime?appid=YahooDemo"));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                // The response is an xml file and i have stored it in a string
	                String responseString = out.toString();
	                Log.d("Response", responseString);
	                //We have to parse the xml file using any parser, but since i have to 
	                //take just one value i have deviced a shortcut to retrieve it
	                int x = responseString.indexOf("<Timestamp>");
	                int y = responseString.indexOf("</Timestamp>");
	                //I am using the x + "<Timestamp>" because x alone gives only the start value
	                Log.d("Response", responseString.substring(x + "<Timestamp>".length(),y) );
	                String timestamp =  responseString.substring(x + "<Timestamp>".length(),y);
	                // The time returned is in UNIX format so i need to multiply it by 1000 to use it
	                return Long.parseLong(timestamp);
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	                
	            }
	        }catch (Exception e) {
	        	return (-System.currentTimeMillis());
	        } 
		}
	
	private void manageUserData(){
		JSONObject userData = getUserRegistration();
		if(userData==null){
			registerUser();
		}
		else{
			try {
				 userName = userData.getString("first_name") + " "+userData.getString("last_name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.e("Fetching user data","Couldn't read user data: "+e.getMessage());
			}
		}
		
		
	}
	
	public void registerUser(){
		Intent registrationIntent = new Intent(CheckInLocation.this, RegisterActivity.class);
		startActivity(registrationIntent);
	}
	
	//Code to run when Handling the response from the check-in Async process.
	@Override
	public void processFinish(String response) {
			//Clear the textboxes
			commentsTV.setText("");
			comments = locationCode = "";
			
			Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
			changeButtonVisibility();
			submitBtn.setText(R.string.CheckIN);

	}
	
	//Currently not in use - needed for handling tokens to communicate with Google
	private String getAccountName() {
	    AccountManager mAccountManager = AccountManager.get(getApplicationContext());
	    
	    Account[] accounts = mAccountManager.getAccountsByType(
	    		GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
	   if(accounts.length == 0){
		   Toast.makeText(this, "This application requires a Google account. None is associated with your phone",Toast.LENGTH_LONG).show();
		   return null;
	   	}
	    return  accounts[0].name;
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
		if(requestCode==GOOGLE_ACTIVITY){
			String account = getAccountName();
			Log.i("External activity returned","Returned from getting auth token");
			getTokenInAsyncTask(account);
		}
		else if(requestCode==SCAN_ACTIVITY){
			Log.i("External activity returned","Returned from getting auth token");
			String qrCode = data.getStringExtra("SCAN_RESULT");
			locationCode = qrCode;
			Toast.makeText(CheckInLocation.this, qrCode, Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putString("userName", this.userName);
		outState.putString("locationCode", this.locationCode);
		outState.putString("comments", commentsTV.getText().toString());
		
	}
	
	private void changeButtonVisibility(){
		if(locationCode!=null && !"".equals(locationCode)){
			submitBtn.setVisibility(Button.VISIBLE);
			commentsTV.setVisibility(TextView.VISIBLE);
			launchQrBtn.setVisibility(Button.INVISIBLE);
		}
		else{
			submitBtn.setVisibility(Button.INVISIBLE);
			commentsTV.setVisibility(TextView.INVISIBLE);
			launchQrBtn.setVisibility(Button.VISIBLE);
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
		
		long currentTS = System.currentTimeMillis()/1000;
		submitBtn.setText("sending check-in data...");
		submitBtn.setEnabled(false);
		List<String> params = new ArrayList<String>();
		params.add(this.userName);
		params.add(locationCode);
		params.add(commentsTV.getText().toString());
		params.add(Long.toString(currentTS));
		/*
		ProcessCheckIn task = new ProcessCheckIn(params);
		task.setDelegate(handler);
		task.execute();
		*/
		locationController.pingProvider();
		long ts = locationController.getTimestamp();
		Toast.makeText(this, "Network time: "+ts, Toast.LENGTH_SHORT).show();
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
			String[] files = this.fileList();
			//If this is the first installation - return null, which will invoke the registration
			if(files==null || files.length==0){
				return null;
			}
			for(String file : files){
				Log.i("checking context files",file);
			}
			br = new BufferedReader(new FileReader(getFilesDir()+"/"+Constants.USER_DATA_FILE));
			String line;
			StringBuilder text = new StringBuilder();
			while((line = br.readLine())!=null){
				text.append(line);
			}
			JSONObject json = new JSONObject(text.toString());
			return json;
		} catch (FileNotFoundException e) {
			Log.e("Reading user data","File doesn't exist - "+e.getMessage());
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
