package owenspangler.stapleshighschoolscheduler;

//import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
//import android.graphics.Color;
//import android.graphics.PorterDuff;
//import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
//import android.text.SpannableString;
import android.text.SpannableStringBuilder;
//import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

//import android.util.Log;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
//import android.view.ViewDebug;
//import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
//import java.util.Arrays;
import java.util.concurrent.ExecutionException;
//import android.graphics.drawable.Drawable;

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
    int progressForOverallBar = 0;
    String jsonNotice;
    String progressBarTextPercent = "";
    String progressBarTextTime = "";
    String progressBarTextDescription = "Remaining";
    boolean offline = false;
    boolean passingTime = false;
    boolean noSchool = false;
    boolean useHardCoded = false;
    boolean dialogAnswered = false;
    Calendar cal = Calendar.getInstance();
    int currentDayNum = cal.get(Calendar.DAY_OF_MONTH);
    //int currentDayNum = 19;
    int currentDayDay = cal.get(Calendar.DAY_OF_WEEK);
    int currentMonth = (cal.get(Calendar.MONTH) + 1);
    int currentHour = cal.get(Calendar.HOUR_OF_DAY);
    //int currentHour = 10;
    int currentMinute = cal.get(Calendar.MINUTE);
    //int currentMinute = 20;
    //int currentSecond = cal.get(Calendar.SECOND);
    ProgressBar progressBar;
    ProgressBar overallProgressBar;
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
        Beginning();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    void Beginning() {
        GetJson();
        ////
        if ((!offline) && (!noSchool) && (!passingTime)) {
            //Log.i("finalizing setup" , "ok");
            FinalizingSetupProcedures();
        } else if ((!offline) && (passingTime)) {
            FinalizingSetupProcedures();
        } else if ((!offline) && (noSchool)) {
            NoSchoolProcedures();
        }
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
            //Log.e("JSONDATA Error", "JSONDATA can't be reached, reverting to hardcoded backup");
            useHardCoded = true;
            offline = true;
            OfflineDayAlertPopup("No Connection. Pick a Day.");
            //currentPeriodNumber = PeriodNumber(normalPeriodTimes);
        } else {//WITH CONNECTION WITH SUB CONDITIONS

            GetInfoFromJSON(jsonData);

            if ((jsonMonth == currentMonth) && (jsonDay == currentDayNum)) {//SPECIAL SCHEDULE WITH CONNECTION CONDITION
                todayScheduleFormat = jsonNewScheduleFormat;
                currentPeriodNumber = PeriodNumber(jsonPeriodTimes);
                if(!noSchool) {
                    FindTimeUntilEndOfDay(jsonPeriodTimes);
                    if(!passingTime) {
                        findTimeUntilEndNormal(jsonPeriodTimes);
                    }else{
                        FindTimeUntilEndPassingTime(jsonPeriodTimes);
                    }
                }
            } else {//NORMAL SCHEDULE WITH CONNECTION CONDITION
                String tempdayletter = FindDayLetter();
                if(!offline) {
                    todayScheduleFormat = ScheduleFormat(tempdayletter);
                    currentPeriodNumber = PeriodNumber(normalPeriodTimes);
                    if(!noSchool) {
                        FindTimeUntilEndOfDay(normalPeriodTimes);
                        if(!passingTime) {
                            findTimeUntilEndNormal(normalPeriodTimes);
                        }else{
                            FindTimeUntilEndPassingTime(normalPeriodTimes);
                        }
                    }else{
                        //add no School Condition
                    }
                }
                //useHardCoded = true;
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
            ////Log.i("wwwww", Arrays.deepToString(jsonPeriodTimes));
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
                //Log.i("currentdaynum", Integer.toString(currentDayNum));
                temppos = i;
                //Log.i("temppos", Integer.toString(temppos));
                //Log.i("jsondayletterliststart", Integer.toString(jsondayLetterListStart));
                break;
            }
        }

        if (temppos == -1) {
            //String tempresult = "";
            if ((jsonMonth == currentMonth) && (jsonDay == currentDayNum)) {
                //Log.i("Day Letter", "Today's date has a special schedule according to the json file");
                offline = false;
            }else{
                //Log.e("Day Letter", "Today's date is not found on the json file");
                //
                OfflineDayAlertPopup("Server Corruption Detected. Please Pick a Day.");
                offline = true;
            }
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

        //Log.i("Input Period Times Length 1", Integer.toString(inputPeriodTimes[0].length));
        //Log.i("Input Period Times Length 2", Integer.toString(inputPeriodTimes.length));

        if((currentHour < inputPeriodTimes[0][0])||
                (currentHour > inputPeriodTimes[2][((inputPeriodTimes[0].length)-1)])||
                (currentHour == inputPeriodTimes[0][0] && currentMinute < inputPeriodTimes[1][0])||
                (currentHour == inputPeriodTimes[2][((inputPeriodTimes[0].length)-1)] &&
                        currentMinute > inputPeriodTimes[3][((inputPeriodTimes[0].length)-1)]))
        {
            //Log.i("No School", "The time of day is out of bounds offered by the schedule. We don't need no education");
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
                return (i+1);
                //return -1;
            }
        }
        return (i + 1);//returns period number, must subtract one to get proper array position
    }


    void displayPeriodString() {

        ////Log.i("current period num", Integer.toString(currentPeriodNumber));

        //The below code was inspired from a StackOverflow answer by Jave
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
        if(passingTime){
            int tempPeriodPlacement = (currentPeriodNumber - 1);

            if(tempPeriodPlacement == 0){
                tempStartPos = 0;
                tempEndPos = 1;
            }else {
                tempStartPos = ((tempPeriodPlacement-1)*2);
                tempEndPos = ((tempPeriodPlacement-1)*2)+1;
            }
            //Log.i("we reached it", "good job");
            int tempLastColor = ContextCompat.getColor(this, R.color.colorLastPeriodScheduleHighlighted);
            ForegroundColorSpan fcslast = new ForegroundColorSpan(tempLastColor);
            sb.setSpan(fcslast, (tempStartPos), (tempEndPos), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        TextView scheduleTextView = findViewById(R.id.ScheduleLayout);
        scheduleTextView.setText(sb);
        //END CODE ATTRIBUTION by Jave
    }

    void DisplayNoticeText(){
        TextView noticetext = findViewById(R.id.noticeOfTheDay);
        noticetext.setText(jsonNotice);
    }

    void findTimeUntilEndNormal(int finderinputPeriodTimes[][]) {
        //Log.i("currentPeriodNumber", Integer.toString(currentPeriodNumber));
        int timeUntilEndHour = 0;
        int timeUntilEndMinute = 0;
        int totalTimeHour = 0;
        int totalTimeMinute = 0;
        int PeriodArrayPosition = (currentPeriodNumber - 1);
        int tempCurrentHour = currentHour;
        //int tempCurrentMinute = currentMinute;
        while (true) {
            if (tempCurrentHour < finderinputPeriodTimes[2][PeriodArrayPosition]) {
                timeUntilEndHour++;
                tempCurrentHour++;
            } else {
                timeUntilEndMinute = ((finderinputPeriodTimes[3][PeriodArrayPosition]) - currentMinute);
                break;
            }
        }
        tempCurrentHour = finderinputPeriodTimes[0][PeriodArrayPosition];
        while (true) {
            if (tempCurrentHour < finderinputPeriodTimes[2][PeriodArrayPosition]) {
                totalTimeHour++;
                tempCurrentHour++;
            } else {
                totalTimeMinute = ((finderinputPeriodTimes[1][PeriodArrayPosition]) - (finderinputPeriodTimes[3][PeriodArrayPosition]));
                break;
            }
        }

        float tempTotalMinutes = Math.abs((totalTimeHour*60)+totalTimeMinute);
        float tempLeftMinutes = Math.abs((timeUntilEndHour*60)+timeUntilEndMinute);
        progressForBar = Math.round((tempLeftMinutes/tempTotalMinutes)*100);
        progressBarTextPercent = (Integer.toString(progressForBar) + "%");
        int tempoftempLeftMinutes = (timeUntilEndHour*60)+timeUntilEndMinute;
        int tempDisplayHour = 0;
        while(true){
            if(tempoftempLeftMinutes-60 >= 0) {
                tempoftempLeftMinutes = tempoftempLeftMinutes - 60;
                tempDisplayHour++;
            }else{
                if(tempoftempLeftMinutes>=10) {
                    progressBarTextTime = (Integer.toString(tempDisplayHour) + ":" + Integer.toString(tempoftempLeftMinutes));
                }else{
                    progressBarTextTime = (Integer.toString(tempDisplayHour) + ":0" + Integer.toString(tempoftempLeftMinutes));
                }
                break;
            }
        }
        progressBarTextDescription = "Remaining";
    }

    void FindTimeUntilEndPassingTime(int[][] finderinputPeriodTimes) {
        int timeUntilEndHour = 0;
        int timeUntilEndMinute = 0;
        int totalTimeHour = 0;
        int totalTimeMinute = 0;
        int PeriodArrayPosition = (currentPeriodNumber - 1);
        int tempCurrentHour = currentHour;
        //int tempCurrentMinute = currentMinute;
        while (true) {
            if (tempCurrentHour < finderinputPeriodTimes[0][PeriodArrayPosition]) {
                timeUntilEndHour++;
                tempCurrentHour++;
            } else {
                timeUntilEndMinute = ((finderinputPeriodTimes[1][PeriodArrayPosition]) - currentMinute);
                //timeUntilEndMinute = (currentMinute - (finderinputPeriodTimes[1][PeriodArrayPosition]));
                break;
            }
        }

            tempCurrentHour = finderinputPeriodTimes[2][PeriodArrayPosition];
            while (true) {
                if (tempCurrentHour < finderinputPeriodTimes[0][PeriodArrayPosition]) {
                    totalTimeHour++;
                    tempCurrentHour++;
                } else {
                    totalTimeMinute = ((finderinputPeriodTimes[3][PeriodArrayPosition - 1]) - (finderinputPeriodTimes[1][PeriodArrayPosition]));
                    break;
                }
            }

        float tempTotalMinutes = Math.abs((totalTimeHour * 60) + totalTimeMinute);
        float tempLeftMinutes = Math.abs((timeUntilEndHour * 60) + timeUntilEndMinute);

        progressForBar = Math.round(((tempLeftMinutes / tempTotalMinutes) * 100));
        progressBarTextPercent = (Integer.toString(progressForBar) + "%");
        int tempoftempLeftMinutes = (timeUntilEndHour * 60) + timeUntilEndMinute;
        int tempDisplayHour = 0;
        while (true) {
            if (tempoftempLeftMinutes - 60 >= 0) {
                tempoftempLeftMinutes = tempoftempLeftMinutes - 60;
                tempDisplayHour++;
            } else {
                if (tempoftempLeftMinutes >= 10) {
                    progressBarTextTime = (Integer.toString(tempDisplayHour) + ":" + Integer.toString(tempoftempLeftMinutes));
                } else {
                    progressBarTextTime = (Integer.toString(tempDisplayHour) + ":0" + Integer.toString(tempoftempLeftMinutes));
                }
                break;
            }
        }
    }

    void FindTimeUntilEndOfDay(int[][] finderinputPeriodTimes){
        //int PeriodArrayPosition = (currentPeriodNumber - 1);
        int tempCurrentHour = currentHour;
        int tempTimeUntilHour = 0;
        int tempTimeUntilMinute = 0;
        int tempTotalTimeHour = 0;
        int tempTotalTimeMinute = 0;
        //int tempCurrentMinute = currentMinute;
        while (true) {
            if (tempCurrentHour < finderinputPeriodTimes[2][(finderinputPeriodTimes[0].length) - 1]) {
                tempTimeUntilHour++;
                tempCurrentHour++;
            } else {
                tempTimeUntilMinute = ((finderinputPeriodTimes[3][(finderinputPeriodTimes[0].length) - 1])-currentMinute);
                break;
            }
        }
        tempCurrentHour = finderinputPeriodTimes[0][0];
        while (true) {
            if (tempCurrentHour < finderinputPeriodTimes[2][(finderinputPeriodTimes[0].length)-1]) {
                tempTotalTimeHour++;
                tempCurrentHour++;
            }else {
                tempTotalTimeMinute = ((finderinputPeriodTimes[3][(finderinputPeriodTimes[0].length) - 1]) - (finderinputPeriodTimes[1][0]));
                break;
            }
        }

        float tempTotalMinutes = Math.abs((tempTotalTimeHour*60)+tempTotalTimeMinute);
        //Log.i("temptotalminutestotal", Float.toString(tempTotalMinutes));
        float tempLeftMinutes = Math.abs((tempTimeUntilHour*60)+tempTimeUntilMinute);
        //Log.i("templeftminutestotal", Float.toString(tempLeftMinutes));
        progressForOverallBar = 100 - Math.round((tempLeftMinutes/tempTotalMinutes)*100);

    }


    void FinalizingSetupProcedures(){
        //progressForBar = 40;
        progressBar = findViewById(R.id.progressBar);
        overallProgressBar = findViewById(R.id.OverallDayProgressBar);
        ////Log.i("progressforbar", Integer.toString(progressForBar));
        progressBar.setProgress(progressForBar);
        overallProgressBar.setProgress(progressForOverallBar);
        //Log.i("progressforoverall",Integer.toString(progressForOverallBar));
        TextView ProgressBarTextPercent = findViewById(R.id.ProgressBarTextPercent);
        ProgressBarTextPercent.setText(progressBarTextPercent);
        TextView ProgressBarTextTime = findViewById(R.id.ProgressBarTextTime);
        ProgressBarTextTime.setText(progressBarTextTime);
        TextView ProgressBarTextDescription = findViewById(R.id.ProgressBarTextDescription);
        ProgressBarTextDescription.setText(progressBarTextDescription);
        if(!noSchool) {
            displayPeriodString();
            DisplayNoticeText();
        }

        if(passingTime){
            ProgressBarTextDescription.setText("Passing Time");
            //Drawable circular = ContextCompat.getDrawable(this, R.drawable.circular);
            //circular.setColorFilter(ContextCompat.getColor(this, R.color.colorLastPeriodScheduleHighlighted), PorterDuff.Mode.DST_IN);
            //circular.setColorFilter(0xffff0000, PorterDuff.Mode.DST_IN);
        }
        final Button refreshButton = findViewById(R.id.RefreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reset();
            }
        });
    }

    void OfflineProcedures(){
        TextView noticetext = findViewById(R.id.noticeOfTheDay);
        noticetext.setText("No Connection");
        currentPeriodNumber = PeriodNumber(normalPeriodTimes);
        //Log.i("period time", Integer.toString(currentPeriodNumber));
        if(!noSchool) {
            FindTimeUntilEndOfDay(normalPeriodTimes);
            findTimeUntilEndNormal(normalPeriodTimes);
            FinalizingSetupProcedures();
        }else{
            NoSchoolProcedures();
        }
    }

    void NoSchoolProcedures(){
        String tempScheduleString = "";
        for (int i = 0; i < todayScheduleFormat.length; i++) {
            tempScheduleString += todayScheduleFormat[i];
            if (i < todayScheduleFormat.length - 1) tempScheduleString += " ";
        }
        TextView scheduleTextView = findViewById(R.id.ScheduleLayout);
        scheduleTextView.setText(tempScheduleString);
        progressBar = findViewById(R.id.progressBar);
        overallProgressBar = findViewById(R.id.OverallDayProgressBar);
        ////Log.i("progressforbar", Integer.toString(progressForBar));
        progressBar.setProgress(100);
        overallProgressBar.setProgress(100);
        //Log.i("progressforoverall",Integer.toString(progressForOverallBar));
        TextView ProgressBarTextPercent = findViewById(R.id.ProgressBarTextPercent);
        ProgressBarTextPercent.setText("NO");
        TextView ProgressBarTextTime = findViewById(R.id.ProgressBarTextTime);
        ProgressBarTextTime.setTextSize(30);
        ProgressBarTextTime.setText("SCHOOL");
        TextView ProgressBarTextDescription = findViewById(R.id.ProgressBarTextDescription);
        ProgressBarTextDescription.setText("Check App Later");
        if(!offline) {
            DisplayNoticeText();
        }
        ///
        AlertDialog.Builder noSchoolDialog = new AlertDialog.Builder(this);
        noSchoolDialog.setMessage("There is currently no school. The current information displayed may be incorrect.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = noSchoolDialog.create();
        alert.show();
    }


    void OfflineDayAlertPopup(String title) {
        // The Below Code was adapted from a StackOverflow Answer by WhereDatApp
        //The full answer can be found here: https://stackoverflow.com/a/19658646
        AlertDialog.Builder alertbuilder = new AlertDialog.Builder(MainPage.this);
        alertbuilder.setTitle(title);
        alertbuilder.setItems(new CharSequence[]
                        {"A Day", "B Day", "C Day", "D Day", "Help"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                Toast.makeText(MainPage.this , "Set as A Day", Toast.LENGTH_SHORT).show();
                                //Log.i("This is today's day letter", "A");
                                todayScheduleFormat = ScheduleFormat("a");
                                OfflineProcedures();
                                break;
                            case 1:
                                Toast.makeText(MainPage.this, "Set as B Day", Toast.LENGTH_SHORT).show();
                                //Log.i("This is today's day letter", "B");
                                todayScheduleFormat = ScheduleFormat("b");
                                OfflineProcedures();
                                break;
                            case 2:
                                Toast.makeText(MainPage.this, "Set as C Day", Toast.LENGTH_SHORT).show();
                                //Log.i("This is today's day letter", "C");
                                todayScheduleFormat = ScheduleFormat("c");
                                OfflineProcedures();
                                break;
                            case 3:
                                Toast.makeText(MainPage.this, "Set as D Day", Toast.LENGTH_SHORT).show();
                                //Log.i("This is today's day letter", "D");
                                todayScheduleFormat = ScheduleFormat("d");
                                FinalizingSetupProcedures();
                                break;
                            case 4:
                                Toast.makeText(MainPage.this, "Help", Toast.LENGTH_LONG).show();
                                TextView scheduleTextView = findViewById(R.id.ScheduleLayout);
                                scheduleTextView.setText("WILL ADD PICTURE OF YEAR LONG SCHEDULE HERE");
                                //FinalizingSetupProcedures();
                                break;

                        }
                    }
                });
        alertbuilder.create();
        alertbuilder.show();

    }
    //END CODE ATTRIBUTION FROM WhatDatApp

    void reset(){
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        /*
        offline = false;
        passingTime = false;
        noSchool = false;
        useHardCoded = false;
        dialogAnswered = false;
        currentDayNum = cal.get(Calendar.DAY_OF_MONTH);
        //int currentDayNum = 19;
        //currentDayDay = cal.get(Calendar.DAY_OF_WEEK);
        currentMonth = (cal.get(Calendar.MONTH) + 1);
        currentHour = cal.get(Calendar.HOUR_OF_DAY);
        //currentHour = 10;
        currentMinute = cal.get(Calendar.MINUTE);
        //int currentMinute = 35;
        //int currentSecond = cal.get(Calendar.SECOND);
        Beginning();
        */
    }
}
