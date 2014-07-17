package com.yishai.sep_patrol;

import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	
	
	TextView fNameTV;
	TextView lNameTV;
	TextView emailTV;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e("Where's null","haven't called create parent");
		super.onCreate(savedInstanceState);
		Log.e("Where's null","Registration activity created");
		setContentView(R.layout.activity_register);
		
		fNameTV = (TextView) findViewById(R.id.FirstNameTB);
		lNameTV = (TextView) findViewById(R.id.LastNameTB);
		emailTV = (TextView) findViewById(R.id.EmailAddressTB);
		Button registerB = (Button) findViewById(R.id.ResgiserButton);
		
		registerB.setOnClickListener(registrationListener);
		Log.e("Looking for null","Got here. Look further");
	}
		

	private OnClickListener registrationListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			String fname = fNameTV.getText().toString();
			String lname = lNameTV.getText().toString();
			String email = emailTV.getText().toString();
			 
			JSONObject jsonObject = genrateJson(fname, lname, email);
			
			try{
				saveRegistrationData(jsonObject);
				Toast.makeText(RegisterActivity.this, "Registration saved", Toast.LENGTH_SHORT).show();
				Intent checkInActivity = new Intent(RegisterActivity.this,CheckInLocation.class);
				startActivity(checkInActivity);
			}
			catch(IOException e){
				Log.e("Saving registration","Could not save registration data - "+e.getMessage());
				Toast.makeText(RegisterActivity.this, "Registration data not saved, please notify administrator", Toast.LENGTH_SHORT).show();
				return;
			}
			
		}
	};
	
	public JSONObject genrateJson(String fname, String lname, String email){
		JSONObject json = new JSONObject();
		try {
			json.put("first_name", fname);
			json.put("last_name", lname);
			json.put("email",email);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return json;
	}
		
	void saveRegistrationData(JSONObject data) throws IOException{
			FileOutputStream fos = openFileOutput(Constants.USER_DATA_FILE, Context.MODE_PRIVATE);
			fos.write(data.toString().getBytes());
			fos.close();
			Log.e("Saving registration data", data.toString());
	}
	
	
}
