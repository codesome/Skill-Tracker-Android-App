package com.ibm.in.skilltracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkIfNetworkIsConnected();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*===========================*/
        SharedPreferences user = getSharedPreferences("userDetails",Context.MODE_PRIVATE);
        if(!user.getBoolean("LoggedIn",false)){
            startActivity(new Intent(UserActivity.this, FirstActivity.class));
        }
        View header = navigationView.inflateHeaderView(R.layout.nav_header_user);
        ((TextView) header.findViewById(R.id.nav_name)).setText(user.getString("name","Name"));
        ((TextView) header.findViewById(R.id.nav_email)).setText(user.getString("email", "Email"));


        // POST req to get user data
        (new GetUserData()).execute(
                "/app/getUserData",
                "id="+user.getString("id",null)
        );
        /*===========================*/
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            getSharedPreferences("userDetails", Context.MODE_PRIVATE).edit().clear().putBoolean("LoggedIn", false).commit();
            startActivity(new Intent(UserActivity.this, FirstActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkIfNetworkIsConnected(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())){
            startActivity(new Intent(UserActivity.this,FirstActivity.class));
        }
    }

    private class GetUserData extends HttpPost {
        public ProgressDialog progress;

        @Override
        protected void onPreExecute(){
            progress= new ProgressDialog(UserActivity.this);
            progress.setMessage("Loading...");
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(String res){
            progress.dismiss();
            if(!res.equals("")){
                if(res.equals("invalid")){
                    getSharedPreferences("userDetails", Context.MODE_PRIVATE).edit().clear().putBoolean("LoggedIn", false).commit();
                    startActivity(new Intent(UserActivity.this, FirstActivity.class));
                } else {
                    try {
                        JSONObject userData = new JSONObject(res);

                        JSONArray userSkills = new JSONArray(userData.getString("skills"));
                        JSONArray certificates = new JSONArray(userData.getString("certificates"));
                        JSONArray clients = new JSONArray(userData.getString("clients"));

                        /*String certificateString = userData.getString("certificates");
                        String[] certificates = certificateString.substring(1,certificateString.length()-1).split(",");
                        for(int i=0;i<certificates.length;i++){
                            certificates[i] = certificates[i].substring(1,certificates[i].length()-1);
                        }

                        String clientString = userData.getString("clients");
                        String[] clients = clientString.substring(1,clientString.length()-1).split(",");
                        for(int i=0;i<clients.length;i++){
                            clients[i] = clients[i].substring(1,clients[i].length()-1);
                        }*/

                        JSONObject skill;
                        ArrayList ReadSkillsAdapterArray = new ArrayList();
                        String role,skillName,skillType;
                        for(int i=0;i<userSkills.length();i++){
                            skill = userSkills.getJSONObject(i);
                            role = skill.getString("role");
                            skillName = skill.getString("skillName");
                            skillType = skill.getString("skillType");
                            ReadSkillsAdapterArray.add(role+"-"+skillName+"-"+skillType);
                        }

                        ArrayList ReadCertAdapterArray = new ArrayList();
                        for(int i=0;i<certificates.length();i++){
                            ReadCertAdapterArray.add(certificates.getString(i));
                        }

                        ArrayList ReadCliAdapterArray = new ArrayList();
                        for(int i=0;i<clients.length();i++){
                            ReadCliAdapterArray.add(clients.getString(i));
                        }



                        ArrayAdapter<String> ReadSkillAdapter = new ArrayAdapter<String>(
                                UserActivity.this,
                                R.layout.read_skill_list_item,
                                R.id.read_skill_textview,
                                ReadSkillsAdapterArray
                        );
                        ((ListView) findViewById(R.id.read_skills)).setAdapter(ReadSkillAdapter);

                        ArrayAdapter<String> ReadCertAdapter = new ArrayAdapter<String>(
                                UserActivity.this,
                                R.layout.read_skill_list_item,
                                R.id.read_skill_textview,
                                ReadCertAdapterArray
                        );
                        ((ListView) findViewById(R.id.read_certificates)).setAdapter(ReadCertAdapter);

                        ArrayAdapter<String> ReadCliAdapter = new ArrayAdapter<String>(
                                UserActivity.this,
                                R.layout.read_skill_list_item,
                                R.id.read_skill_textview,
                                ReadCliAdapterArray
                        );
                        ((ListView) findViewById(R.id.read_clients)).setAdapter(ReadCliAdapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                ((TextView) findViewById(R.id.error_msg)).setText("Problem occured while authenticating");
            }
        }
    }


}
