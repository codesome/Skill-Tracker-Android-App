package com.ibm.in.skilltracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class EditClientFragment extends Fragment {

    public ProgressDialog progress;

    private int startClientID = 1117 , endClientID , cbllClientKey = 3323 , dClientKey = 4723 , lwClientKey = 6343 ;


    public EditClientFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshUserClients();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_client, container, false);
    }


    private void refreshUserClients(){
        SharedPreferences user = getActivity().getSharedPreferences("userDetails",Context.MODE_PRIVATE);
        (new GetUserClients()).execute(
                "/app/getAllClients",
                "id=" + user.getString("id", null)
        );
    }











    private void checkIfNetworkIsConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            startActivity(new Intent(getActivity(),FirstActivity.class));
        }
    }







    /* To get the User Data */
    private class GetUserClients extends HttpPost {

        @Override
        protected void onPreExecute(){
            checkIfNetworkIsConnected();
            progress= new ProgressDialog(getActivity());
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

                LinearLayout clientLayout = (LinearLayout) getView().findViewById(R.id.edit_clients);
                clientLayout.removeAllViews();

                CheckBox c;
                LinearLayout ll;
                String text;
                int checkboxID;
                JSONObject client;
                for(int i=0;i<allClients.length();i++){
                    checkboxID = startClientID+i;

                    client = allClients.getJSONObject(i);

                    c = new CheckBox(getActivity());
                    text = client.getString("data");
                    c.setText(text);
                    c.setId(checkboxID);
                    final int finalCheckboxID = checkboxID;
                    c.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CheckBox c = (CheckBox) v;
                            LinearLayout l = (LinearLayout) getView().findViewById(finalCheckboxID + cbllClientKey);
                            if (c.isChecked()) {
                                Spinner spinner = new Spinner(getActivity());

                                ArrayList<String> spinnerArray = new ArrayList<String>();
                                spinnerArray.add("Duration");
                                for (int i = 1; i <= 15; i++) {
                                    spinnerArray.add("" + i);
                                }
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_format, spinnerArray);
                                spinner.setAdapter(spinnerArrayAdapter);
                                spinner.setId(finalCheckboxID + dClientKey);
                                l.addView(spinner);

                                spinner = new Spinner(getActivity());
                                spinnerArray = new ArrayList<String>();
                                spinnerArray.add("Last Worked");
                                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                                for (int i = 1990; i <= currentYear; i++) {
                                    spinnerArray.add("" + i);
                                }
                                spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_format, spinnerArray);
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

                    ll = new LinearLayout(getActivity());
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

                Button b = new Button(getActivity());
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
                                c = (CheckBox) getView().findViewById(i);
                                if (c.isChecked()) {
                                    d = ((Spinner) getView().findViewById(i + dClientKey));
                                    lw = ((Spinner) getView().findViewById(i + lwClientKey));
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
                            SharedPreferences user = getActivity().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
                            (new UpdateUserClients()).execute(
                                    "/app/UpdateUserClients",
                                    "id=" + user.getString("id", null) + "&clients=" + clients.toString()
                            );
                        }
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
            progress= new ProgressDialog(getActivity());
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
                for(int i = startClientID+cbllClientKey ; i<= endClientID+cbllClientKey ; i++){
                    ll = (LinearLayout) getView().findViewById(i);
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
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

}
