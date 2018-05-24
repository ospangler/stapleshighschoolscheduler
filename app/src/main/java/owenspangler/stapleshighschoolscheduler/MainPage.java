package owenspangler.stapleshighschoolscheduler;

import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import android.util.Log;
import android.widget.TextView;
import java.nio.charset.Charset;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Object;
/*
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
*/
//import org.apache.http.client.HttpClient;
//import org.apache.http.impl.client.DefaultHttpClient;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
//Button btnHit;
//TextView txtJson;
//ProgressDialog pd;
////


public class MainPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
//get rid of below code and put in separate functions with controls for -1
        int temp = PeriodNumber();
        int[] tempsced = ScheduleFormat('a');
        int periodforreals = tempsced[temp - 1];
        String tempstring = Integer.toString(periodforreals);
        TextView textView1 = findViewById(R.id.textView1);
        textView1.setText(tempstring);


    }
////ESTABLISH CONNECTION
URL url = new URL("http://www.google.com");
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
   try {
        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        readStream(in);
    } finally {
        urlConnection.disconnect();
    }


    ///
    //Public Vars
    char dayLetter = 'z';
    String temperature = JSONObject(html).getString("name");///////FIX AND ADD HTML CODE
    //TimeZone tz;
    boolean passingTime = false;
    boolean noSchool = false;

    Calendar cal = Calendar.getInstance();
    int currentDayNum = cal.get(Calendar.DAY_OF_MONTH);
    int currentDayDay = cal.get(Calendar.DAY_OF_WEEK);
    int currentMonth = cal.get(Calendar.MONTH);

    int currentHour = cal.get(Calendar.HOUR_OF_DAY);
    //int currentHour = 8;
    int currentMinute = cal.get(Calendar.MINUTE);
    //int currentMinute = 15;
    int currentSecond = cal.get(Calendar.SECOND);


    int[] ScheduleFormat(char inputDayType) {//edit here for schedule changes

        if (inputDayType == 'a') {
            int[] tempA = {1, 2, 3, 5, 8, 7}; //A day
            return tempA;
        } else if (inputDayType == 'b') {
            int[] tempB = {2, 3, 4, 6, 7, 8}; //B day
            return tempB;
        } else if (inputDayType == 'c') {
            int[] tempC = {3, 4, 1, 7, 6, 5}; //C day
            return tempC;
        } else if (inputDayType == 'd') {
            int[] tempD = {4, 1, 2, 8, 5, 6}; //D day
            return tempD;
        } else {
            //CALL FUNCTION TO PULL DATA FROM SERVER HERE
            return null;
        }
    }

    int PeriodNumber() {
        int i = 0; //array position
        passingTime = false;//If set to true in function, school is in passing time, this line resets.
        noSchool = false;//If set to tru in function, is before or after school, this line resets.

        if ((currentHour < 7) || ((currentHour == 7) && (currentMinute < 30)) || (currentHour > 14) || ((currentHour == 14) && (currentMinute >= 15))) {
            noSchool = true;
            return -1;
        }

        int[][] periodTimes = //CHANGE BELOW TIMES WHEN SCHEDULE CHANGES
                {{7, 8, 9, 10, 12, 13},//START HOUR
                 {30, 25, 50, 45, 30, 25},//START MINUTE

                 {8, 9, 10, 12, 13, 14},//END HOUR
                 {25, 45, 40, 25, 20, 15}};//END MINUTE

        while (true) {
            if ((currentHour > periodTimes[2][i])) {
                i++;
            } else if ((currentHour == periodTimes[2][i]) && (currentMinute > periodTimes[3][i])) {
                i++;
            } else if ((currentHour == periodTimes[0][i]) && (currentMinute > periodTimes[1][i])) {
                break;
            } else if (currentHour > periodTimes[0][i]) {
                break;
            } else {
                passingTime = true;
                return -1;
            }
        }
        return (i + 1);
    }
}


