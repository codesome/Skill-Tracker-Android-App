package com.ibm.in.skilltracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FirstActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        startApp();
    }

    public void refreshFunction(View view){
        startApp();
    }

    private void startApp(){
        final TextView textView = (TextView) findViewById(R.id.error_textview);
        final Button refreshBtn = (Button) findViewById(R.id.refresh_btn);
        textView.setText("Connecting");
        refreshBtn.setVisibility(View.GONE);
        final ProgressDialog progress= new ProgressDialog(this);
        progress.setMessage("Connecting to server");
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        progress.show();
        if(isNetworkAvailable()){
            progress.dismiss();
            SharedPreferences userDetails = getSharedPreferences("userDetails",Context.MODE_PRIVATE);
            if(userDetails.getBoolean("LoggedIn",false)){
                startActivity(new Intent(FirstActivity.this, UserActivity.class));
            } else {
                startActivity(new Intent(FirstActivity.this, LoginActivity.class));
            }
            finish();
        } else {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progress.dismiss();
                    refreshBtn.setVisibility(View.VISIBLE);
                    textView.setText("Connection Error");
                }
            }, 2000);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
