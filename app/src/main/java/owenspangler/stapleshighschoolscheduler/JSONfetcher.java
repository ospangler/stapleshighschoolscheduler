package owenspangler.stapleshighschoolscheduler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.Response;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;

class JSONfetcher extends AsyncTask<String,Integer,String> {
    ///Vars Below///
    String data = "";
    ///End Vars Section///

    @Override
    protected void onPreExecute() {
        //ADD SOME SORT OF LOADING SCREEN HERE IF POSSIBLE WHILE NEW DATA IS RETRIVED

        //progress.setMessage("Analysing");
        //progress.setIndeterminate(true);
        //progress.show();
    }

    @Override
    protected String doInBackground(String...String){
        if(InternetConnected()) {
            Log.i("INTERNET CONNECTION", "There is an internet connection, will check for changes");
            RetrieveJson();
            return data;
        }else{
            Log.e("INTERNET CONNECTION","There is no internet connection, relying on hardcoded backup");
            return "NO CONNECTION";
        }
    }

void RetrieveJson(){
        //START CODE ATTRIBUTION HERE
        //Portions of Below Code are from code made by Abhishek Panwar (2017)
        //The original code can be found at https://github.com/panwarabhishek345/Receive-JSON-Data
    try {
        URL url = new URL("https://ospangler.github.io/schedulechangedatabase.json");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";

        while (true) {
            line = bufferedReader.readLine();
            if (line == null) {
                break;
            }
            data = data + line;
        }
    //END CODE ATTRIBUTION HERE
    } catch (MalformedURLException e) {
        Log.e("MalformedURL", "Something is wrong with the URL you put in here. Fix it.");
        e.printStackTrace();
    } catch (IOException e) {
        Log.e("IOException", "I don't know what you did, but you better fix it");
        e.printStackTrace();
    }
}

    public boolean InternetConnected() {
        //The below code was adapted from a StackOverflow answer by YLS
        //The full answer can be found here: https://stackoverflow.com/a/40111665
        boolean success = false;
        int responseCode = 0;
        try {
            URL url = new URL("https://ospangler.github.io");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.connect();
            responseCode = connection.getResponseCode();
            success = responseCode == 200;
        } catch (IOException e) {
            e.printStackTrace();
            //Maybe Add Response Codes Here Later
        }
        return success;
        //END CODE ATTRIBUTION by YLS
    }



    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //set text here and do something later
    }
}

