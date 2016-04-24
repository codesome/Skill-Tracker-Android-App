package com.ibm.in.skilltracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void attemptLogin(View view){
        if(isNetworkAvailable()){
            String email = ((EditText) findViewById(R.id.email)).getText().toString();
            String password = ((EditText) findViewById(R.id.password)).getText().toString();
            if(email.equals("")){
                toastMessage("Enter email");
            } else if(password.equals("")){
                toastMessage("Enter Password");
            } else {
                (new LoginRequest()).execute(
                        "/app/login",
                        "email="+email+"&password="+password
                );
            }
        } else {
            startActivity(new Intent(LoginActivity.this, FirstActivity.class));
            finish();
        }
    }

    private class LoginRequest extends HttpPost {
        public ProgressDialog progress;

        @Override
        protected void onPreExecute(){
            progress= new ProgressDialog(LoginActivity.this);
            progress.setMessage("Authenticating");
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(String res){
            progress.dismiss();
            if(!res.equals("")){
                if(res.equals("invalid")){
                    ((TextView) findViewById(R.id.error_msg)).setText("Invalid Credentials");
                } else {
                    try {
                        JSONObject userObject = new JSONObject(res);
                        SharedPreferences userDetails = getSharedPreferences("userDetails",Context.MODE_PRIVATE);
                        SharedPreferences.Editor user = userDetails.edit();
                        user.putString("id",userObject.getString("_id"));
                        user.putString("name",userObject.getString("name"));
                        user.putString("email",userObject.getString("email"));
                        user.putBoolean("LoggedIn", true);
                        user.commit();
                        startActivity(new Intent(LoginActivity.this, UserActivity.class));
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                ((TextView) findViewById(R.id.error_msg)).setText("Problem occured while authenticating");
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void toastMessage(String message){
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

}

