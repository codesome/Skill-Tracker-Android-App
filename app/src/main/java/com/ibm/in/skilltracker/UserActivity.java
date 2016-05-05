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
import android.view.SubMenu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class UserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private int EmployeeCount;
    private JSONArray employees;
    private final int EmpKey = 3593;
    public ProgressDialog progress;

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


        /*===========================*/
        SharedPreferences user = getSharedPreferences("userDetails",Context.MODE_PRIVATE);
        if(!user.getBoolean("LoggedIn",false)){
            startActivity(new Intent(UserActivity.this, FirstActivity.class));
        }

        // To set header
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.inflateHeaderView(R.layout.nav_header_user);
        ((TextView) header.findViewById(R.id.nav_name)).setText(user.getString("name", "Name"));
        ((TextView) header.findViewById(R.id.nav_email)).setText(user.getString("email", "Email"));

        EmployeeCount = 0;
        progress= new ProgressDialog(UserActivity.this);
        progress.setMessage("Loading...");
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        progress.show();
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

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.logout){
            getSharedPreferences("userDetails", Context.MODE_PRIVATE).edit().clear().putBoolean("LoggedIn", false).commit();
            startActivity(new Intent(UserActivity.this, FirstActivity.class));
            finish();
        } else if (id == R.id.nav_edit){
            startActivity(new Intent(UserActivity.this,EditActivity.class));
        } else if (id == R.id.nav_home){
            startActivity(new Intent(UserActivity.this,UserActivity.class));
            finish();
        } else if (id%EmpKey == 0){
            int empId = id/EmpKey -1 ;
            try {
                Intent intent = new Intent(UserActivity.this,EmployeeView.class);
                intent.putExtra("id",employees.getJSONObject(empId).getString("id"));
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkIfNetworkIsConnected(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            startActivity(new Intent(UserActivity.this,FirstActivity.class));
        }
    }

    /* To get the User Data */
    private class GetUserData extends HttpPost {

        @Override
        protected void onPreExecute(){
            checkIfNetworkIsConnected();
        }

        @Override
        protected void onPostExecute(String res){
            progress.dismiss();
            if(!res.equals("")){
                if(res.equals("invalid")){
                    progress.dismiss();
                    getSharedPreferences("userDetails", Context.MODE_PRIVATE).edit().clear().putBoolean("LoggedIn", false).commit();
                    startActivity(new Intent(UserActivity.this, FirstActivity.class));
                } else {
                    try {
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
                                TextView t = new TextView(UserActivity.this);
                                t.setText(role + "-" + skillName + "-" + skillType);
                                readSkills.addView(t);
                            }
                        } else {
                            TextView t = new TextView(UserActivity.this);
                            t.setText("None");
                            readSkills.addView(t);
                        }

                        // Adding certificates to read
                        LinearLayout readCert = (LinearLayout) findViewById(R.id.read_certificates);
                        if(certificates.length()!=0) {
                            JSONObject obj;
                            for (int i = 0; i < certificates.length(); i++) {
                                TextView t = new TextView(UserActivity.this);
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
                            TextView t = new TextView(UserActivity.this);
                            t.setText("None");
                            readCert.addView(t);
                        }

                        // Adding clients to read
                        LinearLayout readCli = (LinearLayout) findViewById(R.id.read_clients);
                        if(clients.length()!=0) {
                            for (int i = 0; i < clients.length(); i++) {
                                TextView t = new TextView(UserActivity.this);
                                t.setText(clients.getString(i));
                                readCli.addView(t);
                            }
                        } else {
                            TextView t = new TextView(UserActivity.this);
                            t.setText("None");
                            readCli.addView(t);
                        }

                        progress.dismiss();

                        (new GetEmployeeData()).execute(
                                "/app/getEmployeeData",
                                "id="+userData.getString("_id")
                        );

                    } catch (JSONException e) {
                        progress.dismiss();
                        e.printStackTrace();
                    }
                }
            } else {
                progress.dismiss();
                ((TextView) findViewById(R.id.error_msg)).setText("Problem occured while authenticating");
            }
        }
    }

    /* To get the User Data */
    private class GetEmployeeData extends HttpPost {

        @Override
        protected void onPreExecute(){
            checkIfNetworkIsConnected();
            progress= new ProgressDialog(UserActivity.this);
            progress.setMessage("Fetching latest data");
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(String res){
            progress.dismiss();
            if(!res.equals("")){
                if(res.equals("invalid")){
                    progress.dismiss();
                    getSharedPreferences("userDetails", Context.MODE_PRIVATE).edit().clear().putBoolean("LoggedIn", false).commit();
                    startActivity(new Intent(UserActivity.this, FirstActivity.class));
                } else {
                    try {

                        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                        final Menu menu = navigationView.getMenu();

                        /* To display the employee on Nav bar */
                        employees = new JSONArray(res);
                        if(employees.length()!=0){
                            final SubMenu EmpSubMenu = menu.addSubMenu("Employees");
                            JSONObject emp;
                            for(int i=0;i<employees.length();i++){
                                EmployeeCount++;
                                emp = employees.getJSONObject(i);
                                EmpSubMenu.add(Menu.NONE,EmpKey*EmployeeCount,Menu.NONE,emp.getString("name"));
                            }
                            if(EmployeeCount==0){
                                EmpSubMenu.add("None have updated their data");
                            }
                        }

                        // Logout button
                        final SubMenu subMenu = menu.addSubMenu("");
                        subMenu.add(Menu.NONE,R.id.logout,Menu.NONE,R.string.title_logout);
                        progress.dismiss();
                    } catch (JSONException e) {
                        progress.dismiss();
                        e.printStackTrace();
                    }
                }
            } else {
                progress.dismiss();
                ((TextView) findViewById(R.id.error_msg)).setText("Problem occured while fetching data");
            }
        }
    }

    private void toastMessage(String message){
        Toast.makeText(UserActivity.this, message, Toast.LENGTH_SHORT).show();
    }


}
