package com.yishai.touchNgo.activities;




import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.yishai.touchNgo.AsyncResponseHandler;
import com.yishai.touchNgo.Constants;
import com.yishai.touchNgo.LocationController;
import com.yishai.touchNgo.ProcessCheckIn;
import com.yishai.touchNgo.R;
import com.yishai.touchNgo.model.CheckIn;
import com.yishai.touchNgo.model.User;
import com.yishai.touchNgo.services.FirebaseService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;

import android.net.Uri;
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


public class CheckInActivity extends Activity implements AsyncResponseHandler {


    TextView commentsTV;
    Button launchQrBtn;
    Button submitBtn;
    TextView descriptionView;

    ProgressBar pb;


    User user;
    CheckIn checkIn;
    String locationCode = "";
    String comments = "";

    public static final String LOGCAT = "Check-in CheckIn";

    private LocationController locController;
    private String token;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("com.yishai.touchNgo.prefs", MODE_PRIVATE);
        setContentView(R.layout.activity_main);

        commentsTV = (TextView) findViewById(R.id.commentsTB);
        descriptionView = (TextView) findViewById(R.id.descView);

        launchQrBtn = (Button) findViewById(R.id.scnButton);
        launchQrBtn.setOnClickListener(new launchQrListener());

        submitBtn = (Button) findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new submitListener());

        pb = (ProgressBar) findViewById(R.id.mainProgressBar);
    }


    @Override
    protected void onResume() {
        super.onResume();

        //Collect saved variables
        comments = sharedPreferences.getString("comments","");
        locationCode = sharedPreferences.getString("locationCode","");
        String userString = sharedPreferences.getString("user","");
        if(!"".equals(userString)){
            try {
                JSONObject userJson = new JSONObject(userString);
                this.user = new User(userJson);
            }
            catch(JSONException je){
                Log.e("CheckinActivity", "Failed to get User object from file: " + je.getMessage());
            }
        }

        //if loading saved params failed, or there are no saved params
        if(user==null){
            Intent registrationIntent = new Intent(this, RegisterActivity.class);
            startActivity(registrationIntent);
            return;
        }
        //Get user details, either from an existing file, or from registration process (which creates the file)
        //getUserData();
        descriptionView.setText("Hello, " + user.getName());

        //Toggle between Scan and submit buttons
        changeButtonVisibility();
        //Start the location controller service
        locController = new LocationController((LocationManager) getSystemService(Context.LOCATION_SERVICE));

        //We might arrive at this app via a direct link. In that case, no need to launch the QR reader, just confirm and send
        Intent intent = getIntent();
        if (intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            locationCode = uri.getQueryParameter("locationCode");;
            Toast.makeText(getApplicationContext(), "Code received: " + locationCode, Toast.LENGTH_LONG).show();

            changeButtonVisibility();
        }

    }


    private class launchQrListener implements OnClickListener {
        @Override
        public void onClick(View v) {


            launchQrBtn.setText("Please wait...");
            launchQrBtn.setEnabled(false);
            IntentIntegrator integrator = new IntentIntegrator(CheckInActivity.this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setPrompt("Scan the code");
            integrator.initiateScan();
        }


    }

    private class submitListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            submitBtn.setEnabled(false);
            submitBtn.setText("Please wait...");
            //checkIn(CheckInActivity.this);
            checkIn();
        }
    }

   /*
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

    private long getTimeStamp() {
        //@TODO: Get the time from the network. if no response from network, get the time
        //from the device clock.
        //If returning time from device, return as negative number. Otherwise, positive.

        if (!haveNetworkConnection())
            return (-System.currentTimeMillis());


        try {
            //Make the Http connection so we can retrieve the time
            HttpClient httpclient = new DefaultHttpClient();
            // I am using yahoos api to get the time
            HttpResponse response = httpclient.execute(new
                    HttpGet("http://developer.yahooapis.com/TimeService/V1/getTime?appid=YahooDemo"));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
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
                Log.d("Response", responseString.substring(x + "<Timestamp>".length(), y));
                String timestamp = responseString.substring(x + "<Timestamp>".length(), y);
                // The time returned is in UNIX format so i need to multiply it by 1000 to use it
                return Long.parseLong(timestamp);
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());

            }
        } catch (Exception e) {
            return (-System.currentTimeMillis());
        }
    }
    */

    //Code to run when Handling the response from the check-in Async process.
    @Override
    public void processFinish(JSONObject response) {

        try{
            if(response.has("status")){
                String status = response.getString("status");
                switch(status){
                    case "success":
                        if(response.has("key")){
                            String key = response.getString("key");
                            Log.i(LOGCAT, "Check-in activity successful. Activity code: "+key);
                            //checkIn.setKey(key);
                        }
                        break;
                    case "failure":
                        String reason;
                        if(response.has("reason")){
                             reason = response.getString("reason");
                        }
                        else{
                            reason = "Unknown reason";
                        }
                        Toast.makeText(this, "Check-in failure: "+reason,Toast.LENGTH_LONG).show();
                        Log.e(LOGCAT, "Failed to check-in. Reason: "+reason);
                    }
            }
        }
        catch(JSONException je){
            Log.e(LOGCAT, "Failed to handle check-in response. Reason: "+je.getMessage());
        }

        //Clear the textboxes
        commentsTV.setText("");
        comments = locationCode = "";
        sharedPreferences.edit().remove("locationCode").apply();
        /*try {
            int responseCode = (Integer) response.get("code");
            String responseText = (String) response.get("text");
            if (responseCode == 200) {
                Toast.makeText(getApplicationContext(), responseText, Toast.LENGTH_LONG).show();
                //submitBtn.setText(R.string.CheckIN);
                deleteFile(Constants.CHECKIN_DATA_FILE);
            } else {
                Toast.makeText(this, "Check-in failed. code: " + responseCode, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException je) {
            Log.e("JSON failure", "Failed to read JSON response from checkin");
        } finally {*/

            changeButtonVisibility();
        //}


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            Log.e("External activity returned", "returned after external activity was cancelled.");
            return;
        }
	/*	if(requestCode==GOOGLE_ACTIVITY){
			String account = getAccountName();
			Log.i("External activity returned","Returned from getting auth token");
			getTokenInAsyncTask(account);
		}
	*/
        //Returning from QR Reader
        else if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                locationCode = result.getContents();
            } else {
                super.onActivityResult(requestCode, resultCode, data);
                locationCode = "";
            }
            Toast.makeText(CheckInActivity.this, locationCode, Toast.LENGTH_SHORT).show();
            sharedPreferences.edit().putString("locationCode",locationCode).apply();

            changeButtonVisibility();
        }
    }

    @Override
    protected void onPause() {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(user!=null){
            editor.putString("user",user.toJson().toString());
        }
        if(locationCode!=null && !"".equals(locationCode)) {
            editor.putString("locationCode", locationCode);
        }
        editor.putString("comments", commentsTV.getText().toString());
        editor.apply();
        super.onPause();
    }


    private void changeButtonVisibility() {
        pb.setVisibility(View.INVISIBLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (locationCode != null && !"".equals(locationCode)) {
            launchQrBtn.setVisibility(Button.INVISIBLE);
            submitBtn.setVisibility(Button.VISIBLE);
            commentsTV.setVisibility(TextView.VISIBLE);
            submitBtn.setEnabled(true);
        } else {
            submitBtn.setVisibility(Button.INVISIBLE);
            commentsTV.setVisibility(TextView.INVISIBLE);
            launchQrBtn.setVisibility(Button.VISIBLE);
            launchQrBtn.setText(R.string.scnBtnTxt);
            launchQrBtn.setEnabled(true);
        }
    }



    private void checkIn(AsyncResponseHandler handler) {
		
		/*
		submitBtn.setText("sending check-in data...");
		submitBtn.setEnabled(false);
		
		*/
        JSONObject checkInData = saveParamsToFile(collectParams());
        if (checkInData != null) {
            ProcessCheckIn task = new ProcessCheckIn(checkInData);
            task.setDelegate(handler);
            task.execute(token);
        } else {
            Log.e("Checking in data", "No data in file to send");
            Toast.makeText(this, "Failed to send check-in data. If problem persists, please notify", Toast.LENGTH_LONG).show();
        }

    }

    private void checkIn(){
        checkIn = generateCheckIn();
        //saveActivitiesToFile(archivedData);

        FirebaseService fbService = new FirebaseService(this.getApplicationContext(), this);
        fbService.push("activities", "", checkIn);

    }

    private JSONObject collectParams() {
        long currentTS = System.currentTimeMillis() / 1000;
        JSONObject json = new JSONObject();
        try {
            json.put(Constants.USER_TXT, this.user.getKey());
            json.put(Constants.LOCATION_TXT, locationCode);
            json.put(Constants.COMMENTS_TXT, commentsTV.getText().toString());
            json.put(Constants.TIMESTAMP_TXT, Long.toString(currentTS));
            json.put("Longitude", locController.getLongitude());
            json.put("Latitude", locController.getLatitude());
            json.put(Constants.ACTIVITY_TXT, Constants.CHECK_IN_ACTIVITY);
        } catch (JSONException e) {
            Log.e("creating checkin json", e.getMessage());


        }

        return json;
    }

    private CheckIn generateCheckIn(){
        CheckIn checkIn = new CheckIn();
        checkIn.setTimestamp(System.currentTimeMillis()/1000);
        checkIn.setLocationKey(locationCode);
        checkIn.setUserKey(user.getKey());
        //checkIn.setLocationKey();
        checkIn.setLongitude(locController.getLongitude());
        checkIn.setLatitude(locController.getLatitude());
        checkIn.setComments(commentsTV.getText().toString());

        return checkIn;
    }


    private JSONArray retrieveActivitiesFromFile(){
        JSONArray dataArray = new JSONArray();
        try {
            BufferedReader br = new BufferedReader(new FileReader(getFilesDir() + "/" + Constants.CHECKIN_DATA_FILE));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            JSONObject fileData = new JSONObject(sb.toString());
            Log.e("check-in process", "file content: " + fileData.toString());
            dataArray = (JSONArray) fileData.get("check-in");

        } catch (FileNotFoundException fnfe) {
            Log.e(LOGCAT, "File Exception when trying to read check-in file: "+fnfe.getMessage());
        }
        catch(IOException ioe){
            Log.e(LOGCAT, "IO Exception when trying to read check-in file");
        }
        catch(JSONException je){
            Log.e(LOGCAT,"Failed to retrieve check-in file: "+je.getMessage());
        }
            return dataArray;
    }

    private void saveActivitiesToFile(JSONArray jsonObject){
        try{
            FileOutputStream fos = openFileOutput(Constants.CHECKIN_DATA_FILE, Context.MODE_PRIVATE);
            byte[] jsonContent = jsonObject.toString().getBytes();
            fos.write(jsonContent);
            fos.close();
        } catch (IOException ioe) {
            Log.e("Saving Check-in data", "IO Exception: " + ioe.getMessage());
        }
    }

    private JSONObject saveParamsToFile(JSONObject data) {
        JSONArray dataArray;
        JSONObject jsonObject = null;
        try {
            //deleteFile(Constants.CHECKIN_DATA_FILE);
            try {
                BufferedReader br = new BufferedReader(new FileReader(getFilesDir() + "/" + Constants.CHECKIN_DATA_FILE));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();

                JSONObject fileData = new JSONObject(sb.toString());
                Log.e("check-in process", "file content: " + fileData.toString());
                dataArray = (JSONArray) fileData.get("check-in");
            } catch (FileNotFoundException fnfe) {
                dataArray = new JSONArray();
            }
            dataArray.put(data);
            jsonObject = new JSONObject();
            jsonObject.put("check-in", dataArray);

            FileOutputStream fos = openFileOutput(Constants.CHECKIN_DATA_FILE, Context.MODE_PRIVATE);
            byte[] jsonContent = jsonObject.toString().getBytes();
            fos.write(jsonContent);
            fos.close();
        } catch (JSONException e) {
            Log.e("Saving Check-in data", "JSON Error: " + e.getMessage());
        } catch (IOException ioe) {
            Log.e("Saving Check-in data", "IO Exception: " + ioe.getMessage());
        }

        Log.e("Saving registration data", data.toString());
        if (jsonObject != null)
            return jsonObject;
        else
            return null;
    }


    private JSONObject getUserFromFile() {
        BufferedReader br = null;
        try {
            String[] files = this.fileList();
            //If this is the first installation - return null, which will invoke the registration
            if (files == null || files.length == 0) {
                return null;
            }
            for (String file : files) {
                Log.i("checking context files", file);
            }
            br = new BufferedReader(new FileReader(getFilesDir() + "/" + Constants.USER_DATA_FILE));
            String line;
            StringBuilder text = new StringBuilder();
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            JSONObject json = new JSONObject(text.toString());
            return json;
        } catch (FileNotFoundException e) {
            Log.e("Reading user data", "File doesn't exist - " + e.getMessage());
        } catch (JSONException e1) {
            Log.e("Reading user data", "JSON failure");
        } catch (IOException e1) {
            Log.e("Reading user data", "IO failure");
        } finally {
            try {
                if (null != br)
                    br.close();
            } catch (IOException e) {
                Log.e("reading user data", "Couldn't close user data file");
            }

        }
        return null;
    }

}