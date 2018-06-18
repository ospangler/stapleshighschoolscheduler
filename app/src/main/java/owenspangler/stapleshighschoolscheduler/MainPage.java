package owenspangler.stapleshighschoolscheduler;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    int jsondayLetterListStart;
    int[] todayScheduleFormat;
    int[] jsonNewScheduleFormat;
    int[] jsondayLetterDayNumber;
    int[][] jsonPeriodTimes;
    int currentPeriodNumber = -1;
    int progressForBar = 0;
    String jsonNotice;
    boolean offline = false;
    boolean passingTime = false;
    boolean noSchool = false;
    boolean useHardCoded = false;
    boolean dialogAnswered = false;
    Calendar cal = Calendar.getInstance();
    int currentDayNum = cal.get(Calendar.DAY_OF_MONTH);
    int currentDayDay = cal.get(Calendar.DAY_OF_WEEK);
    int currentMonth = cal.get(Calendar.MONTH);
    int currentHour = cal.get(Calendar.HOUR_OF_DAY);
    //int currentHour = 14;
    int currentMinute = cal.get(Calendar.MINUTE);
    //int currentMinute = 25;
    int currentSecond = cal.get(Calendar.SECOND);
    ProgressBar progressBar;
    ///
    int[][] normalPeriodTimes = //CHANGE BELOW TIMES WHEN SCHEDULE CHANGES
            {
                {7, 8, 9, 10, 12, 13},//START HOUR
                    {30, 25, 50, 45, 30, 25},//START MINUTE

                    {8, 9, 10, 12, 13, 14},//END HOUR
                    {25, 45, 40, 25, 20, 15}};//END MINUTE
    ///END VARS///

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        GetJson();

        if(!offline) {
           FinalizingSetupProcedures();
        }
    }

    protected void onStart(Bundle savedInstanceState) {
        Log.i("hi", "this got to onStart");

    }

    void GetJson() {
        try {
            jsonData = new JSONfetcher().execute().get();//Will wait for task to finish
        } catch (InterruptedException e) {
            //I'm not catching anything, I just wanted the error messages to go away
        } catch (ExecutionException e) {
            //I don't care if I'm defeating the purpose of an Async task, I don't care.
        }

        if ((jsonData.equals("NO CONNECTION")) || (jsonData.equals(""))) { //NO CONNECTION CONDITION
            Log.e("JSONDATA Error", "JSONDATA can't be reached, reverting to hardcoded backup");
            useHardCoded = true;
            offline = true;
            OfflineDayAlertPopup();
            //currentPeriodNumber = PeriodNumber(normalPeriodTimes);
        } else {//WITH CONNECTION WITH SUB CONDITIONS

            GetInfoFromJSON(jsonData);

            if ((jsonMonth == currentMonth) && (jsonDay == currentDayNum)) {//SPECIAL SCHEDULE WITH CONNECTION CONDITION
                todayScheduleFormat = jsonNewScheduleFormat;
                currentPeriodNumber = PeriodNumber(jsonPeriodTimes);
                useHardCoded = false;
            } else {//NORMAL SCHEDULE WITH CONNECTION CONDITION
                String tempdayletter = FindDayLetter();
                Log.i("This is today's day letter", tempdayletter);
                todayScheduleFormat = ScheduleFormat(tempdayletter);
                Log.i("This is today's schedule", Arrays.toString(todayScheduleFormat));
                currentPeriodNumber = PeriodNumber(normalPeriodTimes);
                useHardCoded = true;
            }
        }
    }

    void GetInfoFromJSON(String inputdata) {
        try {
            JSONObject JO = new JSONObject(inputdata);
            jsonDayLetter = JO.getString("dayletter");
            jsonMonth = JO.getInt("month");
            jsonDay = JO.getInt("day");
            jsondayLetterListStart = JO.getInt("dayletterliststart");

            jsonNewScheduleFormat = getArrayFromJSON("newscheduleformat");
            jsondayLetterDayNumber = getArrayFromJSON("dayletterdaynumber");

            jsonNotice = JO.getString("notice");

            int[] tempJsonStartTimesHour = getArrayFromJSON("starttimeshour");
            int[] tempJsonStartTimesMinute = getArrayFromJSON("starttimesminute");
            int[] tempJsonEndTimesHour = getArrayFromJSON("endtimeshour");
            int[] tempJsonEndTimesMinute = getArrayFromJSON("endtimesminute");

            jsonPeriodTimes = new int[4][tempJsonStartTimesHour.length];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < tempJsonStartTimesHour.length; j++) {
                    if (i == 0) {
                        jsonPeriodTimes[i][j] = tempJsonStartTimesHour[j];
                    } else if (i == 1) {
                        jsonPeriodTimes[i][j] = tempJsonStartTimesMinute[j];
                    } else if (i == 2) {
                        jsonPeriodTimes[i][j] = tempJsonEndTimesHour[j];
                    } else {
                        jsonPeriodTimes[i][j] = tempJsonEndTimesMinute[j];
                    }
                }

            }
            Log.i("wwwww", Arrays.deepToString(jsonPeriodTimes));
        } catch (JSONException e) {
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
        } catch (NullPointerException e) {
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
            return /*SERVER DATA IN HERE*/ null;
        }
    }

    String FindDayLetter() {
        //int tempday;
        int temppos = -1;
        for (int i = 0; i < jsondayLetterDayNumber.length; i++) {
            if (jsondayLetterDayNumber[i] == currentDayNum) {
                Log.i("currentdaynum", Integer.toString(currentDayNum));
                temppos = i;
                Log.i("temppos", Integer.toString(temppos));
                Log.i("jsondayletterliststart", Integer.toString(jsondayLetterListStart));
                break;
            }
        }
        if (temppos == -1) {
            Log.e("TEMPPOS", "Today's date is not found on the json file");
            return "";
        }

        if (((temppos % 4) + jsondayLetterListStart) == 0) {
            return "a";
        } else if (((temppos % 4) + jsondayLetterListStart) == 1) {
            return "b";
        } else if (((temppos % 4) + jsondayLetterListStart) == 2) {
            return "c";
        } else {
            return "d";
        }
    }

    int PeriodNumber(int[][] inputPeriodTimes) { //NOTE: ADD SAFEGUARDS TO PREVENT ARRAY READ AT -1!!!!!
        int i = 0; //array position
        passingTime = false;//If set to true in function, school is in passing time, this line resets.
        noSchool = false;//If set to true in function, is before or after school, this line resets.
        //0 start times hour, 1 start times min, 2 end times hour, 3 end times minute

        if((currentHour < inputPeriodTimes[0][0])||
                (currentHour > inputPeriodTimes[2][inputPeriodTimes.length-1])||
                (currentHour == inputPeriodTimes[0][0] && currentMinute < inputPeriodTimes[1][0])||
                (currentHour == inputPeriodTimes[2][inputPeriodTimes.length-1] && currentMinute > inputPeriodTimes[3][inputPeriodTimes.length-1]))
        {
            Log.i("There is no school", "We don't need no education");
            noSchool = true;
            return -1;
        }
        while (true) {
            if ((currentHour > inputPeriodTimes[2][i])) {
                i++;
            } else if ((currentHour == inputPeriodTimes[2][i]) && (currentMinute > inputPeriodTimes[3][i])) {
                i++;
            } else if ((currentHour == inputPeriodTimes[0][i]) && (currentMinute > inputPeriodTimes[1][i])) {
                break;
            } else if (currentHour > inputPeriodTimes[0][i]) {
                break;
            } else {
                passingTime = true;
                return -1;
            }
        }
        return (i + 1);//returns period number, must subtract one to get proper array position
    }


    void displayPeriodString() {

        //Log.i("current period num", Integer.toString(currentPeriodNumber));

        //The below code was adapted from a StackOverflow answer by Jave
        //The full answer can be found here: https://stackoverflow.com/a/8518613
        String tempScheduleString = "";
        for (int i = 0; i < todayScheduleFormat.length; i++) {
            tempScheduleString += todayScheduleFormat[i];
            if (i < todayScheduleFormat.length - 1) tempScheduleString += " ";
        }

        int tempStartPos;
        int tempEndPos;
        if(currentPeriodNumber == 1){
            tempStartPos = 0;
            tempEndPos = 1;
        }else {
            tempStartPos = ((currentPeriodNumber-1)*2);
            tempEndPos = ((currentPeriodNumber-1)*2)+1;
        }
        SpannableStringBuilder sb = new SpannableStringBuilder(tempScheduleString);
        int tempColor = ContextCompat.getColor(this, R.color.colorScheduleHighlighted);
        ForegroundColorSpan fcs = new ForegroundColorSpan(tempColor);
        sb.setSpan(fcs, tempStartPos, tempEndPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView scheduleTextView = findViewById(R.id.ScheduleLayout);
        scheduleTextView.setText(sb);
        //END CODE ATTRIBUTION by Jave
    }

    void DisplayNoticeText(){
        TextView noticetext = findViewById(R.id.noticeOfTheDay);
        noticetext.setText(jsonNotice);
    }

    void FinalizingSetupProcedures(){
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setProgress(5, true);
        displayPeriodString();
        DisplayNoticeText();
    }

    void OfflineDayAlertPopup() {
        // The Below Code was adapted from a StackOverflow Answer by WhereDatApp
        //The full answer can be found here: https://stackoverflow.com/a/19658646
        AlertDialog.Builder alertbuilder = new AlertDialog.Builder(MainPage.this);
        alertbuilder.setTitle("Servers Can't Be Reached. Please Select Today's Day Letter.");
        alertbuilder.setItems(new CharSequence[]
                        {"A Day", "B Day", "C Day", "D Day", "Goddamit I don't know what day it is"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                Toast.makeText(MainPage.this , "A Day", Toast.LENGTH_SHORT).show();
                                Log.i("this would have run", "yes");
                                FinalizingSetupProcedures();
                                //dialogAnswered = true;
                                break;
                            case 1:
                                Toast.makeText(MainPage.this, "B Day", Toast.LENGTH_SHORT).show();
                                FinalizingSetupProcedures();
                                break;
                            case 2:
                                Toast.makeText(MainPage.this, "C Day", Toast.LENGTH_SHORT).show();
                                FinalizingSetupProcedures();
                                break;
                            case 3:
                                Toast.makeText(MainPage.this, "D Day", Toast.LENGTH_SHORT).show();
                                FinalizingSetupProcedures();
                                break;
                            case 4:
                                Toast.makeText(MainPage.this, "Goddamit I don't know what day it is", Toast.LENGTH_SHORT).show();
                                String tempdumString = "lol ur dum";
                                TextView scheduleTextView = findViewById(R.id.ScheduleLayout);
                                scheduleTextView.setText(tempdumString);
                                //FinalizingSetupProcedures();
                                break;

                        }
                    }
                });
        alertbuilder.create();
        alertbuilder.show();

    }
    //END CODE ATTRIBUTION FROM WhatDatApp
}
