package owenspangler.stapleshighschoolscheduler;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;

class JSONfetcher extends AsyncTask<String, Integer, String> {

    String data = ""; //full raw data retrieved

    @Override
    protected void onPreExecute() {

        //ADD SOME SORT OF LOADING SCREEN HERE IF POSSIBLE WHILE NEW DATA IS RETRIEVED

        //progress.setMessage("Analysing");
        //progress.setIndeterminate(true);
        //progress.show();
    }

    @Override
    protected String doInBackground(String... String) {

        if (InternetConnected()) { //There is an internet connection, will download data from server
            RetrieveJson();
            return data;
        } else { //There is no internet connection, user will have to input day letter

            return "NO CONNECTION";
        }
    }

    void RetrieveJson() {

        //START CODE ATTRIBUTION HERE
        //Portions of Below Code are from code made by Abhishek Panwar
        //The original code can be found at: https://github.com/panwarabhishek345/Receive-JSON-Data
        try {
            URL url = new URL("https://ospangler.github.io/SHSS/schedulechangedatabase_v2.json");
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
            ////
            httpURLConnection.disconnect();//see if this fixes leaking of response body
            ////

            //END CODE ATTRIBUTION FROM Abhishek Panwar
        } catch (MalformedURLException e) {
            //Something is wrong with the URL you put in here. Fix it.
            e.printStackTrace();
        } catch (IOException e) {
            //I don't know what you did, but you better fix it.
            e.printStackTrace();
        }
    }

    private boolean InternetConnected() { //Checks for an internet connection to test for offline status to github site

        //The below code was adapted from a StackOverflow answer by YLS
        //The full answer can be found here: https://stackoverflow.com/a/40111665
        boolean success = false;

        try {
            URL url = new URL("https://ospangler.github.io");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.connect();
            success = connection.getResponseCode() == 200;
            connection.disconnect(); //check to see if this prevents leaking of response body
        } catch (IOException e) {
            e.printStackTrace();
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

