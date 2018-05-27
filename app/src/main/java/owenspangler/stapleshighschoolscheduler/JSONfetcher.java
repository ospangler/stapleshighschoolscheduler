package owenspangler.stapleshighschoolscheduler;

import android.os.AsyncTask;
///
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
//import org.json.simple.parser.*;
import java.io.FileReader;
///
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
//
///Portions of Below Code are taken or derived from code made by Abhishek Panwar, 2017///
///The original code can be found at https://abhishekprogramming.blogspot.in/2017/07/json-fetching-and-parsing-android-studio.html///
public class JSONfetcher extends AsyncTask<Void,Void,Void>{
    ///
    String dayletter;
    int month;
    int day;
    //String newscheduleformatSTR;
    String starttimesSTR;
    String endtimesSTR;
    //int[] newscheduleformat;
    String newscheduleformat;
    int[][] starttimes;
    int[][] endtimes;
    String notice;
    String linktopdf;
    String[] arr;


    ///
    String data ="";
    String dataParsed = "";
    String singleParsed ="";
    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL("https://ospangler.github.io/schedulechangedatabase.json");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";

            while(line != null){
                bufferedReader.readLine();
                dayletter =  bufferedReader.readLine();
                data = data + line;
            }

       JSONObject JO = new JSONObject(data);
            //dayletter = JO.get("dayletter");
            //JSONArray arr = new JSONArray(data);
            //JSONObject jObj = arr.getJSONObject(1);
            //dayletter = jObj.getString("dayletter");
            //Object obj = new ().parse(new FileReader("JSONExample.json"));
            //JSONArray JA = new JSONArray(data);


/*
            dayletter = jsonfile.getString("dayletter");
            month = jsonfile.getInt("month");
            day = jsonfile.getInt("day");
            newscheduleformat = jsonfile.getString("newscheduleformat");
            //starttimes = jsonfile.getString("starttimes");
            //endtimes = jsonfile.getString("endtimes");
            notice = jsonfile.getString("notice");
            linktopdf = jsonfile.getString("linktopdf");
*/
            //JSONArray arraynewscheduleformat = jsonfile.optJSONArray("newscheduleformat");

// = jsonObj.optJSONObject("data");



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
          e.printStackTrace();
        }

        return null;
    }
    public String geter(){
        return dayletter;
    }
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        //MainPage.data.setText(this.dataParsed);

    }
}
///END CODE ATTRIBUTION HERE///

