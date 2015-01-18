package com.yishai.touchNgo.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yishai.touchNgo.AsyncResponseHandler;
import com.yishai.touchNgo.Constants;
import com.yishai.touchNgo.PostToFirebase;
import com.yishai.touchNgo.R;
import com.yishai.touchNgo.model.User;
import com.yishai.touchNgo.services.FirebaseService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class RegisterActivity extends Activity implements AsyncResponseHandler {



    TextView nameTV;
    TextView emailTV;
    TextView passwordTV;

    User user;
    SharedPreferences sharedPreferences;

    private final static String LOGCAT = "Register CheckIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        user = new User();

        nameTV = (TextView) findViewById(R.id.NameTB);
        emailTV = (TextView) findViewById(R.id.EmailAddressTB);
        passwordTV = (TextView)findViewById(R.id.PasswordTB);
        Button registerB = (Button) findViewById(R.id.ResgiserButton);


        registerB.setOnClickListener(registrationListener);
        sharedPreferences = getSharedPreferences("com.yishai.touchNgo.prefs", MODE_PRIVATE);
    }

    private void registerToFirebase(){
        //pushUserData(user.toJson());
        FirebaseService fbService = new FirebaseService(getApplicationContext(), this);
        fbService.register(user);
    }

    private void addUserToFirebase(User user){
        FirebaseService fbService = new FirebaseService(getApplicationContext(), this);
        fbService.push("users","", user);
    }


    private View.OnClickListener registrationListener = new View.OnClickListener() {


        @Override
        public void onClick(View v) {

            user.setName(nameTV.getText().toString());
            user.setEmail(emailTV.getText().toString());
            user.setPassword(passwordTV.getText().toString());
            user.setJoinDate(System.currentTimeMillis()/1000);
            registerToFirebase();
           // addUserToFirebase(user);

        }
    };


    /**
     * This method is a part of three methods repsonsible for saving user's data.
     * first - send the data to firebase.
     * second - once firebase returns with a response, store the key value from the response as the user's key
     * third - save the full user object to the database, as JSON
     *
     * @param data
     * @throws java.io.IOException
     */
    private void pushUserData(JSONObject data) {
        PostToFirebase postToFirebase = new PostToFirebase("users", RegisterActivity.this, "");
        postToFirebase.execute(data);
    }


    /**
     * When user data is saved to Firebase, this method is responsible for handlnig the response from Firebase.
     * The response includes the user key that was generated. Store that key, and call save user data to file
     * @param response
     */
    @Override
    public void processFinish(JSONObject response) {
        Log.i(LOGCAT, "Current response: " +response.toString());
        try {
            if(response.has("stage")){
                //First step, registering user to Firebase. @TODO set up permissions
                if(response.getString("stage").equals("registration")) {
                    if (response.has("status") && response.getString("status").equals("failure")) {
                        String reason = (response.has("reason")) ? response.getString("reason") : "No reason provided";
                        Toast.makeText(this, "Registration failure: " + reason, Toast.LENGTH_SHORT).show();
                    } else {
                        if (response.has("user")) {
                            String userString = response.getString("user");
                            user = new User(new JSONObject(userString));
                            Toast.makeText(this.getApplicationContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                            addUserToFirebase(user);
                        }
                    }
                }
                //This is the second step, sending user-data to firebase table
                else if(response.getString("stage").equals("push_data")){
                    if(response.has("status")&& response.getString("status").equals("success")){
                        Log.i(LOGCAT,"Added user data to Firebase successfully");
                        if(response.has("key")){
                            user.setKey(response.getString("key"));
                         }
                    }
                    else{
                        if(response.has("reason")){
                            Log.e(LOGCAT, "Failed to add user data to Firebase: " + response.getString("reason"));
                        }
                        else{
                            Log.e(LOGCAT,"Failed to add user data to Firebase. Unknown reason");
                        }
                    }
                    endRegistration();
                }
            }
            else{
                Log.e(LOGCAT, "Received an unexpected JSON response: " + response.toString());
            }
        } catch (JSONException je) {
            Log.e("Registration failure", "Could not get user key. Reason: " + je.getMessage());
        }
        //endRegistration();
    }


    private void saveToFile() {
//        try {

            JSONObject userJson = user.toJson();
            String userString = userJson.toString();
            Log.i(LOGCAT, "Saving the following user data: "+userString);
            try {
                FileOutputStream fos = openFileOutput(Constants.USER_DATA_FILE, Context.MODE_PRIVATE);
                OutputStreamWriter writer = new OutputStreamWriter(fos);

                writer.write(userString);
               writer.flush();
                writer.close();
            }
            catch (Exception e){
                Log.e(LOGCAT, "Failed to save file: "+e.getMessage());
                finish();
            }

            Log.i(LOGCAT, "Saved registration data to file");
            endRegistration();
//        }
//        catch(IOException ie){
//            Log.e("Saving user data","Could not save user data to DB: "+ie.getMessage());
//        }
    }

    private void endRegistration(){
        Toast.makeText(RegisterActivity.this, "Registration Completed", Toast.LENGTH_SHORT).show();
        sharedPreferences.edit().putString("user",user.toJson().toString()).apply();
        finish();
        return;
        /*Intent checkInActivity = new Intent(this, CheckInActivity.class);
        startActivity(checkInActivity);*/
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user",user.toJson().toString());
        editor.apply();
        super.onPause();
    }
}