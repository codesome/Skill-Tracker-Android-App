package com.ibm.in.skilltracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity {
    public ProgressDialog progress;

    private int startClientID = 1117 , endClientID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkIfNetworkIsConnected();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        refreshUserClients();
    }

    private void refreshUserClients(){
        SharedPreferences user = getSharedPreferences("userDetails",Context.MODE_PRIVATE);
        (new GetUserClients()).execute(
                "/app/getUserClients",
                "id=" + user.getString("id", null)
        );
    }

    private void checkIfNetworkIsConnected(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            startActivity(new Intent(EditActivity.this,FirstActivity.class));
        }
    }

    /* To get the User Data */
    private class GetUserClients extends HttpPost {

        private int compare (String s , JSONArray arr){
            for(int i=0;i<arr.length();i++){
                try {
                    if(s.equals(arr.getString(i))){
                        return i;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return -1;
        }

        @Override
        protected void onPreExecute(){
            checkIfNetworkIsConnected();
            progress= new ProgressDialog(EditActivity.this);
            progress.setMessage("Fetching Clients");
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(String res){

            try {
                progress.setMessage("Updating Clients");
                JSONObject clients = new JSONObject(res);
                JSONArray userClients= new JSONArray(clients.getString("userClients"));
                JSONArray allClients= new JSONArray(clients.getString("allClients"));

                LinearLayout clientLayout = (LinearLayout) findViewById(R.id.edit_clients);
                clientLayout.removeAllViews();

                CheckBox c;
                int pos;
                String text;
                for(int i=0;i<allClients.length();i++){
                    c = new CheckBox(EditActivity.this);
                    text = allClients.getString(i);
                    c.setText(text);
                    c.setId(startClientID+i);
                    pos = compare(text,userClients);
                    if(pos != -1){
                        c.setChecked(true);
                    } else {
                        c.setChecked(false);
                    }
                    clientLayout.addView(c);
                }
                endClientID = startClientID + allClients.length()-1;

                Button b = new Button(EditActivity.this);
                b.setText("Submit");
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox c;
                        ArrayList<String> clients = new ArrayList<String>();
                        Log.e("start",Integer.toString(startClientID));
                        Log.e("end",Integer.toString(endClientID));
                        for (int i = startClientID; i <= endClientID; i++) {
                            c = (CheckBox) findViewById(i);
                            Log.e("text - "+i,c.getText().toString());
                            if (c.isChecked()) {
                                clients.add(c.getText().toString());
                            }
                        }

                        SharedPreferences user = getSharedPreferences("userDetails",Context.MODE_PRIVATE);
                        (new UpdateUserClients()).execute(
                                "/app/UpdateUserClients",
                                "id="+user.getString("id",null)+"&clients=" + clients.toString()
                        );

                    }
                });
                clientLayout.addView(b);
                progress.dismiss();

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    private class UpdateUserClients extends HttpPost {

        @Override
        protected void onPreExecute(){
            checkIfNetworkIsConnected();
            progress= new ProgressDialog(EditActivity.this);
            progress.setMessage("Updating Clients");
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(String res){
            progress.dismiss();
            if(!res.equals("success")){
                toastMessage("There was some error, try again");
            }
        }
    }

    private void toastMessage(String message){
        Toast.makeText(EditActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
