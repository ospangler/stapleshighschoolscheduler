package owenspangler.stapleshighschoolscheduler;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

    //String jsonData; //Raw Json String Pulled From Async Task
    //
    int lunchPeriodPosition; //What position in the schedule the lunch period falls on
    int currentPeriodNumber = -1; //What position in schedule the current period is
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
    String imageLink;
    String progressBarTextPercent = ""; //Remaining Time Percent Displayed
    String progressBarTextTime = ""; //Remaining Time Text Displayed
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
    boolean firstTime = true;
    boolean halt = false;
    //
    int currentYear;
    int currentMonth;
    int currentDayNum;
    int currentHour;
    int currentMinute;
    //
    ProgressBar progressBar;
    ProgressBar overallProgressBar;
    MyRecyclerViewAdapter adapter;
    RecyclerView recyclerView;
    SharedPreferences sharedPref;
    ActionBarDrawerToggle mDrawerToggle;


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

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {//Toggle to see if side menu open or closed

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.i("draweropened", jsonNotice);
                //ImageView headerImage = findViewById(R.id.header_image);

                if (imageLink != null) {

                    new ImageFetcher((ImageView) findViewById(R.id.header_image))
                            .execute(imageLink);

                }

                TextView noticeText = findViewById(R.id.header_text);
                noticeText.setText(jsonNotice);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.i("drawerclosed", "jdjdjd");
                invalidateOptionsMenu();
            }

        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

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
                            //startActivity(new Intent(MainPage.this, NotificationActivity.class));
                            return true;
                        } else if (id == R.id.nav_schedule_input) {
                            startActivity(new Intent(MainPage.this, ScheduleInputActivity.class));
                            return true;
                        } else if (id == R.id.nav_quote) {
                            //startActivity(new Intent(MainPage.this, NotificationActivity.class));
                            return true;
                        } else if (id == R.id.nav_settings) {
                            //startActivity(new Intent(MainPage.this, GeneralSettingsActivity.class));
                            return true;
                        } else if (id == R.id.nav_force_server_refresh) {
                            Main(true);
                        }


                        return MainPage.super.onOptionsItemSelected(menuItem);
                    }
                });

        Main(true);
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
    int delay = 1000; //sets refresh delay for app
    Runnable runnable;
    int previousMinute = currentMinute;

    @Override
    protected void onResume() {
        //start handler as activity become visible

        h.postDelayed(runnable = new Runnable() {
            public void run() {
                Calendar tempChangeCal;
                tempChangeCal = Calendar.getInstance();
                int newMinute = tempChangeCal.get(Calendar.MINUTE);
                if (previousMinute != newMinute) {
                    RefreshReset();
                    previousMinute = newMinute;
                    Main(false);
                }
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

    void Main(boolean first) {//Initial Code that executes on startup

        RefreshTime();

        if (first) GetJson();

        if(halt){

        }else {

            if (!noSchool)
                BeforeOrAfterSchoolCheck(periodTimes);//checks to see if before or after school. noSchool checked in getJson

            if (noSchool) {// if no school
                NoSchoolProcedures();
            } else if (beforeSchool) {//if before school
                BeforeSchoolProcedures();
            } else if (afterSchool) {//if after school
                AfterSchoolProcedures();
            } else {//normal condition

                currentPeriodNumber = PeriodNumber(periodTimes, true);

                displayScheduleListInfo(periodTimes, lunchWaveTimes);

                if (noLunch) {//if school day does not have lunch

                    if (passingTime) {//school day w/o lunch during passing time

                        FindTimeUntilEndOfDay(periodTimes);
                        FindTimeUntilEndPassingTime(periodTimes, currentPeriodNumber);
                        FinalizingSetupProcedures();

                    } else {//school day w/o lunch during class time

                        FindTimeUntilEndOfDay(periodTimes);
                        FindTimeUntilEndNormal(periodTimes);
                        FinalizingSetupProcedures();

                    }

                } else {//normal school day with lunch

                    if (lunchPeriodPosition == currentPeriodNumber) {//It is lunch period

                        Log.i("lunchwavetime", "rah jd");

                        PeriodNumber(periodTimes, true);

                        if (passingTime) { //Passing time after normal periods but before lunch waves (10:41 case on normal day)

                            FindTimeUntilEndPassingTime(periodTimes, currentPeriodNumber);
                            FindTimeUntilEndOfDay(periodTimes);
                            FinalizingSetupProcedures();
                            Log.i("lunchbefore", "rah jd 1041");

                        } else {

                            int tempLunchPeriod = PeriodNumber(lunchWaveTimes, false); //Resets, sees if passing time during lunch waves
                            //ROOT OF PROBLEM RIGHT HERE, DOES NOT ALLOW FOR PERIOD MERGING. MUST BE MOVED INSIDE FIND TIME UNTIL BECAUSE OF POSITONS
                            //USE LUNCH DURING PASSING TIME TO TEST

                            if (passingTime) {

                                FindTimeUntilEndPassingTime(lunchWaveTimes, tempLunchPeriod);
                                Log.i("lunchwavetimepassing", "rah jd");
                                FindTimeUntilEndOfDay(periodTimes);
                                FinalizingSetupProcedures();

                            } else {

                                Log.i("lunchwavetimeduring", "rah jd");
                                FindTimeUntilEndOfDay(periodTimes);
                                FindTimeUntilEndLunchWave(lunchWaveTimes, (tempLunchPeriod));
                                FinalizingSetupProcedures();

                            }
                        }

                    } else {//It is not lunch period

                        if (passingTime) {//school day with lunch during non-lunch passing time
                            Log.i("whyareyourunning?", "memes aside, why tho?");
                            FindTimeUntilEndOfDay(periodTimes);
                            FindTimeUntilEndPassingTime(periodTimes, currentPeriodNumber);
                            FinalizingSetupProcedures();

                        } else {//school day with lunch during non-lunch class time

                            FindTimeUntilEndOfDay(periodTimes);
                            FindTimeUntilEndNormal(periodTimes);
                            FinalizingSetupProcedures();
                        }
                    }
                }

            }

            firstTime = false;
        }
    }


    void GetJson() {
        String jsonData;
        try {
            jsonData = new JSONfetcher().execute().get();//Will wait for task to finish
        } catch (InterruptedException e) {
            jsonData = "";
            //I'm not catching anything, I just wanted the error messages to go away
        } catch (ExecutionException e) {
            jsonData = "";
            //I don't care if I'm defeating the purpose of an Async task, I don't care.
        }

        if ((jsonData.equals("NO CONNECTION")) || (jsonData.equals(""))) { //NO CONNECTION CONDITION
            offline = true;

            //Retrieving JSON from Shared Preferences
            //Below Code adapted from a StackOverflow Answer by
            //Full answer can be found at: https://stackoverflow.com/a/29607632

            String offlineJsonData = PreferenceManager.getDefaultSharedPreferences(this).getString("offline_database", "");
            String offlineJsonDataTime = PreferenceManager.getDefaultSharedPreferences(this).getString("offline_database_time", "");

            if (offlineJsonData.isEmpty()) {

                WarningPopup("No Offline Backup", "Your device is offline and has never retrieved a database backup. Connect your device to the internet.");
                Log.i("No previous connection", "No database detected");

            } else {

                //https://developer.android.com/training/snackbar/showing
                Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Offline. Using database backup from: " + offlineJsonDataTime,
                        15000).show();


                Log.i("Previous Connection", "Using Backup Data");
                GetInfoFromJSON(offlineJsonData, false, 0, 0);
            }

        } else {//WITH CONNECTION

            Log.i("Is Connection", "Writing new data");

            //Saving updated JSON to Shared Preferences
            //Below Code adapted from a StackOverflow Answer by
            //Full answer can be found at: https://stackoverflow.com/a/29607632

            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString("offline_database", jsonData).apply();

            String tempUpdateTime = Calendar.getInstance().getTime().toString();

            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString("offline_database_time", tempUpdateTime).apply();

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
            JSONArray scheduleChangeArray = JO.getJSONArray("schedulechange");

            jsonNotice = JO.getString("notice");

            imageLink = JO.getString("imagelink");

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
                } else {
                    noSchool = true;
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

        lunchPeriodPosition = 3; //Period Position that is extended for lunch waves (WARN: STARTS AT ZERO)

        lunchWaveTimes = new int[][]{ //Lunch Wave Times Within Extended Lunch Period
                {10, 11, 11},//START HOUR
                {45, 20, 55},//START MINUTE
                {11, 11, 12},//END HOUR
                {15, 50, 25}//END MINUTE
        };

        labLunchLength = 20; //Length of shortened lab lunch including passing time

        //Period Label Format Below
        switch (inputDayType) {
            case "a": {
                scheduleFormat = new int[]{1, 2, 3, 5, 8, 7}; //'A' day
            }
            break;
            case "b": {
                scheduleFormat = new int[]{2, 3, 4, 6, 7, 8}; //'B' day
            }
            break;
            case "c": {
                scheduleFormat = new int[]{3, 4, 1, 7, 6, 5}; //'C' day
            }
            break;
            case "d": {
                scheduleFormat = new int[]{4, 1, 2, 8, 5, 6}; //'D' day
            }
            break;
            default: //catch errors here
                WarningPopup("Invalid Day Type Found", "Likely caused by server input error. Information may be inaccurate. Contact Developer. Code 3.");
                scheduleFormat = new int[]{4, 1, 2, 8, 5, 6}; //'D' day

        }
    }

    int findAllowedLunchWave(String periodTypeName, int tempMonth) { //EDIT THIS FUNCTION IF LUNCH SCHEDULE CHANGES AND PUSH UPDATE

        int lunchStoreListStart = 8; //Tells program where list of period numbers start (DEFAULT IS 8 = August)
        int[] lunchStoreList;
        labLunch = false;

        switch (periodTypeName) {
            case "Free or Not Applicable":
                return 0;
            case "Academic Support Center":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1};
                break;
            case "Art":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
                break;
            case "English":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3};
                break;
            case "Family and Consumer Sciences":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
                break;
            case "Mathematics":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2};
                break;
            case "Media":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{1, 1, 1, 3, 3, 3, 1, 1, 1, 1, 1};
                break;
            case "Music":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
                break;
            case "Physical Education":
                ///PLEASE REMOVE THIS OMG DON"T RELEASE THIS WITH THIS BELOW STATEMENT
                labLunch = true;
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
                break;
            case "Science":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 1};
                labLunch = true;
                break;
            case "Social Studies":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{1, 1, 1, 3, 3, 3, 2, 2, 2, 2, 2};
                break;
            case "Special Education":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2};
                break;
            case "Technology":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3};
                break;
            case "Theater":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{2, 2, 2, 2, 2, 2, 3, 3, 2, 2, 2};
                break;
            case "World Languages":
                //                         Aug Sep Oct Nov Dec Jan Feb Mar Apr May Jun
                lunchStoreList = new int[]{2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1};
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

        if (tempMonth >= lunchStoreListStart) {
            tempSchedulePosition = tempMonth - lunchStoreListStart;
        } else {
            tempSchedulePosition = tempMonth + (12 - lunchStoreListStart);
        }

        Log.i("lunchwaveallow", Integer.toString(lunchStoreList[tempSchedulePosition]));

        if (labLunch) {
            return (lunchStoreList[tempSchedulePosition] + 10);
        } else {
            return lunchStoreList[tempSchedulePosition];
        }
    }


    int PeriodNumber(int[][] inputPeriodTimes, boolean affectPassingTime) { //Finds the current period number of the day, and determines if it is passing time or if there is no School

        if (noSchool) return -1;

        if (affectPassingTime) passingTime = false;

        int i = 0; //array position
        //KEY: 0 start times hour, 1 start times min, 2 end times hour, 3 end times minute

        while (true) { //runs through all times and counts the period number of the day
            if ((currentHour > inputPeriodTimes[2][i])) {
                i++;
                Log.i("1", Integer.toString(i));
            } else if ((currentHour == inputPeriodTimes[2][i]) && (currentMinute >= inputPeriodTimes[3][i])) {
                i++;
                Log.i("2", Integer.toString(i));
            } else if ((currentHour == inputPeriodTimes[0][i]) && (currentMinute >= inputPeriodTimes[1][i])) {
                Log.i("3", Integer.toString(i));
                break;
            } else if (currentHour > inputPeriodTimes[0][i]) {
                Log.i("4", Integer.toString(i));
                break;
            } else { //If not found, but not no School, it must be passing time
                Log.i("5", Integer.toString(i));
                if (affectPassingTime) {
                    passingTime = true;
                    return i;
                } else {
                    return -1;
                }
            }
        }
        return i;//returns period number array position
    }

    void BeforeOrAfterSchoolCheck(int[][] inputPeriodTimes) { //Checks if the current time is before or after school
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
        ArrayList<String> periodInfo = new ArrayList<>();

        int greenHighlightPosition = -1;//Position of Green Highlight(If -1, will not display)
        int redHighlightPosition = -1;//Position of Red Highlight(If -1, will not display)
        int tempPositionOffset = 0; //Offset of Positions Due to Addition of Lunch Wave Views
        int tempLunchWaveOffset = 0;

        int tempArrayLength = scheduleFormat.length;

        for (int i = 0; i < tempArrayLength; i++) {

            String tempPeriodNameString = sharedPref.getString("key_schedule_period_" + Integer.toString(scheduleFormat[i]) + "_name", "Period " + Integer.toString(scheduleFormat[i]));

            if (i == lunchPeriodPosition) {
                String tempPref = sharedPref.getString("key_schedule_period_" + scheduleFormat[i] + "_type", "Free or Not Applicable");
                String tempPrefInfo = sharedPref.getString("key_schedule_period_" + scheduleFormat[i] + "_info", " ");

                Log.i("tempPrefInfo", tempPrefInfo);

                if (tempPrefInfo.equals("No Info")) tempPrefInfo = " ";

                int tempAllowedLunchWave = findAllowedLunchWave(tempPref, currentMonth);
                Log.i("tempallowedlunchwave", Integer.toString(tempAllowedLunchWave));
                Log.i("tempPref", tempPref);
                switch (tempAllowedLunchWave) {
                    case 0: //Free or Not Applicable
                    {

                        periodNumbers.add(Integer.toString(scheduleFormat[i]));
                        periodNames.add(tempPeriodNameString);
                        lunchWave.add("All");
                        periodInfo.add(tempPrefInfo);

                        int tempStartHour = (inputLunchWaveTimes[0][0]);
                        int tempStartMinute = inputLunchWaveTimes[1][0];
                        int tempEndHour = (inputLunchWaveTimes[2][2]);
                        int tempEndMinute = inputLunchWaveTimes[3][2];

                        periodStart.add(DisplayWithPlaceHolder(ToTwelveHour(tempStartHour), tempStartMinute));
                        periodEnd.add(DisplayWithPlaceHolder(ToTwelveHour(tempEndHour), tempEndMinute));

                    }

                    break;
                    case 1: //First Lunch
                    {

                        String tempPeriodDisplay = Integer.toString(scheduleFormat[i]);

                        //First View (Lunch)
                        periodNumbers.add(tempPeriodDisplay);
                        lunchWave.add("1");
                        periodNames.add("Lunch");
                        periodInfo.add(" ");
                        //
                        int tempStartHour = (inputLunchWaveTimes[0][0]);
                        int tempEndHour = (inputLunchWaveTimes[2][0]);
                        int tempStartMinute = inputLunchWaveTimes[1][0];
                        int tempEndMinute = inputLunchWaveTimes[3][0];
                        //
                        periodStart.add(DisplayWithPlaceHolder(ToTwelveHour(tempStartHour), tempStartMinute));
                        periodEnd.add(DisplayWithPlaceHolder(ToTwelveHour(tempEndHour), tempEndMinute));


                        //Second View (Class)
                        periodNumbers.add(tempPeriodDisplay);
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);
                        periodInfo.add(tempPrefInfo);
                        //
                        tempStartHour = (inputLunchWaveTimes[0][1]);
                        tempEndHour = (inputLunchWaveTimes[2][2]);
                        tempStartMinute = inputLunchWaveTimes[1][1];
                        tempEndMinute = inputLunchWaveTimes[3][2];
                        //
                        periodStart.add(DisplayWithPlaceHolder(ToTwelveHour(tempStartHour), tempStartMinute));
                        periodEnd.add(DisplayWithPlaceHolder(ToTwelveHour(tempEndHour), tempEndMinute));
                        //
                        if (InsideTimes(tempStartHour, tempStartMinute, tempEndHour, tempEndMinute))
                            tempLunchWaveOffset = 1;

                        tempPositionOffset += 1;

                    }

                    break;
                    case 2: //Second Lunch
                    {

                        String tempPeriodDisplay = Integer.toString(scheduleFormat[i]);

                        //First View (Class Part 1)
                        periodNumbers.add(tempPeriodDisplay);
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);
                        periodInfo.add(tempPrefInfo);

                        //Second View (Lunch)
                        periodNumbers.add(tempPeriodDisplay);
                        lunchWave.add("2");
                        periodNames.add("Lunch");
                        periodInfo.add(" ");

                        //Third View
                        periodNumbers.add(tempPeriodDisplay);
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);
                        periodInfo.add(tempPrefInfo);

                        //For All Three Views
                        for (int j = 0; j < inputLunchWaveTimes[0].length; j++) {
                            int tempStartHour = (inputLunchWaveTimes[0][j]);
                            int tempEndHour = (inputLunchWaveTimes[2][j]);
                            int tempStartMinute = inputLunchWaveTimes[1][j];
                            int tempEndMinute = inputLunchWaveTimes[3][j];

                            periodStart.add(DisplayWithPlaceHolder(ToTwelveHour(tempStartHour), tempStartMinute));
                            periodEnd.add(DisplayWithPlaceHolder(ToTwelveHour(tempEndHour), tempEndMinute));

                            if (InsideTimes(tempStartHour, tempStartMinute, tempEndHour, tempEndMinute))
                                tempLunchWaveOffset = j;
                        }

                        tempPositionOffset += 2;
                    }

                    break;
                    case 3: //Third Lunch
                    {

                        String tempPeriodDisplay = Integer.toString(scheduleFormat[i]);

                        //First View (Class Period)
                        periodNumbers.add(tempPeriodDisplay);
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);
                        periodInfo.add(tempPrefInfo);
                        //
                        int tempStartHour = (inputLunchWaveTimes[0][0]);
                        int tempEndHour = (inputLunchWaveTimes[2][1]);
                        int tempStartMinute = inputLunchWaveTimes[1][0];
                        int tempEndMinute = inputLunchWaveTimes[3][1];
                        //
                        periodStart.add(DisplayWithPlaceHolder(ToTwelveHour(tempStartHour), tempStartMinute));
                        periodEnd.add(DisplayWithPlaceHolder(ToTwelveHour(tempEndHour), tempEndMinute));

                        //Second View (Lunch)
                        periodNumbers.add(tempPeriodDisplay);
                        lunchWave.add("3");
                        periodNames.add("Lunch");
                        periodInfo.add(" ");
                        //
                        tempStartHour = (inputLunchWaveTimes[0][2]);
                        tempEndHour = (inputLunchWaveTimes[2][2]);
                        tempStartMinute = inputLunchWaveTimes[1][2];
                        tempEndMinute = inputLunchWaveTimes[3][2];
                        //
                        periodStart.add(DisplayWithPlaceHolder(ToTwelveHour(tempStartHour), tempStartMinute));
                        periodEnd.add(DisplayWithPlaceHolder(ToTwelveHour(tempEndHour), tempEndMinute));
                        //
                        if (InsideTimes(tempStartHour, tempStartMinute, tempEndHour, tempEndMinute))
                            tempLunchWaveOffset = 1;

                        tempPositionOffset += 1;

                    }

                    break;
                    case 11: //First Lab Lunch
                    {

                        String tempPeriodDisplay = Integer.toString(scheduleFormat[i]);

                        //First View (Lab Lunch)
                        periodNumbers.add(tempPeriodDisplay);
                        lunchWave.add("L1");
                        periodNames.add("Lab Lunch");
                        periodInfo.add(" ");
                        //
                        int tempStartHour = inputLunchWaveTimes[0][0];
                        int tempEndHour = inputLunchWaveTimes[0][0];
                        int tempStartMinute = inputLunchWaveTimes[1][0];
                        int tempEndMinute;
                        //
                        if ((tempStartMinute + labLunchLength) > 60) {
                            tempEndHour = tempEndHour + 1;
                            tempEndMinute = (tempStartMinute + labLunchLength) - 60;
                        } else {
                            tempEndMinute = tempStartMinute + labLunchLength;
                        }
                        //
                        periodStart.add(DisplayWithPlaceHolder(ToTwelveHour(tempStartHour), tempStartMinute));
                        periodEnd.add(DisplayWithPlaceHolder(ToTwelveHour(tempEndHour), tempEndMinute));

                        //Second View (Class)
                        periodNumbers.add(tempPeriodDisplay);
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);
                        periodInfo.add(tempPrefInfo);
                        //
                        tempStartHour = tempEndHour;
                        tempEndHour = inputLunchWaveTimes[2][2];
                        tempStartMinute = tempEndMinute;
                        tempEndMinute = inputLunchWaveTimes[3][2];
                        //
                        periodStart.add(DisplayWithPlaceHolder(ToTwelveHour(tempStartHour), tempStartMinute));
                        periodEnd.add(DisplayWithPlaceHolder(ToTwelveHour(tempEndHour), tempEndMinute));
                        //
                        if (InsideTimes(tempStartHour, tempStartMinute, tempEndHour, tempEndMinute))
                            tempLunchWaveOffset = 1;

                        tempPositionOffset += 1;

                    }

                    break;
                    case 12: //Second Lab Lunch
                        //This will never happen unless the policy somehow changes
                        //If it gets to here, you done screwed up inputting lunch waves
                        break;
                    case 13: //Third Lab Lunch
                    {

                        String tempPeriodDisplay = Integer.toString(scheduleFormat[i]);

                        //First View (Class)
                        periodNumbers.add(tempPeriodDisplay);
                        lunchWave.add(" ");
                        periodNames.add(tempPeriodNameString);
                        periodInfo.add(tempPrefInfo);
                        //
                        int tempStartHour = inputLunchWaveTimes[0][0];
                        int tempEndHour = inputLunchWaveTimes[2][1];
                        int tempStartMinute = inputLunchWaveTimes[1][0];
                        int tempEndMinute;
                        //
                        if ((tempStartMinute + labLunchLength) > 60) {
                            tempEndHour = tempEndHour + 1;
                            tempEndMinute = (tempStartMinute + labLunchLength) - 60;
                        } else {
                            tempEndMinute = tempStartMinute + labLunchLength;
                        }
                        //
                        periodStart.add(DisplayWithPlaceHolder(ToTwelveHour(tempStartHour), tempStartMinute));
                        periodEnd.add(DisplayWithPlaceHolder(ToTwelveHour(tempEndHour), tempEndMinute));

                        //Second View (Lunch)
                        periodNumbers.add(tempPeriodDisplay);
                        lunchWave.add("L3");
                        periodNames.add("Lab Lunch");
                        periodInfo.add(" ");
                        //
                        //
                        tempStartHour = tempEndHour;
                        tempEndHour = inputLunchWaveTimes[2][2];
                        tempStartMinute = tempEndMinute;
                        tempEndMinute = inputLunchWaveTimes[3][2];
                        //
                        periodStart.add(DisplayWithPlaceHolder(ToTwelveHour(tempStartHour), tempStartMinute));
                        periodEnd.add(DisplayWithPlaceHolder(ToTwelveHour(tempEndHour), tempEndMinute));
                        //
                        if (InsideTimes(tempStartHour, tempStartMinute, tempEndHour, tempEndMinute))
                            tempLunchWaveOffset = 1;

                        tempPositionOffset += 1;

                    }

                    break;

                    default: //catch errors, should never reach here

                        WarningPopup("Invalid Lunch Wave Type Returned", "Information may be incorrect. Contact Developer. Code 7");
                        periodNumbers.add(" ");
                        lunchWave.add(" ");
                        periodNames.add("LUNCH PERIOD ERROR");
                        periodInfo.add("Inccorrect Information Displayed");

                }

            } else {//List Fill for Normal Periods

                String tempPrefString = sharedPref.getString("key_schedule_period_" + Integer.toString(scheduleFormat[i]) + "_info", " ");
                if (tempPrefString.equals("No Info")) tempPrefString = " ";

                periodNumbers.add(Integer.toString(scheduleFormat[i]));
                periodNames.add(tempPeriodNameString);
                lunchWave.add(" ");
                periodInfo.add(tempPrefString);

                int tempStartHour = inputPeriodTimes[0][i];
                int tempEndHour = inputPeriodTimes[2][i];

                periodStart.add(DisplayWithPlaceHolder(ToTwelveHour(tempStartHour), inputPeriodTimes[1][i]));
                periodEnd.add(DisplayWithPlaceHolder(ToTwelveHour(tempEndHour), inputPeriodTimes[3][i]));

            }


        }

        Log.i("periodnumbers", periodNumbers.toString());
        Log.i("periodnames", periodNames.toString());
        Log.i("periodstart", periodStart.toString());
        Log.i("periodend", periodEnd.toString());
        Log.i("periodinfo", periodInfo.toString());


        // set up the RecyclerView
        if (!((periodNames.size() == periodNumbers.size()) && (periodStart.size() == periodNumbers.size()) && (periodEnd.size() == periodNumbers.size()) && (periodEnd.size() == lunchWave.size()) && (periodEnd.size() == periodInfo.size()))) {
            //Throw Error Message
            WarningPopup("Number of Periods Mismatch", "Likely due to incorrect schedule server input. Please contact the developer. Code 3.");
            //Clears all array lists
            periodNumbers.clear();
            periodNames.clear();
            periodStart.clear();
            periodEnd.clear();
            periodInfo.clear();
            //Sets all array lists
            periodNumbers.add("420");
            periodNames.add("Bad Error");
            periodStart.add("WTF");
            periodEnd.add("Goddamit");
            periodInfo.add("For some reason the array lists that populate this are mismatched");
        }

        TextView periodNumberDisplay = findViewById(R.id.PeriodDisplay);
        boolean tempPassingTime = false;

        if (!((noSchool) || (beforeSchool) || (afterSchool))) {
            //Log.i("currentperiodnum1",Integer.toString(currentPeriodNumber));
            //Log.i("lunchwavepos",Integer.toString(PeriodNumber(lunchWaveTimes,false)));

            if ((currentPeriodNumber) < lunchPeriodPosition) {

                greenHighlightPosition = currentPeriodNumber;

            } else if ((currentPeriodNumber) == lunchPeriodPosition) {
                //Log.i("periodnumberlunch", Integer.toString(PeriodNumber(lunchWaveTimes,false)));

                if (passingTime) { //To handle 10:41 case

                    greenHighlightPosition = lunchPeriodPosition;

                } else {

                    greenHighlightPosition = lunchPeriodPosition + tempLunchWaveOffset;

                    if (PeriodNumber(lunchWaveTimes, false) == -1) { ///FIX THIS TO NOT TRIGGER DURING PASSING TIME OVERWRITTEN BY LONG PERIODS
                        tempPassingTime = true;
                        greenHighlightPosition += 1;
                    }
                }


            } else {
                greenHighlightPosition = tempPositionOffset + currentPeriodNumber;
            }

            if (periodNames.get(greenHighlightPosition).equals("Lunch")) {
                periodNumberDisplay.setText("L");
            } else {
                periodNumberDisplay.setText(periodNumbers.get(greenHighlightPosition));
            }

            if (passingTime || tempPassingTime) {
                redHighlightPosition = greenHighlightPosition - 1;
                periodNumberDisplay.setText("P");
            }
        }

        adapter = new MyRecyclerViewAdapter(this, periodNumbers, periodNames, periodStart, periodEnd, lunchWave, periodInfo, greenHighlightPosition, redHighlightPosition);
        recyclerView.setAdapter(adapter);

        if ((greenHighlightPosition != -1) && (firstTime))
            recyclerView.scrollToPosition(greenHighlightPosition); //scrolls to current position here so visible

    }

    int ToTwelveHour(int input) { //Converts 24 hour time to 12 hour time for display purposes ONLY

        if (input > 12) return input - 12;

        return input;
    }

    String DisplayWithPlaceHolder(int inputHour, int inputMinute) {//Displays Time in a string with a zero placeholder for numbers under 10

        if (inputMinute < 10)
            return Integer.toString(inputHour) + ":0" + Integer.toString(inputMinute);

        return Integer.toString(inputHour) + ":" + Integer.toString(inputMinute);
    }

    boolean InsideTimes(int _startHour, int _startMinute, int _endHour, int _endMinute) {

        if ((currentHour > _endHour)) {
            return false;
        } else if ((currentHour == _endHour) && (currentMinute >= _endMinute)) {
            return false;
        } else if ((currentHour == _startHour) && (currentMinute >= _startMinute)) {
            return true;
        } else if (currentHour > _startHour) {
            return true;
        }

        return false;
    }

    void FindTimeUntilEndNormal(int finderinputPeriodTimes[][]) { //Finds the time until the end of the period

        int timeUntilEndHour = 0;
        int timeUntilEndMinute;
        int totalTimeHour = 0;
        int totalTimeMinute;
        int PeriodArrayPosition = currentPeriodNumber;
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

                progressBarTextTime = DisplayWithPlaceHolder(tempDisplayHour, tempoftempLeftMinutes);
                break;

            }
        }
    }

    void FindTimeUntilEndPassingTime(int[][] finderInputPeriodTimes, int tempPosition) { //Finds time until end of passing time

        int timeUntilEndHour = 0;
        int timeUntilEndMinute;
        int totalTimeHour = 0;
        int totalTimeMinute;
        Log.i("periodposition1", Integer.toString(tempPosition));
        int tempCurrentHour = currentHour;


        while (true) {
            if (tempCurrentHour < finderInputPeriodTimes[0][tempPosition]) {
                timeUntilEndHour++;
                tempCurrentHour++;
            } else {
                timeUntilEndMinute = ((finderInputPeriodTimes[1][tempPosition]) - currentMinute);
                break;
            }
        }

        tempCurrentHour = finderInputPeriodTimes[2][tempPosition - 1];
        while (true) {
            if (tempCurrentHour < finderInputPeriodTimes[0][tempPosition]) {
                totalTimeHour++;
                tempCurrentHour++;
            } else {
                totalTimeMinute = ((finderInputPeriodTimes[3][tempPosition - 1]) - (finderInputPeriodTimes[1][tempPosition]));
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
                progressBarTextTime = DisplayWithPlaceHolder(tempDisplayHour, tempoftempLeftMinutes);
                break;
            }
        }
    }

    void FindTimeUntilEndOfDay(int[][] finderInputPeriodTimes) { //Finds time until end of day

        int tempCurrentHour = currentHour;
        int tempTimeUntilHour = 0;
        int tempTimeUntilMinute;
        int tempTotalTimeHour = 0;
        int tempTotalTimeMinute;
        int tempArrayLength = (finderInputPeriodTimes[0].length) - 1;

        while (true) {
            if (tempCurrentHour < finderInputPeriodTimes[2][tempArrayLength]) {
                tempTimeUntilHour++;
                tempCurrentHour++;
            } else {
                tempTimeUntilMinute = ((finderInputPeriodTimes[3][tempArrayLength]) - currentMinute);
                break;
            }
        }

        tempCurrentHour = finderInputPeriodTimes[0][0];

        while (true) {
            if (tempCurrentHour < finderInputPeriodTimes[2][tempArrayLength]) {
                tempTotalTimeHour++;
                tempCurrentHour++;
            } else {
                tempTotalTimeMinute = ((finderInputPeriodTimes[3][tempArrayLength]) - (finderInputPeriodTimes[1][0]));
                break;
            }
        }

        float tempTotalMinutes = Math.abs((tempTotalTimeHour * 60) + tempTotalTimeMinute);
        float tempLeftMinutes = Math.abs((tempTimeUntilHour * 60) + tempTimeUntilMinute);
        progressForOverallBar = 100 - Math.round((tempLeftMinutes / tempTotalMinutes) * 100);

    }

    void FindTimeUntilEndLunchWave(int finderInputPeriodTimes[][], int tempPosition) { //Finds the time until the end of the period

        int timeUntilEndHour = 0;
        int timeUntilEndMinute;
        int totalTimeHour = 0;
        int totalTimeMinute;

        int tempStartHour = 0;
        int tempStartMinute = 0;
        int tempEndHour = 0;
        int tempEndMinute = 0;

        int PeriodArrayPosition = currentPeriodNumber;
        int currentPeriodIDNumber = scheduleFormat[PeriodArrayPosition];
        String PeriodType;

        Log.i("periodID", Integer.toString(currentPeriodIDNumber));
        PeriodType = sharedPref.getString(("key_schedule_period_" + Integer.toString(currentPeriodIDNumber) + "_type"), "Free or Not Applicable");
        Log.i("periodTypeDef", PeriodType);
        switch (findAllowedLunchWave(PeriodType, currentMonth)) {
            case 0:

                tempStartHour = finderInputPeriodTimes[0][0];
                tempStartMinute = finderInputPeriodTimes[1][0];
                tempEndHour = finderInputPeriodTimes[2][2];
                tempEndMinute = finderInputPeriodTimes[3][2];

                break;
            case 1:

                switch (tempPosition + 1) {
                    case 1:
                        tempStartHour = finderInputPeriodTimes[0][0];
                        tempStartMinute = finderInputPeriodTimes[1][0];
                        tempEndHour = finderInputPeriodTimes[2][0];
                        tempEndMinute = finderInputPeriodTimes[3][0];
                        break;
                    case 2:
                        tempStartHour = finderInputPeriodTimes[0][1];
                        tempStartMinute = finderInputPeriodTimes[1][1];
                        tempEndHour = finderInputPeriodTimes[2][2];
                        tempEndMinute = finderInputPeriodTimes[3][2];

                        break;
                    case 3:
                        tempStartHour = finderInputPeriodTimes[0][1];
                        tempStartMinute = finderInputPeriodTimes[1][1];
                        tempEndHour = finderInputPeriodTimes[2][2];
                        tempEndMinute = finderInputPeriodTimes[3][2];
                        break;
                }

                break;
            case 2:

                tempStartHour = finderInputPeriodTimes[0][tempPosition];
                tempStartMinute = finderInputPeriodTimes[1][tempPosition];
                tempEndHour = finderInputPeriodTimes[2][tempPosition];
                tempEndMinute = finderInputPeriodTimes[3][tempPosition];

                break;
            case 3:

                switch (tempPosition + 1) {
                    case 1:

                        tempStartHour = finderInputPeriodTimes[0][0];
                        tempStartMinute = finderInputPeriodTimes[1][0];
                        tempEndHour = finderInputPeriodTimes[2][1];
                        tempEndMinute = finderInputPeriodTimes[3][1];

                        break;
                    case 2:

                        tempStartHour = finderInputPeriodTimes[0][0];
                        tempStartMinute = finderInputPeriodTimes[1][0];
                        tempEndHour = finderInputPeriodTimes[2][1];
                        tempEndMinute = finderInputPeriodTimes[3][1];

                        break;
                    case 3:

                        tempStartHour = finderInputPeriodTimes[0][2];
                        tempStartMinute = finderInputPeriodTimes[1][2];
                        tempEndHour = finderInputPeriodTimes[2][2];
                        tempEndMinute = finderInputPeriodTimes[3][2];

                        break;
                }

                break;
            case 11:

                switch (tempPosition + 1) {
                    case 1:

                        tempStartHour = finderInputPeriodTimes[0][0];
                        tempStartMinute = finderInputPeriodTimes[1][0];
                        tempEndHour = finderInputPeriodTimes[0][0];
                        //tempEndMinute = finderInputPeriodTimes[1][0];

                        if ((tempStartMinute + labLunchLength) > 60) {
                            tempEndHour++;
                            tempEndMinute = (tempStartMinute + labLunchLength) - 60;
                        } else {
                            tempEndMinute = tempStartMinute + labLunchLength;
                        }

                        if (!(((currentHour == tempEndHour) && (currentMinute < tempEndMinute)) || (currentHour < tempEndHour))) {
                            tempStartHour = tempEndHour;
                            tempStartMinute = tempEndMinute;
                            tempEndHour = finderInputPeriodTimes[2][2];
                            tempEndMinute = finderInputPeriodTimes[3][2];
                        }

                        break;
                    case 2:
                        tempStartHour = finderInputPeriodTimes[0][0];
                        tempStartMinute = finderInputPeriodTimes[1][0];
                        tempEndHour = finderInputPeriodTimes[2][2];
                        tempEndMinute = finderInputPeriodTimes[3][2];

                        if ((tempStartMinute + labLunchLength) > 60) {
                            tempStartHour++;
                            tempStartMinute = (tempStartMinute + labLunchLength) - 60;
                        } else {
                            tempStartMinute = tempStartMinute + labLunchLength;
                        }


                        break;
                    case 3:
                        tempStartHour = finderInputPeriodTimes[0][0];
                        tempStartMinute = finderInputPeriodTimes[1][0];
                        tempEndHour = finderInputPeriodTimes[2][2];
                        tempEndMinute = finderInputPeriodTimes[3][2];

                        if ((tempStartMinute + labLunchLength) > 60) {
                            tempStartHour++;
                            tempStartMinute = (tempStartMinute + labLunchLength) - 60;
                        } else {
                            tempStartMinute = tempStartMinute + labLunchLength;
                        }
                        break;
                }


                break;
            case 13:

                switch (tempPosition + 1) {
                    case 1:

                        tempStartHour = finderInputPeriodTimes[0][0];
                        tempStartMinute = finderInputPeriodTimes[1][0];
                        tempEndHour = finderInputPeriodTimes[2][2];
                        tempEndMinute = finderInputPeriodTimes[3][2];

                        if ((tempEndMinute - labLunchLength) < 0) {
                            tempEndHour = tempEndHour - 1;
                            tempEndMinute = 60 + (tempEndMinute - labLunchLength);
                        } else {
                            tempEndMinute = tempEndMinute - labLunchLength;
                        }

                        break;
                    case 2:

                        tempStartHour = finderInputPeriodTimes[0][0];
                        tempStartMinute = finderInputPeriodTimes[1][0];
                        tempEndHour = finderInputPeriodTimes[2][2];
                        tempEndMinute = finderInputPeriodTimes[3][2];

                        if ((tempEndMinute - labLunchLength) < 0) {
                            tempEndHour = tempEndHour - 1;
                            tempEndMinute = 60 + (tempEndMinute - labLunchLength);
                        } else {
                            tempEndMinute = tempEndMinute - labLunchLength;
                        }

                        break;
                    case 3:

                        tempStartHour = finderInputPeriodTimes[0][0];
                        tempStartMinute = finderInputPeriodTimes[1][0];
                        tempEndHour = finderInputPeriodTimes[2][2];
                        tempEndMinute = finderInputPeriodTimes[3][2];

                        if ((tempEndMinute - labLunchLength) < 0) {
                            tempEndHour = tempEndHour - 1;
                            tempEndMinute = 60 + (tempEndMinute - labLunchLength);
                        } else {
                            tempEndMinute = tempEndMinute - labLunchLength;
                        }

                        Log.i("tempendhour", Integer.toString(tempEndHour));
                        Log.i("tempendMinute", Integer.toString(tempEndMinute));

                        if (((currentHour == tempEndHour) && (currentMinute > tempEndMinute)) || (currentHour > tempEndHour)) {
                            tempStartHour = tempEndHour;
                            tempStartMinute = tempEndMinute;
                            tempEndHour = finderInputPeriodTimes[2][2];
                            tempEndMinute = finderInputPeriodTimes[3][2];
                            Log.i("tempstarthour", Integer.toString(tempStartHour));
                            Log.i("tempstartminute", Integer.toString(tempStartMinute));
                            Log.i("tempendhour", Integer.toString(tempEndHour));
                            Log.i("tempendMinute", Integer.toString(tempStartHour));
                        }

                        break;
                    default: //catch errors here
                        WarningPopup("Lunch Wave for Calculating Time Not Found", "To fix this, clear the type of class and enter Free or Not Applicable instead. Please contact the developer. Code 4");
                }

                break;
            default: //catch errors here

                WarningPopup("Allowed Lunch Wave Not Found", "To fix this, clear the type of class and enter Free or Not Applicable instead. Please contact the developer. Code 5");

        }

        int tempCurrentHour = currentHour;

        while (true) {
            if (tempCurrentHour < tempEndHour) {
                timeUntilEndHour++;
                tempCurrentHour++;
            } else {
                timeUntilEndMinute = ((tempEndMinute) - currentMinute);
                break;
            }
        }

        tempCurrentHour = tempStartHour;

        while (true) {
            if (tempCurrentHour < tempEndHour) {
                totalTimeHour++;
                tempCurrentHour++;
            } else {
                totalTimeMinute = ((tempEndMinute) - (tempStartMinute));
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
                progressBarTextTime = DisplayWithPlaceHolder(tempDisplayHour, tempoftempLeftMinutes);
                break;
            }
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
    }


    void NoSchoolProcedures() { //Changes values on UI thread to reflect no school state

        progressBar = findViewById(R.id.progressBar);
        overallProgressBar = findViewById(R.id.OverallDayProgressBar);
        progressBar.setProgress(100);
        overallProgressBar.setProgress(100);
        TextView ProgressBarTextTime = findViewById(R.id.ProgressBarTextTime);
        ProgressBarTextTime.setTextSize(30);
        ProgressBarTextTime.setText("NO");
        TextView ProgressBarTextPercent = findViewById(R.id.ProgressBarTextPercent);
        ProgressBarTextPercent.setText("NO");
        TextView remain1 = findViewById(R.id.remain1);
        remain1.setText("School");
        TextView remain2 = findViewById(R.id.remain2);
        remain2.setText("School");


    }

    void BeforeSchoolProcedures() {

    }

    void AfterSchoolProcedures() {

    }

    void RefreshReset() {
        //noSchool = false;
        passingTime = false;
        beforeSchool = false;
        afterSchool = false;
        labLunch = false;
    }

    void RefreshTime() {
        Calendar cal = Calendar.getInstance();

        currentYear = cal.get(Calendar.YEAR);

        //int currentDayDay = cal.get(Calendar.DAY_OF_WEEK);

        currentMonth = (cal.get(Calendar.MONTH) + 1);
        //int currentMonth = 12;

        currentDayNum = cal.get(Calendar.DAY_OF_MONTH);
        //int currentDayNum = 5;

        currentHour = cal.get(Calendar.HOUR_OF_DAY);
        //int currentHour = 13;

        currentMinute = cal.get(Calendar.MINUTE);
    }

    void WarningPopup(String inputWarnTitle, String inputWarnText) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainPage.this).create();
        alertDialog.setTitle(inputWarnTitle);
        alertDialog.setMessage(inputWarnText);
        halt = true;

            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            System.exit(0);
                        }
                    });

        alertDialog.show();
    }
}
