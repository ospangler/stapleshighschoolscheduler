package owenspangler.stapleshighschoolscheduler;
///
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
import java.util.Calendar;
///
///Portions of Below Code is derived from code made by Abhishek Panwar, 2017///
///The original code can be found at https://github.com/panwarabhishek345/Receive-JSON-Data///

class JSONfetcher extends AsyncTask<Void,Void,Void> {
    String data = "";
    ////
    String jsonDayLetter = "";
    int jsonMonth = -1;
    int jsonDay = -1;
    int[] jsonNewScheduleFormat;
    int[][] jsonStartTimes;
    int[][] jsonEndTimes;
    String jsonNotice;


    // String temperature = JSONObject(html).getString("name");///////FIX AND ADD HTML CODE
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




    ///


    @Override
    protected Void doInBackground(Void...voids) {
        RetrieveJson();
        GetInfoFromJSON(data);


        return null;///redundant
    }
void RetrieveJson(){
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
}
    void GetInfoFromJSON(String inputdata){
        try {
            JSONObject JO = new JSONObject(inputdata);
            jsonDayLetter= JO.getString("dayletter");
            jsonMonth = JO.getInt("month");
            jsonDay = JO.getInt("day");
            ////
            // Retrieve number array from JSON object "newscheduleformat".
            JSONArray NSFarray = JO.optJSONArray("newscheduleformat");
            if (NSFarray == null) { /*ENTER SOME ERROR CODE HERE*/ }
            jsonNewScheduleFormat = new int[NSFarray.length()];
            for (int i = 0; i < NSFarray.length(); ++i) {
                jsonNewScheduleFormat[i] = NSFarray.optInt(i);
            }
            ///
            int[] tempJsonStartTimesHour = getArrayFromJSON("startimeshour");
            int[] tempJsonStartTimesMinute = getArrayFromJSON("starttimesminute");
            int[] tempJsonEndTimesHour = getArrayFromJSON("endtimeshour");
            int[] tempJsonEndTimesMinute = getArrayFromJSON("endtimesminute");

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
    int[] getArrayFromJSON(String jsonID) {
        try {
            JSONObject tempJO = new JSONObject(data);
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
        //return null;
    }
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





    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
//set text here
        //MainActivity.data.setText(this.dataParsed);

    }
}

