package owenspangler.stapleshighschoolscheduler;

//import android.net.sip.SipSession;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Calendar;

class JSONfetcher extends AsyncTask<String,Integer,String> {
    ///Vars Below///
    String data = "";
    ///End Vars Section///

    @Override
    protected void onPreExecute() {

        //progress.setMessage("Analysing");
        //progress.setIndeterminate(true);
        //progress.show();
    }

    @Override
    protected String doInBackground(String...String){
        RetrieveJson();
        return data;///redundant
    }

void RetrieveJson(){
        ///START CODE ATTRIBUTION HERE///
        ///Portions of Below Code is from code made by Abhishek Panwar, 2017
        ///The original code can be found at https://github.com/panwarabhishek345/Receive-JSON-Data
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

    } catch (MalformedURLException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
    ///END CODE ATTRIBUTION HERE///
}

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //set text here and do some cool stuff here later
        //runs when program is finished
    }
}

