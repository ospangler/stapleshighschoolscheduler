package owenspangler.stapleshighschoolscheduler;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class MainPage extends AppCompatActivity {

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
    boolean specialSchedule = false;
    Calendar cal = Calendar.getInstance();
    int currentDayNum = cal.get(Calendar.DAY_OF_MONTH);
    //int currentDayNum = 28;
    //int currentDayDay = cal.get(Calendar.DAY_OF_WEEK);
    int currentMonth = (cal.get(Calendar.MONTH) + 1);
    int currentHour = cal.get(Calendar.HOUR_OF_DAY);
    //int currentHour = 14;
    int currentMinute = cal.get(Calendar.MINUTE);
    //int currentMinute = 16;
    //int currentSecond = cal.get(Calendar.SECOND);
    ProgressBar progressBar;
    ProgressBar overallProgressBar;
    ///
    int[][] normalPeriodTimes = //CHANGE BELOW TIMES WHEN SCHEDULE CHANGES
            {
                    { 7,  8,  9, 10, 12, 13},//START HOUR
                    {30, 25, 50, 45, 30, 25},//START MINUTE

                    { 8,  9, 10, 12, 13, 14},//END HOUR
                    {20, 45, 40, 25, 20, 15}};//END MINUTE
    ///END VARS///

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Main();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    void Main() {

        GetJson();//keep this out of async so server isn't checked every second. add settings menu with manual refresh in future

        int[][] inputPeriodTimes;

        if(!offline) {

            CheckForServerCorruption(); //check if there are wrong entries in the server data that would result in a crash


            if (specialSchedule) { // If the date on the json file matches, program uses special schedule
                inputPeriodTimes = jsonPeriodTimes;
                todayScheduleFormat = jsonNewScheduleFormat;
                currentPeriodNumber = PeriodNumber(inputPeriodTimes);

            } else { // If date from json file does not match or is not available, the normal schedule is used
                inputPeriodTimes = normalPeriodTimes;
                String inputDayLetter = FindDayLetter();
                todayScheduleFormat = ScheduleFormat(inputDayLetter);
                currentPeriodNumber = PeriodNumber(inputPeriodTimes);
            }

            if (noSchool){ // Online, No School using Current Schedule
                NoSchoolProcedures();

            }else if (passingTime){ // Online, School in session, but not inside a period detected
                FindTimeUntilEndOfDay(inputPeriodTimes);
                FindTimeUntilEndPassingTime(inputPeriodTimes);
                FinalizingSetupProcedures();

            }else{ // Online, School in session and during period
                FindTimeUntilEndOfDay(inputPeriodTimes);
                FindTimeUntilEndNormal(inputPeriodTimes);
                FinalizingSetupProcedures();
            }

        }else{
            OfflineDayAlertPopup("No Connection. Pick a Day.");
        }

    }

    void OfflineConditions(){

        int[][] inputPeriodTimes;

        inputPeriodTimes = normalPeriodTimes;
        currentPeriodNumber = PeriodNumber(inputPeriodTimes);


        if(noSchool){ // Offline, No School Detected for Normal Schedule
            OfflineProcedures();
            NoSchoolProcedures();

        }else if(passingTime){ // Offline, Passing Time Detected for Normal Schedule
            OfflineProcedures();
            FindTimeUntilEndOfDay(inputPeriodTimes);
            FindTimeUntilEndPassingTime(inputPeriodTimes);
            FinalizingSetupProcedures();

        }else { // Offline, Normal School Conditions Detected
            OfflineProcedures();
            FindTimeUntilEndOfDay(inputPeriodTimes);
            FindTimeUntilEndNormal(inputPeriodTimes);
            FinalizingSetupProcedures();
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
            offline = true;

        } else {//WITH CONNECTION WITH SUB CONDITIONS

            GetInfoFromJSON(jsonData);

            if ((jsonMonth == currentMonth) && (jsonDay == currentDayNum)) {//SPECIAL SCHEDULE WITH CONNECTION CONDITION
                specialSchedule = true;

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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    int[] getArrayFromJSON(String jsonID) {

        try {
            JSONObject tempJO = new JSONObject(jsonData);
            JSONArray tempJsonArray = tempJO.optJSONArray(jsonID);
            if (tempJsonArray == null) { /*ENTER SOME ERROR CODE HERE LATER*/ }
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

    int[] ScheduleFormat(String inputDayType) { //EDIT THIS FUNCTION IF BASELINE SCHEDULE CHANGES AND PUSH UPDATE

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
        } else { //Error Condition
            return null;
        }
    }

    void CheckForServerCorruption(){ //Checks for server corruption, or most likely, incorrect entry from crashing the app.

        int temppos = -1;
        for (int i = 0; i < jsondayLetterDayNumber.length; i++) {
            if (jsondayLetterDayNumber[i] == currentDayNum) {
                temppos = i;
                break;
            }
        }

        if (temppos == -1) { //Triggers if today's date is not found in the json file list
            if ((jsonMonth == currentMonth) && (jsonDay == currentDayNum)) { //checks to see if the server contains the missing date
                //offline = false;
            }else{// if date is not found in main list or on the special conditions list, an error to manually select is created below
                offline = true;
            }
        }
    }
    String FindDayLetter() { //Finds the current day letter given the list pulled from the json server

        int temppos = -1;
        for (int i = 0; i < jsondayLetterDayNumber.length; i++) {
            if (jsondayLetterDayNumber[i] == currentDayNum) {
                temppos = i;
                break;
            }
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

    int PeriodNumber(int[][] inputPeriodTimes) { //Finds the current period number of the day, and determines if it is passing time or if there is no School

        int i = 0; //array position
        passingTime = false;//If set to true in function, school is in passing time, this line resets.
        noSchool = false;//If set to true in function, is before or after school, this line resets.

        //KEY: 0 start times hour, 1 start times min, 2 end times hour, 3 end times minute

        if((currentHour < inputPeriodTimes[0][0])||
                (currentHour > inputPeriodTimes[2][((inputPeriodTimes[0].length)-1)])||
                (currentHour == inputPeriodTimes[0][0] && currentMinute < inputPeriodTimes[1][0])||
                (currentHour == inputPeriodTimes[2][((inputPeriodTimes[0].length)-1)] &&
                        currentMinute >= inputPeriodTimes[3][((inputPeriodTimes[0].length)-1)]))
        { //Checks if the current time is before or after all values entered into the time array for the day

            noSchool = true;
            return -1;
        }

        while (true) { //runs through all times and counts the period number of the day
            if ((currentHour > inputPeriodTimes[2][i])) {
                i++;
            } else if ((currentHour == inputPeriodTimes[2][i]) && (currentMinute >= inputPeriodTimes[3][i])) {
                i++;
            } else if ((currentHour == inputPeriodTimes[0][i]) && (currentMinute >= inputPeriodTimes[1][i])) {
                break;
            } else if (currentHour > inputPeriodTimes[0][i]) {
                break;
            } else { //If not found, but not No School, it must be passing time
                passingTime = true;
                return (i+1);
                //return -1;
            }
        }
        return (i + 1);//returns period number, must subtract one to get proper array position
    }


    void displayPeriodString() { //Displays and Highlights the Numbers of the Period String

        String tempScheduleString = ""; //Allows for display of numbers by adding a 1 before their numerical equivalent

            for (int i = 0; i < todayScheduleFormat.length; i++) {
                if (todayScheduleFormat[i] >= 100) {
                    String tempalphabet = "ABCDEFGHIJKLMNOPQRRSTUVWXYZ";
                    tempScheduleString += tempalphabet.charAt(todayScheduleFormat[i] - 100);
                } else {
                    tempScheduleString += todayScheduleFormat[i];
                }
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
        //The below code was inspired from a StackOverflow answer by Jave
        //The full answer can be found here: https://stackoverflow.com/a/8518613

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

            int tempLastColor = ContextCompat.getColor(this, R.color.colorLastPeriodScheduleHighlighted);
            ForegroundColorSpan fcslast = new ForegroundColorSpan(tempLastColor);
            sb.setSpan(fcslast, (tempStartPos), (tempEndPos), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        TextView scheduleTextView = findViewById(R.id.ScheduleLayout);
        scheduleTextView.setText(sb);

        //END CODE ATTRIBUTION by Jave
    }

    void DisplayNoticeText(){ //Displays some sick motivational quotes when called
        TextView noticetext = findViewById(R.id.noticeOfTheDay);
        noticetext.setText(jsonNotice);
    }

    void FindTimeUntilEndNormal(int finderinputPeriodTimes[][]) { //Finds the time until the end of the period

        int timeUntilEndHour = 0;
        int timeUntilEndMinute = 0;
        int totalTimeHour = 0;
        int totalTimeMinute = 0;
        int PeriodArrayPosition = (currentPeriodNumber - 1);
        int tempCurrentHour = currentHour;

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
                totalTimeMinute = ((finderinputPeriodTimes[3][PeriodArrayPosition]) - (finderinputPeriodTimes[1][PeriodArrayPosition]));
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

    void FindTimeUntilEndPassingTime(int[][] finderinputPeriodTimes) { //Finds time until end of passing time

        int timeUntilEndHour = 0;
        int timeUntilEndMinute = 0;
        int totalTimeHour = 0;
        int totalTimeMinute = 0;
        int PeriodArrayPosition = (currentPeriodNumber - 1);
        int tempCurrentHour = currentHour;

        while (true) {
            if (tempCurrentHour < finderinputPeriodTimes[0][PeriodArrayPosition]) {
                timeUntilEndHour++;
                tempCurrentHour++;
            } else {
                timeUntilEndMinute = ((finderinputPeriodTimes[1][PeriodArrayPosition]) - currentMinute);
                break;
            }
        }

            tempCurrentHour = finderinputPeriodTimes[2][PeriodArrayPosition];
            while (true) {
                if (tempCurrentHour < finderinputPeriodTimes[0][PeriodArrayPosition]) {
                    totalTimeHour++;
                    tempCurrentHour++;
                } else {
                    totalTimeMinute = ((finderinputPeriodTimes[3][PeriodArrayPosition-1]) - (finderinputPeriodTimes[1][PeriodArrayPosition]));
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

    void FindTimeUntilEndOfDay(int[][] finderinputPeriodTimes){ //Finds time until end of day

        int tempCurrentHour = currentHour;
        int tempTimeUntilHour = 0;
        int tempTimeUntilMinute = 0;
        int tempTotalTimeHour = 0;
        int tempTotalTimeMinute = 0;

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
        float tempLeftMinutes = Math.abs((tempTimeUntilHour*60)+tempTimeUntilMinute);
        progressForOverallBar = 100 - Math.round((tempLeftMinutes/tempTotalMinutes)*100);

    }


    void FinalizingSetupProcedures(){ //Final setting of values on the UI

        progressBar = findViewById(R.id.progressBar);
        overallProgressBar = findViewById(R.id.OverallDayProgressBar);
        progressBar.setProgress(progressForBar);
        overallProgressBar.setProgress(progressForOverallBar);
        TextView ProgressBarTextPercent = findViewById(R.id.ProgressBarTextPercent);
        ProgressBarTextPercent.setText(progressBarTextPercent);
        TextView ProgressBarTextTime = findViewById(R.id.ProgressBarTextTime);
        ProgressBarTextTime.setText(progressBarTextTime);
        TextView ProgressBarTextDescription = findViewById(R.id.ProgressBarTextDescription);
        ProgressBarTextDescription.setText(progressBarTextDescription);

        displayPeriodString();

        if(!offline) DisplayNoticeText();

        if(passingTime){
            ProgressBarTextDescription.setText("Passing Time");
            //Drawable circular = ContextCompat.getDrawable(this, R.drawable.circular);
            //circular.setColorFilter(ContextCompat.getColor(this, R.color.colorLastPeriodScheduleHighlighted), PorterDuff.Mode.DST_IN);
            //circular.setColorFilter(0xffff0000, PorterDuff.Mode.DST_IN);
        }
        //GET RID OF BUTTON IN NEXT UPDATE ONCE SENT TO ASYNC TASK
        final Button refreshButton = findViewById(R.id.RefreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reset();
            }
        });
    }

    void OfflineProcedures(){ //Changes values on UI thread to reflect offline state

        TextView noticetext = findViewById(R.id.noticeOfTheDay);
        noticetext.setText("No Connection. Information may be inaccurate");
    }

    void NoSchoolProcedures(){ //Changes values on UI thread to reflect no school state

        String tempScheduleString = "";
        for (int i = 0; i < todayScheduleFormat.length; i++) {
            tempScheduleString += todayScheduleFormat[i];
            if (i < todayScheduleFormat.length - 1) tempScheduleString += " ";
        }
        TextView scheduleTextView = findViewById(R.id.ScheduleLayout);
        scheduleTextView.setText(tempScheduleString);
        progressBar = findViewById(R.id.progressBar);
        overallProgressBar = findViewById(R.id.OverallDayProgressBar);
        progressBar.setProgress(100);
        overallProgressBar.setProgress(100);
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
    }


    void OfflineDayAlertPopup(String title) { //Alert makes users select today's day due to offline state. Will add support for offline schedule loading in next update.

        // The Below Code was adapted from a StackOverflow Answer by WhereDatApp
        //The full answer can be found here: https://stackoverflow.com/a/19658646
        final AlertDialog.Builder alertbuilder = new AlertDialog.Builder(MainPage.this);
        alertbuilder.setTitle(title);
        alertbuilder.setItems(new CharSequence[]
                        {"A Day", "B Day", "C Day", "D Day", "Help"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                todayScheduleFormat = ScheduleFormat("a");
                                Toast.makeText(MainPage.this , "Set as 'A' Day", Toast.LENGTH_LONG).show();
                                OfflineConditions();
                                break;
                            case 1:
                                todayScheduleFormat = ScheduleFormat("b");
                                Toast.makeText(MainPage.this, "Set as 'B' Day", Toast.LENGTH_LONG).show();
                                OfflineConditions();
                                break;
                            case 2:
                                todayScheduleFormat = ScheduleFormat("c");
                                Toast.makeText(MainPage.this, "Set as 'C' Day", Toast.LENGTH_LONG).show();
                                OfflineConditions();
                                break;
                            case 3:
                                todayScheduleFormat = ScheduleFormat("d");
                                Toast.makeText(MainPage.this, "Set as 'D' Day", Toast.LENGTH_LONG).show();
                                OfflineConditions();
                                break;
                            case 4:
                                Toast.makeText(MainPage.this, "Help is currently not supported", Toast.LENGTH_LONG).show();
                                break;

                        }
                    }
                });
        alertbuilder.create();
        alertbuilder.show();

    }
    //END CODE ATTRIBUTION FROM WhatDatApp

    void reset(){ //WILL REMOVE WHEN RESET BUTTON REMOVED FOR NEXT UPDATE
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }
}
