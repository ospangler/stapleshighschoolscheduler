package owenspangler.stapleshighschoolscheduler;
///
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
///
///Portions of Below Code are taken or derived from code made by Abhishek Panwar, 2017///
///The original code can be found at https://github.com/panwarabhishek345/Receive-JSON-Data///
public class JSONfetcher extends AsyncTask<Void,Void,Void> {
    ///
    String dayletter = "";
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
    String data = "";

    @Override
    public Void doInBackground(Void... voids) {
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
            //println(data);
            Log.d("helpme", data);
            JSONObject JO = new JSONObject(data);
            dayletter = JO.getString("dayletter");
            Log.d("dayletter", dayletter);
            //println(dayletter);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String geter() {
        return dayletter;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        //MainPage.data.setText(this.dataParsed);

    }
}
///END CODE ATTRIBUTION HERE///
