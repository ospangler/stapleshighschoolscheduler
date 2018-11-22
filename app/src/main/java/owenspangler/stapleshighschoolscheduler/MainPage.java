package owenspangler.stapleshighschoolscheduler;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class MainPage extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    String jsonData; //Raw Json String Pulled From Async Task
    //
    int[] scheduleFormat;
    int[][] periodTimes;
    int lunchPeriodPosition;
    int[][] lunchWaveTimes;
    String dayLetter;
    boolean noLunch = false;
    //
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
    ProgressBar progressBar;
    ProgressBar overallProgressBar;
    //int[][] inputPeriodTimes;
    /*
    int[][] normalPeriodTimes = //CHANGE BELOW TIMES WHEN SCHEDULE CHANGES
            {
                    {7, 8, 9, 10, 12, 13},//START HOUR
                    {30, 25, 50, 45, 30, 25},//START MINUTE

                    {8, 9, 10, 12, 13, 14},//END HOUR
                    {20, 45, 40, 25, 20, 15}};//END MINUTE
    ///END VARS///
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        try {
            actionbar.setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            //put stack trace here
        }
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);


        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        int id = menuItem.getItemId();

                        if (id == R.id.nav_notification) {
                            // launch settings activity
                            startActivity(new Intent(MainPage.this, NotificationActivity.class));
                            return true;
                        } else if (id == R.id.nav_schedule_input) {
                            startActivity(new Intent(MainPage.this, ScheduleInputActivity.class));
                            return true;
                        } else if (id == R.id.nav_quote) {
                            startActivity(new Intent(MainPage.this, NotificationActivity.class));
                            return true;
                        } else if (id == R.id.nav_settings) {
                            startActivity(new Intent(MainPage.this, GeneralSettingsActivity.class));
                            return true;
                        }


                        return MainPage.super.onOptionsItemSelected(menuItem);
                    }
                });
        ////

        FirstMain();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    Handler h = new Handler();
    int delay = 15 * 1000; //sets refresh delay for app
    Runnable runnable;

    @Override
    protected void onResume() {
        //start handler as activity become visible

        h.postDelayed(runnable = new Runnable() {
            public void run() {
                RepeatedMain();
                h.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    @Override
    protected void onPause() {
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }

    void FirstMain() {//Initial Code that executes on startup

        GetJson();
/*
        if (!offline) {

            //CheckForServerCorruption(); //check if there are wrong entries in the server data that would result in a crash


            if (specialSchedule) { // If the date on the json file matches, program uses special schedule
                inputPeriodTimes = jsonPeriodTimes;
                todayScheduleFormat = jsonNewScheduleFormat;
                currentPeriodNumber = PeriodNumber(inputPeriodTimes);

            } else { // If date from json file does not match or is not available, the normal schedule is used
                inputPeriodTimes = normalPeriodTimes;
                //String inputDayLetter = FindDayLetter();
                //todayScheduleFormat = ScheduleFormat(inputDayLetter);
                currentPeriodNumber = PeriodNumber(inputPeriodTimes);
            }

            if (noSchool) { // Online, No School using Current Schedule
                NoSchoolProcedures();

            } else if (passingTime) { // Online, School in session, but not inside a period detected
                FindTimeUntilEndOfDay(inputPeriodTimes);
                FindTimeUntilEndPassingTime(inputPeriodTimes);
                FinalizingSetupProcedures();

            } else { // Online, School in session and during period
                FindTimeUntilEndOfDay(inputPeriodTimes);
                FindTimeUntilEndNormal(inputPeriodTimes);
                FinalizingSetupProcedures();
            }

        } else {
            OfflineDayAlertPopup("No Connection. Pick a Day.");
        }
        */

    }

    void RepeatedMain() {//This is the main code that repeats after the initial push
        /*
        passingTime = false;//If set to true in function, school is in passing time, this line resets.
        noSchool = false;//If set to true in function, is before or after school, this line resets.
        Calendar calRefresh = Calendar.getInstance();
        currentHour = calRefresh.get(Calendar.HOUR_OF_DAY);
        currentMinute = calRefresh.get(Calendar.MINUTE);

        PeriodNumber(inputPeriodTimes);//checks to see if there is noSchool

        if (!offline) {
            if (!noSchool) currentPeriodNumber = PeriodNumber(inputPeriodTimes);

            if (noSchool) { // Online, No School using Current Schedule
                NoSchoolProcedures();

            } else if (passingTime) { // Online, School in session, but not inside a period detected
                FindTimeUntilEndOfDay(inputPeriodTimes);
                FindTimeUntilEndPassingTime(inputPeriodTimes);
                FinalizingSetupProcedures();

            } else { // Online, School in session and during period
                FindTimeUntilEndOfDay(inputPeriodTimes);
                FindTimeUntilEndNormal(inputPeriodTimes);
                FinalizingSetupProcedures();
            }
        } else {
            OfflineConditions();
        }
        */
    }

    void OfflineConditions() {
/*
        int[][] offlineInputPeriodTimes;

        offlineInputPeriodTimes = normalPeriodTimes;
        currentPeriodNumber = PeriodNumber(offlineInputPeriodTimes);


        if (noSchool) { // Offline, No School Detected for Normal Schedule
            OfflineProcedures();
            NoSchoolProcedures();

        } else if (passingTime) { // Offline, Passing Time Detected for Normal Schedule
            OfflineProcedures();
            FindTimeUntilEndOfDay(offlineInputPeriodTimes);
            FindTimeUntilEndPassingTime(offlineInputPeriodTimes);
            FinalizingSetupProcedures();

        } else { // Offline, Normal School Conditions Detected
            OfflineProcedures();
            FindTimeUntilEndOfDay(offlineInputPeriodTimes);
            FindTimeUntilEndNormal(offlineInputPeriodTimes);
            FinalizingSetupProcedures();
        }
        */
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

        } else {//WITH CONNECTION

            GetInfoFromJSON(jsonData);
        }

    }

    void GetInfoFromJSON(String inputdata) {

        try {
            JSONObject JO = new JSONObject(inputdata);
            jsonNotice = JO.getString("notice");

            JSONArray scheduleChangeArray = JO.getJSONArray("schedulechange");

            for (int i = 0; i < scheduleChangeArray.length(); i++) { //checks to see if any days are listed as changed

                int tempMonth = 0;
                int tempDay = 0;
                //JO.getJSONObject("schedulechange").getJSONObject(Integer.toString(i)).getInt("month");
                tempMonth = scheduleChangeArray.getJSONObject(i).getInt("month");
                tempDay = scheduleChangeArray.getJSONObject(i).getInt("day");
                Log.i("currentmonth", Integer.toString(tempMonth));
                Log.i("currentday", Integer.toString(tempDay));
                Log.i("arraylength", Integer.toString(scheduleChangeArray.length()));

                if ((tempMonth == currentMonth) && (tempDay == currentDayNum)) { //found day listed matches today's date

                    specialSchedule = true;

                    dayLetter =  scheduleChangeArray.getJSONObject(i).getString("dayletter");

                    JSONArray tempStartTimesHourArray = scheduleChangeArray.getJSONObject(i).getJSONArray("starttimeshour");
                    JSONArray tempStartTimesMinuteArray = scheduleChangeArray.getJSONObject(i).getJSONArray("starttimesminute");
                    JSONArray tempEndTimesHourArray = scheduleChangeArray.getJSONObject(i).getJSONArray("endtimeshour");
                    JSONArray tempEndTimesMinuteArray = scheduleChangeArray.getJSONObject(i).getJSONArray("endtimesminute");
                    periodTimes = new int[4][tempStartTimesHourArray.length()];

                    for (int j = 0; j < 4; j++) {
                        for (int k = 0; k < tempStartTimesHourArray.length(); k++) {
                            if (j == 0) {
                                periodTimes[j][k] = tempStartTimesHourArray.getInt(k);
                            } else if (j == 1) {
                                periodTimes[j][k] = tempStartTimesMinuteArray.getInt(k);
                            } else if (j == 2) {
                                periodTimes[j][k] = tempEndTimesHourArray.getInt(k);
                            } else {
                                periodTimes[j][k] = tempEndTimesMinuteArray.getInt(k);
                            }
                        }

                    }
                    Log.i("dududu", Arrays.deepToString(periodTimes));

                    JSONArray tempStartLunchHourArray = scheduleChangeArray.getJSONObject(i).getJSONArray("lunchwavesstarthour");
                    JSONArray tempStartLunchMinuteArray = scheduleChangeArray.getJSONObject(i).getJSONArray("lunchwavesstartminute");
                    JSONArray tempEndLunchHourArray = scheduleChangeArray.getJSONObject(i).getJSONArray("lunchwavesendhour");
                    JSONArray tempEndLunchMinuteArray = scheduleChangeArray.getJSONObject(i).getJSONArray("lunchwavesendminute");

                    if(tempStartLunchHourArray.length() == 0){ // if nothing in array, no lunch

                        noLunch = true;

                    }else{//if values in array, has lunch

                        lunchPeriodPosition = scheduleChangeArray.getJSONObject(i).getInt("lunchperiodposition");
                        lunchWaveTimes = new int[4][tempStartLunchHourArray.length()];

                        for (int j = 0; j < 4; j++) {
                            for (int k = 0; k < tempStartLunchHourArray.length(); k++) {
                                if (j == 0) {
                                    lunchWaveTimes[j][k] = tempStartLunchHourArray.getInt(k);
                                } else if (j == 1) {
                                    lunchWaveTimes[j][k] = tempStartLunchMinuteArray.getInt(k);
                                } else if (j == 2) {
                                    lunchWaveTimes[j][k] = tempEndLunchHourArray.getInt(k);
                                } else {
                                    lunchWaveTimes[j][k] = tempEndLunchMinuteArray.getInt(k);
                                }
                            }

                        }
                        Log.i("dududu", Arrays.deepToString(periodTimes));
                    }
                    break;
                }

            }

            if (!specialSchedule) { //if no special schedule was found, normal day formats will be written
                int tempDayListStart = JO.getJSONObject("dayletters").getJSONObject(Integer.toString(currentMonth)).getInt("dayletterliststart");
                JSONArray DayLetterListArray = JO.getJSONObject("dayletters").getJSONObject(Integer.toString(currentMonth)).getJSONArray("dayletterlist");
                boolean tempFound = false;
                int tempPosition = -1;

                for (int i = 0; i < DayLetterListArray.length(); i++) {
                    if (DayLetterListArray.getInt(i) == currentDayNum) {
                        tempFound = true;
                        tempPosition = i;
                        break;
                    }
                }

                if (!tempFound) {
                    noSchool = true;
                } else {
                    if (((tempPosition % 4) + tempDayListStart) == 0) {
                        dayLetter = "a";
                        scheduleFormat = NormalScheduleFormat("a");//new int[]{1, 2, 3, 5, 8, 7}; //'A' day
                    } else if (((tempPosition % 4) + tempDayListStart) == 1) {
                        dayLetter = "b";
                        scheduleFormat = NormalScheduleFormat("b");//new int[]{2, 3, 4, 6, 7, 8}; //'B' day
                    } else if (((tempPosition % 4) + tempDayListStart) == 2) {
                        dayLetter = "c";
                        scheduleFormat = NormalScheduleFormat("c");//new int[]{3, 4, 1, 7, 6, 5}; //'C' day
                    } else {
                        dayLetter = "d";
                        scheduleFormat = NormalScheduleFormat("d");//new int[]{4, 1, 2, 8, 5, 6}; //'D' day
                    }
                }
            }

            //Log.i("dayletter", dayLetter);
            Log.i("scheduleFormat", Arrays.toString(scheduleFormat));
            Log.i("noSchool", Boolean.toString(noSchool));
            Log.i("lunchscheduleFormat", Arrays.deepToString(lunchWaveTimes));
            //Log.i("day", Arrays.toString(scheduleFormat));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

        /*
    int[] getArrayFromJSON(String jsonID) { //REDUNDANT CODE!!!!

        try {
            JSONObject tempJO = new JSONObject(jsonData);
            JSONArray tempJsonArray = tempJO.optJSONArray(jsonID);
            if (tempJsonArray == null) {}
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
    */

    int[] NormalScheduleFormat(String inputDayType) { //EDIT THIS FUNCTION IF BASELINE SCHEDULE CHANGES AND PUSH UPDATE

        lunchPeriodPosition = 3; //Period Position that is extended for lunch waves

        lunchWaveTimes = new int[][]{ //Lunch Wave Times Within Extended Lunch Period
                {3,3,3},//START HOUR
                {4,4,4},//START MINUTE
                {5,5,5},//END HOUR
                {6,6,6}//END MINUTE
        };

        periodTimes = new int[][]{ //CHANGE BELOW TIMES WHEN SCHEDULE CHANGES

                {7, 8, 9, 10, 12, 13},//START HOUR
                {30, 25, 50, 45, 30, 25},//START MINUTE

                {8, 9, 10, 12, 13, 14},//END HOUR
                {20, 45, 40, 25, 20, 15}//END MINUTE
        };

        //Period Label Format Below
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

    /*
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
    */
    /*
    String FindDayLetter() { //Finds the current day letter given the list pulled from the json server //REDUNDANT CODE!!!!
        boolean found = false;
        int temppos = -1;
        for (int i = 0; i < jsondayLetterDayNumber.length; i++) {
            if (jsondayLetterDayNumber[i] == currentDayNum) {
                found = true;
                temppos = i;
                break;
            }
        }

        if(!found) noSchool = true;

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
    */
    int PeriodNumber(int[][] inputPeriodTimes) { //Finds the current period number of the day, and determines if it is passing time or if there is no School

        int i = 0; //array position
        //KEY: 0 start times hour, 1 start times min, 2 end times hour, 3 end times minute

        if ((currentHour < inputPeriodTimes[0][0]) ||
                (currentHour > inputPeriodTimes[2][((inputPeriodTimes[0].length) - 1)]) ||
                (currentHour == inputPeriodTimes[0][0] && currentMinute < inputPeriodTimes[1][0]) ||
                (currentHour == inputPeriodTimes[2][((inputPeriodTimes[0].length) - 1)] &&
                        currentMinute >= inputPeriodTimes[3][((inputPeriodTimes[0].length) - 1)])) { //Checks if the current time is before or after all values entered into the time array for the day

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
                return (i + 1);
                //return -1;
            }
        }
        return (i + 1);//returns period number, must subtract one to get proper array position
    }


    void displayPeriodString() { //Displays and Highlights the Numbers of the Period String

        String tempScheduleString = ""; //Allows for display of numbers by adding a 1 before their numerical equivalent

        for (int i = 0; i < scheduleFormat.length; i++) {
            if (scheduleFormat[i] >= 100) {
                String tempalphabet = "ABCDEFGHIJKLMNOPQRRSTUVWXYZ";
                tempScheduleString += tempalphabet.charAt(scheduleFormat[i] - 100);
            } else {
                tempScheduleString += scheduleFormat[i];
            }
            if (i < scheduleFormat.length - 1) tempScheduleString += " ";
        }

        int tempStartPos;
        int tempEndPos;
        if (currentPeriodNumber == 1) {
            tempStartPos = 0;
            tempEndPos = 1;
        } else {
            tempStartPos = ((currentPeriodNumber - 1) * 2);
            tempEndPos = ((currentPeriodNumber - 1) * 2) + 1;
        }
        //The below code was inspired from a StackOverflow answer by Jave
        //The full answer can be found here: https://stackoverflow.com/a/8518613

        SpannableStringBuilder sb = new SpannableStringBuilder(tempScheduleString);
        int tempColor = ContextCompat.getColor(this, R.color.colorScheduleHighlighted);
        ForegroundColorSpan fcs = new ForegroundColorSpan(tempColor);
        sb.setSpan(fcs, tempStartPos, tempEndPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (passingTime) {
            int tempPeriodPlacement = (currentPeriodNumber - 1);

            if (tempPeriodPlacement == 0) {
                tempStartPos = 0;
                tempEndPos = 1;
            } else {
                tempStartPos = ((tempPeriodPlacement - 1) * 2);
                tempEndPos = ((tempPeriodPlacement - 1) * 2) + 1;
            }

            int tempLastColor = ContextCompat.getColor(this, R.color.colorLastPeriodScheduleHighlighted);
            ForegroundColorSpan fcslast = new ForegroundColorSpan(tempLastColor);
            sb.setSpan(fcslast, (tempStartPos), (tempEndPos), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        TextView scheduleTextView = findViewById(R.id.ScheduleLayout);
        scheduleTextView.setText(sb);

        //END CODE ATTRIBUTION by Jave
    }

    void DisplayNoticeText() { //Displays some sick motivational quotes when called
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

        float tempTotalMinutes = Math.abs((totalTimeHour * 60) + totalTimeMinute);
        float tempLeftMinutes = Math.abs((timeUntilEndHour * 60) + timeUntilEndMinute);
        progressForBar = Math.round((tempLeftMinutes / tempTotalMinutes) * 100);
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

    void FindTimeUntilEndOfDay(int[][] finderinputPeriodTimes) { //Finds time until end of day

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
                tempTimeUntilMinute = ((finderinputPeriodTimes[3][(finderinputPeriodTimes[0].length) - 1]) - currentMinute);
                break;
            }
        }

        tempCurrentHour = finderinputPeriodTimes[0][0];

        while (true) {
            if (tempCurrentHour < finderinputPeriodTimes[2][(finderinputPeriodTimes[0].length) - 1]) {
                tempTotalTimeHour++;
                tempCurrentHour++;
            } else {
                tempTotalTimeMinute = ((finderinputPeriodTimes[3][(finderinputPeriodTimes[0].length) - 1]) - (finderinputPeriodTimes[1][0]));
                break;
            }
        }

        float tempTotalMinutes = Math.abs((tempTotalTimeHour * 60) + tempTotalTimeMinute);
        float tempLeftMinutes = Math.abs((tempTimeUntilHour * 60) + tempTimeUntilMinute);
        progressForOverallBar = 100 - Math.round((tempLeftMinutes / tempTotalMinutes) * 100);

    }


    void FinalizingSetupProcedures() { //Final setting of values on the UI

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

        if (!offline) DisplayNoticeText();

        if (passingTime) {
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

    void OfflineProcedures() { //Changes values on UI thread to reflect offline state

        TextView noticetext = findViewById(R.id.noticeOfTheDay);
        noticetext.setText("No Connection. Information may be inaccurate");
    }

    void NoSchoolProcedures() { //Changes values on UI thread to reflect no school state

        String tempScheduleString = "";
        for (int i = 0; i < scheduleFormat.length; i++) {
            tempScheduleString += scheduleFormat[i];
            if (i < scheduleFormat.length - 1) tempScheduleString += " ";
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
        if (!offline) {
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
                                scheduleFormat = NormalScheduleFormat("a");
                                Toast.makeText(MainPage.this, "Set as 'A' Day", Toast.LENGTH_LONG).show();
                                OfflineConditions();
                                break;
                            case 1:
                                scheduleFormat = NormalScheduleFormat("b");
                                Toast.makeText(MainPage.this, "Set as 'B' Day", Toast.LENGTH_LONG).show();
                                OfflineConditions();
                                break;
                            case 2:
                                scheduleFormat = NormalScheduleFormat("c");
                                Toast.makeText(MainPage.this, "Set as 'C' Day", Toast.LENGTH_LONG).show();
                                OfflineConditions();
                                break;
                            case 3:
                                scheduleFormat = NormalScheduleFormat("d");
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

    void reset() { //WILL REMOVE WHEN RESET BUTTON REMOVED FOR NEXT UPDATE
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }
}
