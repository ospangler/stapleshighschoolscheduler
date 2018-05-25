package owenspangler.stapleshighschoolscheduler;

import android.os.AsyncTask;
///
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
///
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
    String newscheduleformatSTR;
    String starttimesSTR;
    String endtimesSTR;
    int[] newscheduleformat;
    int[][] starttimes;
    int[][] endtimes;
    String notice;
    String linktopdf;



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
                line = bufferedReader.readLine();
                data = data + line;
            }

            JSONObject jsonfile = new JSONObject(data);

            dayletter = jsonfile.getString("dayletter");
            month = jsonfile.getInt("month");
            day = jsonfile.getInt("day");
            //newscheduleformat = jsonfile.getString("newscheduleformat");
            //starttimes = jsonfile.getString("starttimes");
            //endtimes = jsonfile.getString("endtimes");
            //notice = jsonfile.getString("notice");
            linktopdf = jsonfile.getString("linktopdf");
/////
            String s="[[4, 2, 2, 4], [3, 4, 5, 6], [6, 7, 8, 9], [3, 2, 1, 4]]";
            s=s.replace("[","");//replacing all [ to ""
            s=s.substring(0,s.length()-2);//ignoring last two ]]
            String s1[]=s.split("],");//separating all by "],"

            String my_matrics[][] = new String[s1.length][s1.length];//declaring two dimensional matrix for input

            for(int i=0;i<s1.length;i++){
                s1[i]=s1[i].trim();//ignoring all extra space if the string s1[i] has
                String single_int[]=s1[i].split(", ");//separating integers by ", "

                for(int j=0;j<single_int.length;j++){
                    my_matrics[i][j]=single_int[j];//adding single values
                }
            }
////


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        //MainPage.data.setText(this.dataParsed);

    }
}
///END CODE ATTRIBUTION HERE///

