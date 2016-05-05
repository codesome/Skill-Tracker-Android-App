package com.ibm.in.skilltracker;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpPost extends AsyncTask<String, Void, String> {

    private String rootURL = "http://192.168.1.33:3000";

    @Override
    protected String doInBackground(String... params) {
        try {

            /*
            * parama[0] = request url
            * params[1] = extra parameters
            * */

            URL url = new URL(rootURL+params[0]);

            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            // Adding extra content
            String urlParameters = params[1];
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.writeBytes(urlParameters);
            dStream.flush();
            dStream.close();


            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder responseOutput = new StringBuilder();
            while((line = br.readLine()) != null ) {
                responseOutput.append(line);
            }
            br.close();


            return responseOutput.toString();


        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}


