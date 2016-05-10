package com.ibm.in.skilltracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EmployeeView extends AppCompatActivity {

    public ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_view);
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

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        progress= new ProgressDialog(EmployeeView.this);
        progress.setMessage("Loading...");
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        progress.show();
        (new GetEmployeeData()).execute(
                "/app/getSingleEmployeeData",
                "id="+id
        );

    }


    /* To get the User Data */
    private class GetEmployeeData extends HttpPost {


        @Override
        protected void onPreExecute(){
            checkIfNetworkIsConnected();
        }

        @Override
        protected void onPostExecute(String res){
            progress.dismiss();
            if(!res.equals("")){
                Log.e("LOOK HERE","CAME 1");
                if(res.equals("invalid")){
                    Log.e("LOOK HERE","CAME 2");
                    progress.dismiss();
                    startActivity(new Intent(EmployeeView.this, UserActivity.class));
                    finish();
                } else {
                    try {
                        Log.e("LOOK HERE","CAME 3");
                        JSONObject userData = new JSONObject(res);

                        JSONArray userSkills = new JSONArray(userData.getString("skills"));
                        JSONArray certificates = new JSONArray(userData.getString("certificates"));
                        JSONArray clients = new JSONArray(userData.getString("clients"));

                        // Adding Skills to read
                        LinearLayout readSkills = (LinearLayout) findViewById(R.id.read_skills);
                        if(userSkills.length()!=0) {
                            JSONObject skill;
                            String role, skillName, skillType;
                            for (int i = 0; i < userSkills.length(); i++) {
                                skill = userSkills.getJSONObject(i);
                                role = skill.getString("role");
                                skillName = skill.getString("skillName");
                                skillType = skill.getString("skillType");
                                TextView t = new TextView(EmployeeView.this);
                                t.setText(role + "-" + skillName + "-" + skillType);
                                readSkills.addView(t);
                            }
                        } else {
                            TextView t = new TextView(EmployeeView.this);
                            t.setText("None");
                            readSkills.addView(t);
                        }

                        // Adding certificates to read
                        LinearLayout readCert = (LinearLayout) findViewById(R.id.read_certificates);
                        if(certificates.length()!=0) {
                            JSONObject obj;
                            for (int i = 0; i < certificates.length(); i++) {
                                TextView t = new TextView(EmployeeView.this);
                                obj = certificates.getJSONObject(i);
                                String text = obj.getString("data") + " | ";
                                if (!obj.getString("month").equals("null")) {
                                    text += obj.getString("month") + ",";
                                }
                                text += obj.getString("year");
                                t.setText(text);
                                readCert.addView(t);
                            }
                        } else {
                            TextView t = new TextView(EmployeeView.this);
                            t.setText("None");
                            readCert.addView(t);
                        }

                        // Adding clients to read
                        LinearLayout readCli = (LinearLayout) findViewById(R.id.read_clients);
                        if(clients.length()!=0) {
                            JSONObject client;
                            for (int i = 0; i < clients.length(); i++) {
                                client = clients.getJSONObject(i);
                                TextView t = new TextView(EmployeeView.this);
                                t.setText(client.getString("data")+" | "+client.getString("duration")+" | "+client.getString("lastWorked"));
                                readCli.addView(t);
                            }
                        } else {
                            TextView t = new TextView(EmployeeView.this);
                            t.setText("None");
                            readCli.addView(t);
                        }

                        progress.dismiss();


                    } catch (JSONException e) {
                        Log.e("LOOK HERE","CAME 4");
                        progress.dismiss();
                        e.printStackTrace();
                    }
                }
            } else {
                Log.e("LOOK HERE","CAME 5");
                progress.dismiss();
                ((TextView) findViewById(R.id.error_msg)).setText("Problem occured while authenticating");
            }
        }
    }

    private void checkIfNetworkIsConnected(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            startActivity(new Intent(EmployeeView.this,FirstActivity.class));
        }
    }

}
