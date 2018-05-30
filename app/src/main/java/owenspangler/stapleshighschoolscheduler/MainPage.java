package owenspangler.stapleshighschoolscheduler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class MainPage extends AppCompatActivity {

    //String data = "";
    String jsonData; //Raw Json String Pulled From Async Task
    String jsonDayLetter = "";
    int jsonMonth = -1;
    int jsonDay = -1;
    int[] jsonNewScheduleFormat;
    int[] jsondayLetterList;
    int[] jsondayLetterDayNumber;
    int[][] jsonPeriodTimes;
    String jsonNotice;
    boolean passingTime = false;
    boolean noSchool = false;
    boolean useHardCoded = false;
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
            jsonData = new JSONfetcher().execute().get();//Will wait for task to finish
        }catch (InterruptedException e){
            //I'm not catching anything, I just wanted the error messages to go away
        }catch (ExecutionException e) {
            //I don't care if I'm defeating the purpose of an Async task, I don't care.
        }
        if(jsonData.equals("NO CONNECTION")){
            Log.e("JSONDATA", "JSONDATA can't be reached, reverting to hardcoded backup");
            useHardCoded = true;
        }else if(jsonData.equals("")){
            Log.e("JSONDATA", "JSONDATA is null, reverting to hardcoded backup");
            useHardCoded = true;
        }else {//normal condition
            GetInfoFromJSON(jsonData);

            if((jsonMonth == currentMonth) && jsonDay == currentDayNum){
                useHardCoded = false;
            }else{
                useHardCoded = true;
            }
        }
    }

    void GetInfoFromJSON(String inputdata){
        try {
            JSONObject JO = new JSONObject(inputdata);
            jsonDayLetter= JO.getString("dayletter");
            jsonMonth = JO.getInt("month");
            jsonDay = JO.getInt("day");

            jsonNewScheduleFormat = getArrayFromJSON("newscheduleformat");
            jsondayLetterList = getArrayFromJSON("dayletterlist");
            jsondayLetterDayNumber = getArrayFromJSON("dayletterdaynumber");

            int[] tempJsonStartTimesHour = getArrayFromJSON("starttimeshour");
            int[] tempJsonStartTimesMinute = getArrayFromJSON("starttimesminute");
            int[] tempJsonEndTimesHour = getArrayFromJSON("endtimeshour");
            int[] tempJsonEndTimesMinute = getArrayFromJSON("endtimesminute");

            jsonPeriodTimes = new int[4][tempJsonStartTimesHour.length];
            for(int i = 0; i<4; i++){
                for(int j = 0; j<tempJsonStartTimesHour.length; j++){
                    if(i == 0) {
                        jsonPeriodTimes[i][j] = tempJsonStartTimesHour[j];
                    }else if(i==1){
                        jsonPeriodTimes[i][j] = tempJsonStartTimesMinute[j];
                    }else if(i==2){
                        jsonPeriodTimes[i][j] = tempJsonEndTimesHour[j];
                    }else{
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

/*
    String FindDayLetter(){
        Log.i("LOOP??","REACHED STEP 1");
        int dayLetterCounter = 0;//A = 0, B = 1, C = 2, D = 3
        //if((jsonData.equals("")) || (jsonData.equals("NO CONNECTION"))) {//Backup in case of network failure
            //int tempADayReferenceMonth = 4;
            //int tempADayReferenceDay = 10;
            //int tempDayofWeek = 1;//Sunday is day 0, Saturday is day 6
        //}else{
            //normal conditions

            if ((jsonnumoflastKnownADay >= 0) && (jsonnumoflastKnownADay<=7)){

                int tempMonth = jsonlastKnownADayMonth;
                int tempDay = jsonlastKnownADayDay;
                int tempDayNum = jsonnumoflastKnownADay;
                //Calendar tempCal = new Calendar(2018, tempMonth, tempDay);

                Log.i("LOOP??","REACHED STEP 2");

                while((currentMonth != tempMonth) && (currentDayDay != tempDay)){
                    //Log.i("LOOP??","REACHED STEP 3");
                    if(tempDay >= amountOfDaysInMonth(tempMonth)) {
                        tempDay = 1;
                        if (tempMonth == 12) {
                            tempMonth = 1;
                        } else {
                            tempMonth++;
                        }
                        //dayLeterCounter++;
                    }
                    if(tempDayNum == 7){
                    dayLetterCounter++;
                    tempDayNum = 1;
                    }else if ((tempDayNum>=1) && (tempDayNum <=6)){
                        dayLetterCounter++;
                        tempDayNum++;
                    }
                }
            }else{
                Log.e("LAST KNOWN A DAY", "You done goofed and set the last known A day to a weekend. Fix it");
            }
            Log.i("LOOP??","REACHED STEP 4");
        //}
Log.i("A DAY COUNTER", Integer.toString(dayLetterCounter));
        if(dayLetterCounter%4 == 0){
            return "A";
        }else if(dayLetterCounter%4 == 1){
            return "B";
        }else if(dayLetterCounter%4 ==2){
            return "C";
        }else{
            return "D";
        }
    }

    int amountOfDaysInMonth(int inputMonth){
        if((inputMonth == 1)||(inputMonth == 3)||(inputMonth == 5)||(inputMonth == 7)||(inputMonth == 8)||(inputMonth == 10)||(inputMonth == 12)){
            return 31;
        }else if((inputMonth == 4)||(inputMonth == 6)||(inputMonth == 9)||(inputMonth == 11)){
            return 30;
        }else{
            return 28; //Change for leap years
        }
    }
*/
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
