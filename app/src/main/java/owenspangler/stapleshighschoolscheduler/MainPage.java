package owenspangler.stapleshighschoolscheduler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class MainPage extends AppCompatActivity {

    String data = "";
    String jsonData;
    String jsonDayLetter = "";
    int jsonMonth = -1;
    int jsonDay = -1;
    int[] jsonNewScheduleFormat;
    int[][] jsonPeriodTimes;
    String jsonNotice;
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
    ///END VARS///

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        try {
            jsonData = new JSONfetcher().execute().get();
        }catch (InterruptedException e){

        }catch (ExecutionException e) {

        }
        //JSONfetcher process = new JSONfetcher();
        //process.execute().get();
        //RetrieveJson();
        GetInfoFromJSON(jsonData);
        Log.e("This is working????", Arrays.deepToString(jsonPeriodTimes));
    }

    void GetInfoFromJSON(String inputdata){
        try {
            JSONObject JO = new JSONObject(inputdata);
            jsonDayLetter= JO.getString("dayletter");
            jsonMonth = JO.getInt("month");
            jsonDay = JO.getInt("day");

            jsonNewScheduleFormat = getArrayFromJSON("newscheduleformat");
            int[] tempJsonStartTimesHour = getArrayFromJSON("starttimeshour");
            int[] tempJsonStartTimesMinute = getArrayFromJSON("starttimesminute");
            int[] tempJsonEndTimesHour = getArrayFromJSON("endtimeshour");
            int[] tempJsonEndTimesMinute = getArrayFromJSON("endtimesminute");

            jsonPeriodTimes = new int[3][tempJsonStartTimesHour.length];
            for(int i = 0; i<3; i++){
                for(int j = 0; j<tempJsonStartTimesHour.length; j++){
                    if(i == 0) {
                        jsonPeriodTimes[i][j] = tempJsonStartTimesHour[j];
                    }else if(i==1){
                        jsonPeriodTimes[i][j] = tempJsonStartTimesMinute[j];
                    }else if(i==2){
                        jsonPeriodTimes[i][j] = tempJsonEndTimesHour[j];
                    }else if(i==3){
                        jsonPeriodTimes[i][j] = tempJsonEndTimesMinute[j];
                    }
                }

            }
            Log.i("wwwww", Arrays.deepToString(jsonPeriodTimes));
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    int[] getArrayFromJSON(String jsonID) {
        try {
            JSONObject tempJO = new JSONObject(jsonData);
            JSONArray tempJsonArray = tempJO.optJSONArray(jsonID);
            if (tempJsonArray == null) { /*ENTER SOME ERROR CODE HERE*/ }
            int[] output = new int[tempJsonArray.length()];
            for (int i = 0; i < tempJsonArray.length(); ++i) {
                output[i] = tempJsonArray.optInt(i);
            }
            return output;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    int[] ScheduleFormat(String inputDayType) { //edit here for schedule changes

        if (inputDayType.equals("a")) {
            int[] tempA = {1, 2, 3, 5, 8, 7}; //'A' day
            return tempA;
        } else if (inputDayType.equals("b")) {
            int[] tempB = {2, 3, 4, 6, 7, 8}; //'B' day
            return tempB;
        } else if (inputDayType.equals("c")) {
            int[] tempC = {3, 4, 1, 7, 6, 5}; //'C' day
            return tempC;
        } else if (inputDayType.equals("d")) {
            int[] tempD = {4, 1, 2, 8, 5, 6}; //'D' day
            return tempD;
        } else {
            //CALL FUNCTION TO PULL DATA FROM SERVER HERE
            return null;
        }
    }

    int PeriodNumber() { //NOTE: ADD SAFEGUARDS TO PREVENT ARRAY READ AT -1!!!!!
        int i = 0; //array position
        passingTime = false;//If set to true in function, school is in passing time, this line resets.
        noSchool = false;//If set to true in function, is before or after school, this line resets.

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
        return (i + 1);//returns period number, must subtract one to get proper array position
    }
}
