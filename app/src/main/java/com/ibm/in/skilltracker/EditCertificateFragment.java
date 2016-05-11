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

public class EditCertificateFragment extends Fragment {


    public ProgressDialog progress;

    private int startCertID = 7109  , endCertID , cbllCertKey = 8231 , dCertKey = 9319 , lwCertKey = 10079 ;


    public EditCertificateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshUserCertificates();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_certificate, container, false);
    }









    private void checkIfNetworkIsConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            startActivity(new Intent(getActivity(),FirstActivity.class));
        }
    }



    private void refreshUserCertificates(){
        SharedPreferences user = getActivity().getSharedPreferences("userDetails",Context.MODE_PRIVATE);
        (new GetUserCertificates()).execute(
                "/app/getAllCertificates",
                "id=" + user.getString("id", null)
        );
    }




    private class GetUserCertificates extends HttpPost {

        @Override
        protected void onPreExecute(){
            checkIfNetworkIsConnected();
            progress= new ProgressDialog(getActivity());
            progress.setMessage("Fetching Certificates");
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(String res){

            try {
                progress.setMessage("Updating Certificates");
                JSONArray allCerts= new JSONArray(res);

                LinearLayout certLayout = (LinearLayout) getView().findViewById(R.id.edit_certificates);
                certLayout.removeAllViews();

                CheckBox c;
                LinearLayout ll;
                String text;
                int checkboxID;
                JSONObject cert;
                for(int i=0;i<allCerts.length();i++){
                    checkboxID = startCertID+i;

                    cert = allCerts.getJSONObject(i);

                    c = new CheckBox(getActivity());
                    text = cert.getString("data");
                    c.setText(text);
                    c.setId(checkboxID);
                    final int finalCheckboxID = checkboxID;
                    c.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CheckBox c = (CheckBox) v;
                            LinearLayout l = (LinearLayout) getView().findViewById(finalCheckboxID + cbllCertKey);
                            if (c.isChecked()) {
                                Spinner spinner = new Spinner(getActivity());

                                ArrayList<String> spinnerArray = new ArrayList<String>();
                                spinnerArray.add("Year");
                                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                                for (int i = 1990; i <= currentYear; i++) {
                                    spinnerArray.add("" + i);
                                }
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_format, spinnerArray);
                                spinner.setAdapter(spinnerArrayAdapter);
                                spinner.setId(finalCheckboxID + dCertKey);
                                l.addView(spinner);

                                spinner = new Spinner(getActivity());
                                spinnerArray = new ArrayList<String>();
                                spinnerArray.add("Month");
                                for (int i = 1990; i <= currentYear; i++) {
                                    spinnerArray.add("" + i);
                                }
                                spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_format, spinnerArray);
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

                    ll = new LinearLayout(getActivity());
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

                Button b = new Button(getActivity());
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
                                c = (CheckBox) getView().findViewById(i);
                                if (c.isChecked()) {
                                    d = ((Spinner) getView().findViewById(i + dCertKey));
                                    lw = ((Spinner) getView().findViewById(i + lwCertKey));
                                    if(d!=null && lw!=null) {
                                        dText = d.getSelectedItem().toString();
                                        lwText = lw.getSelectedItem().toString();
                                        if (dText.equals("Year")) {
                                            flag = false;
                                            toastMessage("Select Year for all Certificates");
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
                            SharedPreferences user = getActivity().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
                            (new UpdateUserCertificates()).execute(
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

    private class UpdateUserCertificates extends HttpPost {

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
                for(int i = startCertID+cbllCertKey; i<= endCertID+cbllCertKey; i++){
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
