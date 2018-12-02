package owenspangler.stapleshighschoolscheduler;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.preference.PreferenceManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class MainPage extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    String jsonData; //Raw Json String Pulled From Async Task
    //
    int lunchPeriodPosition; //What position in the schedule the lunch period falls on (WARN: STARTS AT 1, not 0)
    int currentPeriodNumber = -1; //What position in schedule the current period is (WARN: STARTS AT 1, not 0)
    int progressForBar = 0; //Progress for Main Progress Bar
    int progressForOverallBar = 0; //Progress for Overall Outer Progress Bar
    int labLunchLength;
    //
    int[] scheduleFormat; //Consecutive List of Today's Periods
    int[][] periodTimes; //2D Array of Start Hours, Start Minutes, End Hours and End Minutes for all periods
    int[][] lunchWaveTimes; //2D array like periodTimes, but for lunch waves
    //
    String dayLetter; //What the day letter is
    String jsonNotice; //Quote of the day
    String progressBarTextPercent = ""; //Remaining Time Percent Displayed
    String progressBarTextTime = ""; //Remaining Time Text Displayed
    String progressBarTextDescription = "Remaining";//REDUNDANT: WILL REMOVE AFTER UPDATE
    //
    boolean noLunch = false; //If true, the day has no lunch period
    boolean noSchool = false; //If true, the entire day has no school
    boolean beforeSchool = false; //If true, is not during school, but before school
    boolean afterSchool = false; //If true, is not during school, but after school
    boolean futureView = false; //If true, user is viewing a date in calendar mode
    boolean labLunch = false; //If true, current period has a shortened lunch period
    boolean offline = false; //If true, no connection to server can be made
    boolean passingTime = false; //If true, time is between periods
    boolean specialSchedule = false; //If true, app is following special schedule from server
    //
    Calendar cal = Calendar.getInstance();
    int currentYear = cal.get(Calendar.YEAR);
    //int currentDayNum = cal.get(Calendar.DAY_OF_MONTH);
    int currentDayNum = 30;
    //int currentDayDay = cal.get(Calendar.DAY_OF_WEEK);
    //int currentMonth = (cal.get(Calendar.MONTH) + 1);
    int currentMonth = 11;
    //int currentHour = cal.get(Calendar.HOUR_OF_DAY);
    int currentHour = 12;
    //int currentMinute = cal.get(Calendar.MINUTE);
    int currentMinute = 20;
    ProgressBar progressBar;
    ProgressBar overallProgressBar;
    MyRecyclerViewAdapter adapter;
    RecyclerView recyclerView;
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //SETUP TOOLBAR

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        try {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        } catch (NullPointerException e) {
            //put stack trace here
        }


        //SETUP RECYCLER VIEW

        //Uses Code from StackOverflow Answer by Suragch
        //https://stackoverflow.com/a/40584425
        recyclerView = findViewById(R.id.main_schedule_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        //SETUP HAMBURGER MENU

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

        if (offline) {//if offline
            OfflineDayAlertPopup("No Connection. Pick a Day.");
        } else {//if not offline

            if (!noSchool)
                // BeforeOrAfterSchoolCheck(periodTimes);//checks to see if before or after school. noSchool checked in getJson

                if (noSchool) {// if no school
                    NoSchoolProcedures();
                } else if (beforeSchool) {//if before school
                    BeforeSchoolProcedures();
                } else if (afterSchool) {//if after school
                    AfterSchoolProcedures();
                } else {//normal condition
                    currentPeriodNumber = PeriodNumber(periodTimes);
                    displayScheduleListInfo(periodTimes,lunchWaveTimes);

                    if (noLunch) {//if school day does not have lunch
                        if (passingTime) {//school day w/o lunch during passing time
                            FindTimeUntilEndOfDay(periodTimes);
                            FindTimeUntilEndPassingTime(periodTimes);
                            FinalizingSetupProcedures();
                        } else {//school day w/o lunch during class time
                            FindTimeUntilEndOfDay(periodTimes);
                            FindTimeUntilEndNormal(periodTimes);
                            FinalizingSetupProcedures();
                        }
                    } else {//normal school day with lunch
                        if (lunchPeriodPosition == currentPeriodNumber) {//It is lunch period
                            //NEED TO PULL PREFERENCES FILE HERE
                            //IF DEFAULT VALUES, TREAT IT LIKE NORMAL PERIODS, NO NEED TO MERGE
                        } else {//It is not lunch period
                            if (passingTime) {//school day with lunch during non-lunch passing time
                                FindTimeUntilEndOfDay(periodTimes);
                                FindTimeUntilEndPassingTime(periodTimes);
                                FinalizingSetupProcedures();
                            } else {//school day with lunch during non-lunch class time
                                FindTimeUntilEndOfDay(periodTimes);
                                FindTimeUntilEndNormal(periodTimes);
                                FinalizingSetupProcedures();
                            }
                        }
                    }

                }
        }
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

            GetInfoFromJSON(jsonData, false, 0, 0);
        }

    }

    void GetInfoFromJSON(String inputData, boolean futureViewGet, int inputMonthOffsetJson, int inputDayOffsetJson) {//Responsible for Parsing JSON file
        int monthForJson;
        int dayForJson;

        //Code before try statement allows for reuse if user wants schedule at future date.
        if (futureViewGet) {//FutureViewGet will set date to one from calendar selected by user
            monthForJson = inputMonthOffsetJson;
            dayForJson = inputDayOffsetJson;

        } else {//If not Future View, will simply be offset. Use for after school for next day
            monthForJson = currentMonth + inputMonthOffsetJson;
            dayForJson = currentDayNum + inputDayOffsetJson;
        }

        if (!(DateValidator(monthForJson, dayForJson))) {
            if (monthForJson >= 12) {
                monthForJson = 1;
                dayForJson = 1;
            } else {
                monthForJson++;
                dayForJson = 1;
            }
        }


        try {
            JSONObject JO = new JSONObject(inputData);
            jsonNotice = JO.getString("notice");

            JSONArray scheduleChangeArray = JO.getJSONArray("schedulechange");

            for (int i = 0; i < scheduleChangeArray.length(); i++) { //checks to see if any days are listed as changed

                int tempMonth;
                int tempDay;
                JSONObject ARRJO = scheduleChangeArray.getJSONObject(i);
                //JO.getJSONObject("schedulechange").getJSONObject(Integer.toString(i)).getInt("month");

                tempMonth = ARRJO.getInt("month");
                tempDay = ARRJO.getInt("day");
                Log.i("currentmonth", Integer.toString(tempMonth));
                Log.i("currentday", Integer.toString(tempDay));
                Log.i("arraylength", Integer.toString(scheduleChangeArray.length()));

                if ((tempMonth == (monthForJson)) && (tempDay == (dayForJson))) { //found day listed matches today's date

                    specialSchedule = true;

                    dayLetter = ARRJO.getString("dayletter");

                    JSONArray tempStartTimesHourArray = ARRJO.getJSONArray("starttimeshour");
                    JSONArray tempStartTimesMinuteArray = ARRJO.getJSONArray("starttimesminute");
                    JSONArray tempEndTimesHourArray = ARRJO.getJSONArray("endtimeshour");
                    JSONArray tempEndTimesMinuteArray = ARRJO.getJSONArray("endtimesminute");
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

                    JSONArray tempStartLunchHourArray = ARRJO.getJSONArray("lunchwavesstarthour");
                    JSONArray tempStartLunchMinuteArray = ARRJO.getJSONArray("lunchwavesstartminute");
                    JSONArray tempEndLunchHourArray = ARRJO.getJSONArray("lunchwavesendhour");
                    JSONArray tempEndLunchMinuteArray = ARRJO.getJSONArray("lunchwavesendminute");

                    if (tempStartLunchHourArray.length() == 0) { // if nothing in array, no lunch

                        noLunch = true;

                    } else {//if values in array, has lunch

                        lunchPeriodPosition = ARRJO.getInt("lunchperiodposition");
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
                int tempDayListStart = JO.getJSONObject("dayletters").getJSONObject(Integer.toString(monthForJson)).getInt("dayletterliststart");
                JSONArray DayLetterListArray = JO.getJSONObject("dayletters").getJSONObject(Integer.toString(monthForJson)).getJSONArray("dayletterlist");
                boolean tempFound = false;
                int tempPosition = -1;

                for (int i = 0; i < DayLetterListArray.length(); i++) {
                    if (DayLetterListArray.getInt(i) == dayForJson) {
                        tempFound = true;
                        tempPosition = i;
                        break;
                    }
                }

                if (tempFound) {
                    if (((tempPosition + tempDayListStart) % 4) == 0) {
                        dayLetter = "a";
                        NormalScheduleFormat("a");//new int[]{1, 2, 3, 5, 8, 7}; //'A' day
                    } else if (((tempPosition + tempDayListStart) % 4) == 1) {
                        dayLetter = "b";
                        NormalScheduleFormat("b");//new int[]{2, 3, 4, 6, 7, 8}; //'B' day
                    } else if (((tempPosition + tempDayListStart) % 4) == 2) {
                        dayLetter = "c";
                        NormalScheduleFormat("c");//new int[]{3, 4, 1, 7, 6, 5}; //'C' day
                    } else {
                        dayLetter = "d";
                        NormalScheduleFormat("d");//new int[]{4, 1, 2, 8, 5, 6}; //'D' day
                    }

                    BeforeOrAfterSchoolCheck(periodTimes);
                } else {
                    noSchool = true;
                }
            }

            if ((noSchool) || (afterSchool)) {//finds next day and displays schedule for that day

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

    boolean DateValidator(int inputMonth, int inputDay) { //checks if generated date is actually a real date

        if (inputMonth == 2) { //February Case

            if ((currentYear % 100) == 0) {

                if ((currentYear % 400) == 0) {

                    return (inputDay <= 29);//Leap Year if div by 100 and 400

                } else {

                    return (inputDay <= 28);//Not leap year if div by 100 but not 400

                }

            } else if ((currentYear % 4) == 0) {

                return (inputDay <= 29);//Leap Year if div by 4 but not 100

            } else {
                return (inputDay <= 28);//Not leap year if not div by 4
            }

        } else if ((inputMonth == 1) || (inputMonth == 3) || (inputMonth == 5) || (inputMonth == 7) || (inputMonth == 8) || (inputMonth == 10) || (inputMonth == 12)) { //31 days

            return (inputDay <= 31);

        } else {//30 days

            return (inputDay <= 30);

        }

    }

    void NormalScheduleFormat(String inputDayType) { //EDIT THIS FUNCTION IF BASELINE SCHEDULE CHANGES AND PUSH UPDATE

        periodTimes = new int[][]{ //CHANGE BELOW TIMES WHEN SCHEDULE CHANGES

                {7, 8, 9, 10, 12, 13},//START HOUR
                {30, 25, 50, 45, 30, 25},//START MINUTE

                {8, 9, 10, 12, 13, 14},//END HOUR
                {20, 45, 40, 25, 20, 15}//END MINUTE
        };

        lunchPeriodPosition = 4; //Period Position that is extended for lunch waves

        lunchWaveTimes = new int[][]{ //Lunch Wave Times Within Extended Lunch Period
                {10, 11, 11},//START HOUR
                {45, 20, 55},//START MINUTE
                {11, 11, 12},//END HOUR
                {15, 50, 25}//END MINUTE
        };

        labLunchLength = 20; //Length of shortened lab lunch including passing time

        //Period Label Format Below
        if (inputDayType.equals("a")) {
            scheduleFormat = new int[]{1, 2, 3, 5, 8, 7}; //'A' day
        } else if (inputDayType.equals("b")) {
            scheduleFormat = new int[]{2, 3, 4, 6, 7, 8}; //'B' day
        } else if (inputDayType.equals("c")) {
            scheduleFormat = new int[]{3, 4, 1, 7, 6, 5}; //'C' day
        } else if (inputDayType.equals("d")) {
            scheduleFormat = new int[]{4, 1, 2, 8, 5, 6}; //'D' day
        }
    }


    int PeriodNumber(int[][] inputPeriodTimes) { //Finds the current period number of the day, and determines if it is passing time or if there is no School

        if (noSchool) return -1;

        int i = 0; //array position
        //KEY: 0 start times hour, 1 start times min, 2 end times hour, 3 end times minute

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

    void BeforeOrAfterSchoolCheck(int[][] inputPeriodTimes) {
        if ((currentHour < inputPeriodTimes[0][0]) ||
                (currentHour > inputPeriodTimes[2][((inputPeriodTimes[0].length) - 1)])) {
            beforeSchool = true;
        } else if ((currentHour == inputPeriodTimes[0][0] && currentMinute < inputPeriodTimes[1][0]) ||
                (currentHour == inputPeriodTimes[2][((inputPeriodTimes[0].length) - 1)] &&
                        currentMinute >= inputPeriodTimes[3][((inputPeriodTimes[0].length) - 1)])) { //Checks if the current time is before or after all values entered into the time array for the day
            afterSchool = true;
        }
    }

    void displayScheduleListInfo(int[][] inputPeriodTimes, int[][] inputLunchWaveTimes) {
        ArrayList<String> periodNumbers = new ArrayList<>();
        ArrayList<String> periodNames = new ArrayList<>();
        ArrayList<String> periodStart = new ArrayList<>();
        ArrayList<String> periodEnd = new ArrayList<>();
        ArrayList<String> lunchWave = new ArrayList<>();


        for (int i = 0; i < scheduleFormat.length; i++) {

            String tempPeriodNameString = sharedPref.getString("key_schedule_period_"+Integer.toString(scheduleFormat[i])+"_name", "Period "+Integer.toString(scheduleFormat[i]));

            if ((i+1) == lunchPeriodPosition){
                String tempPref = sharedPref.getString("key_schedule_period_"+Integer.toString(scheduleFormat[i])+"_type","Free or Not Applicable");
                int tempAllowedLunchWave = findAllowedLunchWave(tempPref, currentMonth);
                Log.i("tempallowedlunchwave",Integer.toString(tempAllowedLunchWave));
                Log.i("tempPref", tempPref);
                switch (tempAllowedLunchWave){
                    case 0: //Free or Not Applicable
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        periodNames.add(tempPeriodNameString);
                        periodNames.add(tempPeriodNameString);
                        periodNames.add(tempPeriodNameString);
                        lunchWave.add("1");
                        lunchWave.add("2");
                        lunchWave.add("3");

                        for(int j = 0; j< inputLunchWaveTimes[0].length;j++) {
                            int tempStartHour = ToTwelveHour(inputLunchWaveTimes[0][j]);
                            int tempEndHour = ToTwelveHour(inputLunchWaveTimes[2][j]);
                            int tempStartMinute = inputLunchWaveTimes[1][j];
                            int tempEndMinute = inputLunchWaveTimes[3][j];
                            String tempStartTimeString;
                            String tempEndTimeString;

                            if(tempStartMinute >= 10) {
                                tempStartTimeString = tempStartHour + ":" + tempStartMinute;
                            }else{
                                tempStartTimeString = tempStartHour + ":0" + tempStartMinute;
                            }

                            if(tempEndMinute >= 10) {
                                tempEndTimeString = tempEndHour + ":" + tempEndMinute;
                            }else{
                                tempEndTimeString = tempEndHour + ":0" + tempEndMinute;
                            }

                            periodStart.add(tempStartTimeString);
                            periodEnd.add(tempEndTimeString);
                        }
                        //periodStart.add(tempStartTimeString);
                        //periodEnd.add(tempEndTimeString);

                        break;
                    case 1: //First Lunch
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        lunchWave.add("1");
                        periodNames.add("Lunch");
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);

                    {
                        int tempStartHour = ToTwelveHour(inputLunchWaveTimes[0][0]);
                        int tempEndHour = ToTwelveHour(inputLunchWaveTimes[2][0]);
                        int tempStartMinute = inputLunchWaveTimes[1][0];
                        int tempEndMinute = inputLunchWaveTimes[3][0];
                        String tempStartTimeString;
                        String tempEndTimeString;

                        if(tempStartMinute >= 10) {
                            tempStartTimeString = tempStartHour + ":" + tempStartMinute;
                        }else{
                            tempStartTimeString = tempStartHour + ":0" + tempStartMinute;
                        }

                        if(tempEndMinute >= 10) {
                            tempEndTimeString = tempEndHour + ":" + tempEndMinute;
                        }else{
                            tempEndTimeString = tempEndHour + ":0" + tempEndMinute;
                        }
                        periodStart.add(tempStartTimeString);
                        periodEnd.add(tempEndTimeString);

                        //
                        tempStartHour = ToTwelveHour(inputLunchWaveTimes[0][1]);
                        tempEndHour = ToTwelveHour(inputLunchWaveTimes[2][2]);
                        tempStartMinute = inputLunchWaveTimes[1][1];
                        tempEndMinute = inputLunchWaveTimes[3][2];

                        if(tempStartMinute >= 10) {
                            tempStartTimeString = tempStartHour + ":" + tempStartMinute;
                        }else{
                            tempStartTimeString = tempStartHour + ":0" + tempStartMinute;
                        }

                        if(tempEndMinute >= 10) {
                            tempEndTimeString = tempEndHour + ":" + tempEndMinute;
                        }else{
                            tempEndTimeString = tempEndHour + ":0" + tempEndMinute;
                        }
                        periodStart.add(tempStartTimeString);
                        periodEnd.add(tempEndTimeString);

                    }

                        break;
                    case 2: //Second Lunch
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);
                        lunchWave.add("2");
                        periodNames.add("Lunch");
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);

                        for(int j = 0; j < inputLunchWaveTimes[0].length;j++) {
                            int tempStartHour = ToTwelveHour(inputLunchWaveTimes[0][j]);
                            int tempEndHour = ToTwelveHour(inputLunchWaveTimes[2][j]);
                            int tempStartMinute = inputLunchWaveTimes[1][j];
                            int tempEndMinute = inputLunchWaveTimes[3][j];
                            String tempStartTimeString;
                            String tempEndTimeString;
                            if(tempStartMinute >= 10) {
                                tempStartTimeString = tempStartHour + ":" + tempStartMinute;
                            }else{
                                tempStartTimeString = tempStartHour + ":0" + tempStartMinute;
                            }

                            if(tempEndMinute >= 10) {
                                tempEndTimeString = tempEndHour + ":" + tempEndMinute;
                            }else{
                                tempEndTimeString = tempEndHour + ":0" + tempEndMinute;
                            }

                            periodStart.add(tempStartTimeString);
                            periodEnd.add(tempEndTimeString);
                        }

                        break;
                    case 3: //Third Lunch
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);
                        lunchWave.add("3");
                        periodNames.add("Lunch");

                    {
                        int tempStartHour = ToTwelveHour(inputLunchWaveTimes[0][0]);
                        int tempEndHour = ToTwelveHour(inputLunchWaveTimes[2][1]);
                        int tempStartMinute = inputLunchWaveTimes[1][0];
                        int tempEndMinute = inputLunchWaveTimes[3][1];
                        String tempStartTimeString;
                        String tempEndTimeString;

                        if(tempStartMinute >= 10) {
                            tempStartTimeString = tempStartHour + ":" + tempStartMinute;
                        }else{
                            tempStartTimeString = tempStartHour + ":0" + tempStartMinute;
                        }

                        if(tempEndMinute >= 10) {
                            tempEndTimeString = tempEndHour + ":" + tempEndMinute;
                        }else{
                            tempEndTimeString = tempEndHour + ":0" + tempEndMinute;
                        }
                        periodStart.add(tempStartTimeString);
                        periodEnd.add(tempEndTimeString);
                        //
                        tempStartHour = ToTwelveHour(inputLunchWaveTimes[0][2]);
                        tempEndHour = ToTwelveHour(inputLunchWaveTimes[2][2]);
                        tempStartMinute = inputLunchWaveTimes[1][2];
                        tempEndMinute = inputLunchWaveTimes[3][2];

                        if(tempStartMinute >= 10) {
                            tempStartTimeString = tempStartHour + ":" + tempStartMinute;
                        }else{
                            tempStartTimeString = tempStartHour + ":0" + tempStartMinute;
                        }

                        if(tempEndMinute >= 10) {
                            tempEndTimeString = tempEndHour + ":" + tempEndMinute;
                        }else{
                            tempEndTimeString = tempEndHour + ":0" + tempEndMinute;
                        }
                        periodStart.add(tempStartTimeString);
                        periodEnd.add(tempEndTimeString);

                    }

                        break;
                    case 11: //First Lab Lunch
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        lunchWave.add("L1");
                        periodNames.add("Lab Lunch");
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);

                    {
                        int tempStartHour = inputLunchWaveTimes[0][0];
                        int tempEndHour = inputLunchWaveTimes[2][0];
                        int tempStartMinute = inputLunchWaveTimes[1][0];
                        int tempEndMinute;

                        if((tempStartMinute + labLunchLength) > 60){
                            tempEndHour++;
                            tempEndMinute = (tempStartMinute + labLunchLength) - 60;
                        }else{
                            tempEndMinute = tempStartMinute +labLunchLength;
                        }

                        tempStartHour = ToTwelveHour(tempStartHour);
                        tempEndHour = ToTwelveHour(tempEndHour);

                        String tempStartTimeString;
                        String tempEndTimeString;

                        if(tempStartMinute >= 10) {
                            tempStartTimeString = tempStartHour + ":" + tempStartMinute;
                        }else{
                            tempStartTimeString = tempStartHour + ":0" + tempStartMinute;
                        }

                        if(tempEndMinute >= 10) {
                            tempEndTimeString = tempEndHour + ":" + tempEndMinute;
                        }else{
                            tempEndTimeString = tempEndHour + ":0" + tempEndMinute;
                        }

                        periodStart.add(tempStartTimeString);
                        periodEnd.add(tempEndTimeString);
                        //
                        tempStartHour = tempEndHour;
                        tempEndHour = ToTwelveHour(inputLunchWaveTimes[2][2]);
                        tempStartMinute = tempEndMinute;
                        tempEndMinute = inputLunchWaveTimes[3][2];

                        if(tempStartMinute >= 10) {
                            tempStartTimeString = tempStartHour + ":" + tempStartMinute;
                        }else{
                            tempStartTimeString = tempStartHour + ":0" + tempStartMinute;
                        }

                        if(tempEndMinute >= 10) {
                            tempEndTimeString = tempEndHour + ":" + tempEndMinute;
                        }else{
                            tempEndTimeString = tempEndHour + ":0" + tempEndMinute;
                        }
                        periodStart.add(tempStartTimeString);
                        periodEnd.add(tempEndTimeString);
                    }
                        break;
                    case 12: //Second Lab Lunch
                        //This will never happen unless the policy somehow changes
                        /*
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);
                        lunchWave.add("2");
                        periodNames.add("Lab Lunch");
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);
                        */
                        break;
                    case 13: //Third Lab Lunch
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);
                        lunchWave.add("L3");
                        periodNames.add("Lab Lunch");

                    {
                        int tempStartHour = ToTwelveHour(inputLunchWaveTimes[0][0]);
                        int tempEndHour = ToTwelveHour(inputLunchWaveTimes[2][1]);
                        int tempStartMinute = inputLunchWaveTimes[1][0];
                        int tempEndMinute = inputLunchWaveTimes[3][1];

                        if((tempEndMinute + labLunchLength) < 60){
                            tempEndHour--;
                            tempEndMinute = 60 + (tempEndMinute-labLunchLength);
                        }else{
                            tempEndMinute = tempEndMinute - labLunchLength;
                        }

                        String tempStartTimeString;
                        String tempEndTimeString;

                        if(tempStartMinute >= 10) {
                            tempStartTimeString = tempStartHour + ":" + tempStartMinute;
                        }else{
                            tempStartTimeString = tempStartHour + ":0" + tempStartMinute;
                        }

                        if(tempEndMinute >= 10) {
                            tempEndTimeString = tempEndHour + ":" + tempEndMinute;
                        }else{
                            tempEndTimeString = tempEndHour + ":0" + tempEndMinute;
                        }
                        periodStart.add(tempStartTimeString);
                        periodEnd.add(tempEndTimeString);
                        //
                        tempStartHour = tempEndHour;
                        tempEndHour = ToTwelveHour(inputLunchWaveTimes[2][2]);
                        tempStartMinute = tempEndMinute;
                        tempEndMinute = inputLunchWaveTimes[3][2];

                        if(tempStartMinute >= 10) {
                            tempStartTimeString = tempStartHour + ":" + tempStartMinute;
                        }else{
                            tempStartTimeString = tempStartHour + ":0" + tempStartMinute;
                        }

                        if(tempEndMinute >= 10) {
                            tempEndTimeString = tempEndHour + ":" + tempEndMinute;
                        }else{
                            tempEndTimeString = tempEndHour + ":0" + tempEndMinute;
                        }
                        periodStart.add(tempStartTimeString);
                        periodEnd.add(tempEndTimeString);
                    }

                        break;

                     default:


                }

            }else{//List Fill for Normal Periods
                periodNumbers.add(Integer.toString(scheduleFormat[i]));
                periodNames.add(tempPeriodNameString);
                lunchWave.add(" ");

                int tempStartHour = inputPeriodTimes[0][i];
                int tempEndHour = inputPeriodTimes[2][i];

                if (tempStartHour > 12) {
                    tempStartHour = tempStartHour - 12;
                    //tempStartPM = true;
                }

                if (tempEndHour > 12) {
                    tempEndHour = tempEndHour - 12;
                    //tempEndPM = true;
                }
                String tempStartTimeString = tempStartHour + ":" + inputPeriodTimes[1][i];
                String tempEndTimeString = tempEndHour + ":" + inputPeriodTimes[3][i];

                periodStart.add(tempStartTimeString);
                periodEnd.add(tempEndTimeString);
            }


        }

        //for (int i = 0; i < (inputPeriodTimes[0].length); i++) {
            //boolean tempStartPM = false;
            //boolean tempEndPM = false;
        /*
            int tempStartHour = inputPeriodTimes[0][i];
            //int tempStartMinute = inputPeriodTimes[1][i];
            int tempEndHour = inputPeriodTimes[2][i];
            //int tempEndMinute = inputPeriodTimes[3][i];


            if (tempStartHour > 12) {
                tempStartHour = tempStartHour - 12;
                //tempStartPM = true;
            }

            if (tempEndHour > 12) {
                tempEndHour = tempEndHour - 12;
                //tempEndPM = true;
            }
            String tempStartTimeString = tempStartHour + ":" + inputPeriodTimes[1][i];
            String tempEndTimeString = tempEndHour + ":" + inputPeriodTimes[3][i];

            periodStart.add(tempStartTimeString);
            periodEnd.add(tempEndTimeString);
        //}
        */
/*
        if(noLunch){

        }else {
            for (int i = 0; i < scheduleFormat.length; i++) {
                lunchWave.add(" ");
            }
        }
*/
        Log.i("periodnumers", periodNumbers.toString());
        Log.i("periodnames", periodNames.toString());
        Log.i("periodstart", periodStart.toString());
        Log.i("periodend", periodEnd.toString());


        // set up the RecyclerView
        if(!((periodNames.size() == periodNumbers.size())&&(periodStart.size() == periodNumbers.size()) && (periodEnd.size() == periodNumbers.size()) && (periodEnd.size()  == lunchWave.size()))){
            //Throw Error Message
            ErrorPopup("Owen has done Goofed Up", "Code 420");
            periodNumbers.clear();
            periodNames.clear();
            periodStart.clear();
            periodEnd.clear();

            periodNumbers.add("420");
            periodNames.add("Bad Error");
            periodStart.add("WTF");
            periodEnd.add("Goddamit");
        }

        adapter = new MyRecyclerViewAdapter(this, periodNumbers, periodNames, periodStart, periodEnd, lunchWave);
        recyclerView.setAdapter(adapter);

    }

    int ToTwelveHour(int input){
        if (input > 12) {
            input = input - 12;
        }
       return input;
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

    void FindTimeUntilEndLunchWave(int finderinputPeriodTimes[][]) { //Finds the time until the end of the period

        int timeUntilEndHour = 0;
        int timeUntilEndMinute = 0;
        int totalTimeHour = 0;
        int totalTimeMinute = 0;
        int PeriodArrayPosition = (currentPeriodNumber - 1);
        int currentPeriodIDNumber = scheduleFormat[PeriodArrayPosition];
        String PeriodType;

        if ((currentPeriodIDNumber>0) && (currentPeriodIDNumber<=8)) {
            PeriodType = sharedPref.getString(("key_schedule_period_" + Integer.toString(currentPeriodIDNumber) + "_type"), "Free or Not Applicable");
        }else{
            PeriodType = "Free or Not Applicable";
            Log.i("periodTypeDef",PeriodType);
        }

        if(PeriodType.equals("0")){
            PeriodType = "Free or Not Applicable";
        }

        int allowedLunchWave = findAllowedLunchWave(PeriodType,currentMonth);

        ///
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

    int findAllowedLunchWave(String periodTypeName, int tempMonth){
        int lunchStoreListStart = 8; //Tells program where list of period numbers start (Def 8 = August)
        int[] lunchStoreList = {};
        labLunch = false;
        Log.i("findallowedrun","gggg");
        switch(periodTypeName) {
            case "Free or Not Applicable":
                return -1;
            case "Academic Support Center":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 2 , 2 , 2 , 2 , 2 , 2 , 2 , 2 , 1 , 1 , 1 };
                break;
            case "Art":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 };
                break;
            case "English":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 2 , 2 , 2 , 2 , 2 , 2 , 3 , 3 , 3 , 3 , 3 };
                break;
            case "Family and Consumer Sciences":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 };
                break;
            case "Mathematics":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 1 , 1 , 1 , 1 , 1 , 1 , 2 , 2 , 2 , 2 , 2 };
                break;
            case "Media":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 1 , 1 , 1 , 3 , 3 , 3 , 1 , 1 , 1 , 1 , 1 };
                break;
            case "Music":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 };
                break;
            case "Physical Education":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 };
                break;
            case "Science":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 3 , 3 , 3 , 1 , 1 , 1 , 1 , 1 , 1 , 1 , 1 };
                labLunch = true;
                break;
            case "Social Studies":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 1 , 1 , 1 , 3 , 3 , 3 , 2 , 2 , 2 , 2 , 2 };
                break;
            case "Special Education":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 2 , 2 , 2 , 2 , 2 , 2 , 1 , 1 , 2 , 2 , 2 };
                break;
            case "Technology":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 1 , 1 , 1 , 3 , 3 , 3 , 3 , 3 , 3 , 3 , 3 };
                break;
            case "Theater":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 2 , 2 , 2 , 2 , 2 , 2 , 3 , 3 , 2 , 2 , 2 };
                break;
            case "World Languages":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{ 2 , 2 , 2 , 2 , 2 , 2 , 1 , 1 , 1 , 1 , 1 };
                break;
            case "Unknown: First Lunch":
                return 1;
            case "Unknown: Second Lunch":
                return 2;
            case "Unknown: Third Lunch":
                return 3;
            default:
                return 0;
        }

        int tempSchedulePosition;

        if (tempMonth >= lunchStoreListStart){
            tempSchedulePosition = tempMonth - lunchStoreListStart;
        }else{
            tempSchedulePosition = tempMonth + (12-lunchStoreListStart);
        }

        Log.i("lunchwaveallow", Integer.toString(lunchStoreList[tempSchedulePosition]));

        if(labLunch){
            return (lunchStoreList[tempSchedulePosition] + 10);
        }else {
            return lunchStoreList[tempSchedulePosition];
        }
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

    void BeforeSchoolProcedures() {

    }

    void AfterSchoolProcedures() {

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
                                NormalScheduleFormat("a");
                                Toast.makeText(MainPage.this, "Set as 'A' Day", Toast.LENGTH_LONG).show();
                                OfflineConditions();
                                break;
                            case 1:
                                NormalScheduleFormat("b");
                                Toast.makeText(MainPage.this, "Set as 'B' Day", Toast.LENGTH_LONG).show();
                                OfflineConditions();
                                break;
                            case 2:
                                NormalScheduleFormat("c");
                                Toast.makeText(MainPage.this, "Set as 'C' Day", Toast.LENGTH_LONG).show();
                                OfflineConditions();
                                break;
                            case 3:
                                NormalScheduleFormat("d");
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

    void ErrorPopup(String inputErrorText, String code) {

    }
}
