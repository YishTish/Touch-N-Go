package com.yishai.touchNgo.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yishai.touchNgo.Constants;
import com.yishai.touchNgo.HandleAsyncResponse;
import com.yishai.touchNgo.PostToFirebase;
import com.yishai.touchNgo.R;
import com.yishai.touchNgo.activities.CheckInActivity;
import com.yishai.touchNgo.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class RegisterActivity extends Activity implements HandleAsyncResponse {



    TextView fNameTV;
    TextView lNameTV;
    TextView emailTV;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        user = new User();

        fNameTV = (TextView) findViewById(R.id.FirstNameTB);
        lNameTV = (TextView) findViewById(R.id.LastNameTB);
        emailTV = (TextView) findViewById(R.id.EmailAddressTB);
        Button registerB = (Button) findViewById(R.id.ResgiserButton);

        registerB.setOnClickListener(registrationListener);
    }


    private View.OnClickListener registrationListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String fname = fNameTV.getText().toString();
            String lname = lNameTV.getText().toString();
            String email = emailTV.getText().toString();

            user.setFname(fname);
            user.setLname(lname);
            user.setEmail(email);

            //genrateJson(fname, lname, email);

            try {
                saveRegistrationData(user.toJson());
            } catch (IOException e) {
                Log.e("Saving registration", "Could not save registration data - " + e.getMessage());
                Toast.makeText(RegisterActivity.this, "Registration data not saved, please notify administrator", Toast.LENGTH_SHORT).show();
                return;
            }

        }
    };

        /*
        public JSONObject genrateJson(String fname, String lname, String email) {
            JSONObject json = new JSONObject();
            try {
                json.put("first_name", fname);
                json.put("last_name", lname);
                json.put("email", email);
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            return json;
        }*/

    /**
     * This method is a part of three methods repsonsible for saving user's data.
     * first - send the data to firebase.
     * second - once firebase returns with a response, store the key value from the response as the user's key
     * third - save the full user object to the database, as JSON
     *
     * @param data
     * @throws java.io.IOException
     */
    private void saveRegistrationData(JSONObject data) throws IOException {
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
        try {
            user.setKey(response.getString("text"));
        } catch (JSONException je) {
            Log.e("Registration failure", "Could not get user key. Reason: " + je.getMessage());
        }
        saveToFile();
        Toast.makeText(this.getApplicationContext(), "User data has been saved to DB", Toast.LENGTH_SHORT).show();
    }


    private void saveToFile() {
        try {
            FileOutputStream fos = openFileOutput(Constants.USER_DATA_FILE, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(user.toJson().toString());
            fos.flush();
            fos.close();
            endRegistration();
        }
        catch(IOException ie){
            Log.e("Saving user data","Could not save user data to DB: "+ie.getMessage());
        }
    }

    private void endRegistration(){
        Toast.makeText(RegisterActivity.this, "Registration saved", Toast.LENGTH_SHORT).show();
        Intent checkInActivity = new Intent(RegisterActivity.this, CheckInActivity.class);
        startActivity(checkInActivity);
    }

}