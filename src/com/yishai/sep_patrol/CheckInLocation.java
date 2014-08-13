package com.yishai.sep_patrol;




import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class CheckInLocation extends Activity implements HandleAsyncResponse {

	private final int GOOGLE_ACTIVITY=1;
	private final int SCAN_ACTIVITY=2;
	
	
	TextView commentsTV;
	Button launchQrBtn;
	Button submitBtn;

    ProgressBar pb;
	
	
	String userName="";
	String locationCode ="";
	String comments = "";

    LocationController locController;
    String token;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadSavedVariables(savedInstanceState);
		
		setContentView(R.layout.activity_main);

		TextView descriptionView = (TextView)findViewById(R.id.descView);
		descriptionView.setText("Hello, "+userName);
		
		commentsTV = (TextView)findViewById(R.id.commentsTB);

		//Get user details, either from an existing file, or from registration process (which creates the file)
		manageUserData();

		
		launchQrBtn = (Button)findViewById(R.id.scnButton);
		launchQrBtn.setOnClickListener(new launchQrListener());
		
		
		
		
		submitBtn = (Button)findViewById(R.id.submitBtn);
		//submitBtn.setVisibility(Button.INVISIBLE);
		submitBtn.setOnClickListener(new submitListener());

        pb = (ProgressBar)findViewById(R.id.mainProgressBar);
		//commentsTV.setVisibility(TextView.INVISIBLE);
	}
	
	private void loadSavedVariables(Bundle savedInstance){
		if(savedInstance==null)
			return;
		String userName = savedInstance.getString("userName");
		if(userName!=null && !"".equals(userName))
			this.userName = userName;
		String locationCode = savedInstance.getString("locationCode");
		if(locationCode!=null && !"".equals(locationCode))
			this.locationCode = locationCode;
		String comments = savedInstance.getString("comments");
		if(comments!=null && !"".equals(comments))
			this.comments = comments;
	}
	
	@Override
	protected void onResume() {
        token = new GoogleTokenController(this).getToken();
        // TODO Auto-generated method stub
		super.onResume();
		if(null != this.userName && !"".equals(userName)){
			TextView descriptionView = (TextView)findViewById(R.id.descView);
			descriptionView.setText("Hello "+userName);
		}
		changeButtonVisibility();
        locController = new LocationController((LocationManager)getSystemService(Context.LOCATION_SERVICE));
	}
	
	
	
	
	private class launchQrListener implements OnClickListener{
			@Override
			public void onClick(View v) {


                launchQrBtn.setText("Please wait...");
                launchQrBtn.setEnabled(false);
                IntentIntegrator integrator = new IntentIntegrator(CheckInLocation.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan the code");
                integrator.initiateScan();

				/*
				Toast.makeText(CheckInLocation.this,"Launching QR scanner", Toast.LENGTH_SHORT).show();
				Intent qrIntent = new Intent(getApplicationContext(),CaptureActivity.class);
				qrIntent.setAction("com.google.zxing.client.android.SCAN");
				qrIntent.putExtra("SAVE_HISTORY",false);
				startActivityForResult(qrIntent, 2);
			//	launchQrBtn.setText("Please wait");
				launchQrBtn.setEnabled(false);
				*/
			}
			
			
	}
	
	private class submitListener implements OnClickListener{
		
		@Override
		public void onClick(View v) {
		//	String accountName = getAccountName();
			//@TODO: Get time from network, and if fails get from device and add comment
			//long currentTS = System.currentTimeMillis()/1000;
		/*	long currentTS = getTimeStamp();
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
			task.execute(); */

            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            submitBtn.setEnabled(false);
			submitBtn.setText("Please wait...");
			checkIn(CheckInLocation.this);
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
			Intent registrationIntent = new Intent(CheckInLocation.this, RegisterActivity.class);
			startActivity(registrationIntent);
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
	
	
	
	//Code to run when Handling the response from the check-in Async process.
	@Override
	public void processFinish(JSONObject response) {
			
		//Clear the textboxes
		commentsTV.setText("");
		comments = locationCode = "";
		try{
			int responseCode = (Integer)response.get("code");
			String responseText = (String)response.get("text");
			if(responseCode == 200){
				Toast.makeText(getApplicationContext(), responseText, Toast.LENGTH_LONG).show();
				//submitBtn.setText(R.string.CheckIN);
				deleteFile(Constants.CHECKIN_DATA_FILE);
			}
			else{
				Toast.makeText(this,"Check-in failed. code: "+responseCode, Toast.LENGTH_SHORT).show();
			}
		}
		catch(JSONException je){
			Log.e("JSON failure","Failed to read JSON response from checkin");
		}
		finally{
			changeButtonVisibility();
		}
		

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
		else {//if(requestCode==SCAN_ACTIVITY){
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode, data);
            if(result != null){
                locationCode = result.getContents();
            }
            else{
                super.onActivityResult(requestCode,resultCode,data);
                locationCode = "";
            }
            Toast.makeText(CheckInLocation.this, "Request Code: "+requestCode, Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putString("userName", this.userName);
		outState.putString("locationCode", this.locationCode);
		outState.putString("comments", commentsTV.getText().toString());
        //outState.putString("token", commentsTV.getText().toString());
		
	}
	
	private void changeButtonVisibility(){
        pb.setVisibility(View.INVISIBLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

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
		
		/*
		submitBtn.setText("sending check-in data...");
		submitBtn.setEnabled(false);
		
		*/
		JSONObject checkInData = saveParamsToFile(collectParams());
		if(checkInData != null){
			ProcessCheckIn task = new ProcessCheckIn(checkInData);
			task.setDelegate(handler);
			task.execute(token);
		}
		else{
			Log.e("Checking in data", "No data in file to send");
			Toast.makeText(this, "Failed to send check-in data. If problem persists, please notify", Toast.LENGTH_LONG).show();
		}
		
	}
	
	private JSONObject collectParams () {
		long currentTS = System.currentTimeMillis()/1000;
		JSONObject json = new JSONObject();
        try {
				json.put(Constants.NAME_TXT, this.userName);
				json.put(Constants.LOCATION_TXT, locationCode);
				json.put(Constants.COMMENTS_TXT, commentsTV.getText().toString());
				json.put(Constants.TIMESTAMP_TXT,Long.toString(currentTS));
                json.put("Longitude",locController.getLongitude());
                json.put("Latitude", locController.getLatitude());
		} catch (JSONException e) {
			Log.e("creating checkin json", e.getMessage());

		}

		return json;
	}
	
	private JSONObject saveParamsToFile(JSONObject data){
		JSONArray dataArray;
		JSONObject jsonObject = null;
		try{
			//deleteFile(Constants.CHECKIN_DATA_FILE);
			try{
				BufferedReader br = new BufferedReader(new FileReader(getFilesDir()+"/"+Constants.CHECKIN_DATA_FILE));
				StringBuilder sb = new StringBuilder();
				String line;
				while((line = br.readLine())!=null){
					sb.append(line);
				}
				br.close();

				JSONObject fileData = new JSONObject(sb.toString());
				Log.e("check-in process","file content: "+fileData.toString());
				dataArray = (JSONArray)fileData.get("data");
			}
			catch(FileNotFoundException fnfe){
				dataArray = new JSONArray();
			}
			dataArray.put(data);
			jsonObject = new JSONObject();
			jsonObject.put("data",dataArray);
			
			FileOutputStream fos = openFileOutput(Constants.CHECKIN_DATA_FILE, Context.MODE_PRIVATE);
			byte[] jsonContent = jsonObject.toString().getBytes();
			fos.write(jsonContent);
			fos.close();
		}
		catch(JSONException e){
			Log.e("Saving Check-in data", "JSON Error: "+e.getMessage());
		}
		catch(IOException ioe){
			Log.e("Saving Check-in data","IO Exception: "+ioe.getMessage());
		}
		
		Log.e("Saving registration data", data.toString());
		if(jsonObject != null)
			return jsonObject;
		else 
			return null;
}

	 /*private void createCheckingFile(){
		 output
	 }*/

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