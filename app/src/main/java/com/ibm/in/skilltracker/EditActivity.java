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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class EditActivity extends AppCompatActivity {
    public ProgressDialog progress;

    private int startClientID = 1117 , endClientID , cbllClientKey = 3323 , dClientKey = 4723 , lwClientKey = 6343 ;
    private int startCertID = 7109  , endCertID , cbllCertKey = 8231 , dCertKey = 9319 , lwCertKey = 10079 ;

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
                "/app/getAllClients",
                "id=" + user.getString("id", null)
        );
    }

    private void refreshUserCertificates(){
        SharedPreferences user = getSharedPreferences("userDetails",Context.MODE_PRIVATE);
        (new GetUserCertificates()).execute(
                "/app/getAllCertificates",
                "id=" + user.getString("id", null)
        );
    }

    private void checkIfNetworkIsConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            startActivity(new Intent(EditActivity.this,FirstActivity.class));
        }
    }

    /* To get the User Data */
    private class GetUserClients extends HttpPost {

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
                JSONArray allClients= new JSONArray(res);

                LinearLayout clientLayout = (LinearLayout) findViewById(R.id.edit_clients);
                clientLayout.removeAllViews();

                CheckBox c;
                LinearLayout ll;
                String text;
                int checkboxID;
                JSONObject client;
                for(int i=0;i<allClients.length();i++){
                    checkboxID = startClientID+i;

                    client = allClients.getJSONObject(i);

                    c = new CheckBox(EditActivity.this);
                    text = client.getString("data");
                    c.setText(text);
                    c.setId(checkboxID);
                    final int finalCheckboxID = checkboxID;
                    c.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CheckBox c = (CheckBox) v;
                            LinearLayout l = (LinearLayout) findViewById(finalCheckboxID + cbllClientKey);
                            if (c.isChecked()) {
                                Spinner spinner = new Spinner(EditActivity.this);

                                ArrayList<String> spinnerArray = new ArrayList<String>();
                                spinnerArray.add("Duration");
                                for (int i = 1; i <= 15; i++) {
                                    spinnerArray.add("" + i);
                                }
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(EditActivity.this, R.layout.spinner_format, spinnerArray);
                                spinner.setAdapter(spinnerArrayAdapter);
                                spinner.setId(finalCheckboxID + dClientKey);
                                l.addView(spinner);

                                spinner = new Spinner(EditActivity.this);
                                spinnerArray = new ArrayList<String>();
                                spinnerArray.add("Last Worked");
                                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                                for (int i = 1990; i <= currentYear; i++) {
                                    spinnerArray.add("" + i);
                                }
                                spinnerArrayAdapter = new ArrayAdapter<String>(EditActivity.this, R.layout.spinner_format, spinnerArray);
                                spinner.setAdapter(spinnerArrayAdapter);
                                spinner.setId(finalCheckboxID + lwClientKey);
                                l.addView(spinner);

                            } else {
                                if (l != null) {
                                    l.removeAllViews();
                                }
                            }
                        }
                    });

                    ll = new LinearLayout(EditActivity.this);
                    ll.setOrientation(LinearLayout.HORIZONTAL);
                    ll.setId(checkboxID+cbllClientKey);


                    if(client.getBoolean("checked")){
                        c.setChecked(true);
                    } else {
                        c.setChecked(false);
                    }
                    clientLayout.addView(c);
                    clientLayout.addView(ll);
                }
                endClientID = startClientID + allClients.length()-1;

                Button b = new Button(EditActivity.this);
                b.setText("Submit");
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox c;
                        Spinner d , lw ;
                        String dText , lwText;
                        boolean flag = true;
                        JSONArray clients = new JSONArray();
                        JSONObject cli;
                        for (int i = startClientID; i <= endClientID; i++) {
                            if(flag) {
                                c = (CheckBox) findViewById(i);
                                if (c.isChecked()) {
                                    d = ((Spinner) findViewById(i + dClientKey));
                                    lw = ((Spinner) findViewById(i + lwClientKey));
                                    if(d!=null && lw!=null) {
                                        dText = d.getSelectedItem().toString();
                                        lwText = lw.getSelectedItem().toString();
                                        if (dText.equals("Duration")) {
                                            flag = false;
                                            toastMessage("Select Duration for all Clients");
                                        } else if (lwText.equals("Last Worked")) {
                                            flag = false;
                                            toastMessage("Select Last Worked for all Clients");
                                        } else {
                                            cli = new JSONObject();
                                            try {
                                                cli.put("data", c.getText().toString());
                                                cli.put("duration", dText);
                                                cli.put("lastWorked", lwText);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            clients.put(cli);
                                        }
                                    } else {
                                        cli = new JSONObject();
                                        try {
                                            cli.put("data", c.getText().toString());
                                            cli.put("duration", null);
                                            cli.put("lastWorked", null);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        clients.put(cli);
                                    }
                                }
                            } else {
                                break;
                            }
                        }

                        if(flag) {
                            SharedPreferences user = getSharedPreferences("userDetails", Context.MODE_PRIVATE);
                            (new UpdateUserClients()).execute(
                                    "/app/UpdateUserClients",
                                    "id=" + user.getString("id", null) + "&clients=" + clients.toString()
                            );
                        }
                    }
                });
                clientLayout.addView(b);
                refreshUserCertificates();

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }



    private class GetUserCertificates extends HttpPost {

        @Override
        protected void onPreExecute(){
            checkIfNetworkIsConnected();
            //progress= new ProgressDialog(EditActivity.this);
            progress.setMessage("Fetching Certificates");
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(String res){

            try {
                Log.e("GOOOOOOOO",res);
                progress.setMessage("Updating Certificates");
                JSONArray allCerts= new JSONArray(res);

                LinearLayout certLayout = (LinearLayout) findViewById(R.id.edit_certificates);
                certLayout.removeAllViews();

                CheckBox c;
                LinearLayout ll;
                String text;
                int checkboxID;
                JSONObject cert;
                for(int i=0;i<allCerts.length();i++){
                    checkboxID = startCertID+i;

                    cert = allCerts.getJSONObject(i);

                    c = new CheckBox(EditActivity.this);
                    text = cert.getString("data");
                    c.setText(text);
                    c.setId(checkboxID);
                    final int finalCheckboxID = checkboxID;
                    c.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CheckBox c = (CheckBox) v;
                            LinearLayout l = (LinearLayout) findViewById(finalCheckboxID + cbllCertKey);
                            if (c.isChecked()) {
                                Spinner spinner = new Spinner(EditActivity.this);

                                ArrayList<String> spinnerArray = new ArrayList<String>();
                                spinnerArray.add("Year");
                                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                                for (int i = 1990; i <= currentYear; i++) {
                                    spinnerArray.add("" + i);
                                }
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(EditActivity.this, R.layout.spinner_format, spinnerArray);
                                spinner.setAdapter(spinnerArrayAdapter);
                                spinner.setId(finalCheckboxID + dCertKey);
                                l.addView(spinner);

                                spinner = new Spinner(EditActivity.this);
                                spinnerArray = new ArrayList<String>();
                                spinnerArray.add("Month");
                                for (int i = 1990; i <= currentYear; i++) {
                                    spinnerArray.add("" + i);
                                }
                                spinnerArrayAdapter = new ArrayAdapter<String>(EditActivity.this, R.layout.spinner_format, spinnerArray);
                                spinner.setAdapter(spinnerArrayAdapter);
                                spinner.setId(finalCheckboxID + lwCertKey);
                                l.addView(spinner);

                            } else {
                                if (l != null) {
                                    l.removeAllViews();
                                }
                            }
                        }
                    });

                    ll = new LinearLayout(EditActivity.this);
                    ll.setOrientation(LinearLayout.HORIZONTAL);
                    ll.setId(checkboxID+cbllCertKey);


                    if(cert.getBoolean("checked")){
                        c.setChecked(true);
                    } else {
                        c.setChecked(false);
                    }
                    certLayout.addView(c);
                    certLayout.addView(ll);
                }
                endCertID= startCertID + allCerts.length()-1;

                Button b = new Button(EditActivity.this);
                b.setText("Submit");
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox c;
                        Spinner d , lw ;
                        String dText , lwText;
                        boolean flag = true;
                        JSONArray certificates = new JSONArray();
                        JSONObject cert;
                        for (int i = startCertID; i <= endCertID; i++) {
                            if(flag) {
                                c = (CheckBox) findViewById(i);
                                if (c.isChecked()) {
                                    d = ((Spinner) findViewById(i + dCertKey));
                                    lw = ((Spinner) findViewById(i + lwCertKey));
                                    if(d!=null && lw!=null) {
                                        dText = d.getSelectedItem().toString();
                                        lwText = lw.getSelectedItem().toString();
                                        if (dText.equals("Year")) {
                                            flag = false;
                                            toastMessage("Select Year for all Clients");
                                        } else {
                                            if (lwText.equals("Month")) {
                                                lwText = null;
                                            }
                                            cert = new JSONObject();
                                            try {
                                                cert.put("data", c.getText().toString());
                                                cert.put("year", dText);
                                                cert.put("month", lwText);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            certificates.put(cert);
                                        }
                                    } else {
                                        cert = new JSONObject();
                                        try {
                                            cert.put("data", c.getText().toString());
                                            cert.put("year", null);
                                            cert.put("month", null);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        certificates.put(cert);
                                    }
                                }
                            } else {
                                break;
                            }
                        }

                        if(flag) {
                            SharedPreferences user = getSharedPreferences("userDetails", Context.MODE_PRIVATE);
                            (new UpdateUserClients()).execute(
                                    "/app/UpdateUserCertificates",
                                    "id=" + user.getString("id", null) + "&certificate=" + certificates.toString()
                            );
                        }
                    }
                });
                certLayout.addView(b);
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
            if(res.equals("success")){
                LinearLayout ll;
                for(int i = startCertID+cbllClientKey ; i<= endClientID+cbllClientKey ; i++){
                    ll = (LinearLayout) findViewById(i);
                    if(ll!=null){
                        ll.removeAllViews();
                    }
                }
            } else if (res.equals("fail")){
                toastMessage("There was some error, try again");
            } else {
                toastMessage("There was some error. Please restart the app or contact the admin.");
            }
        }
    }

    private void toastMessage(String message){
        Toast.makeText(EditActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
