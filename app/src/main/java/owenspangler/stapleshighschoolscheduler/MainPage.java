package owenspangler.stapleshighschoolscheduler;

import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        int temp = PeriodNumber();
        int[] tempsced = ScheduleFormat('d');
        int periodforreals = tempsced[temp-1];
        String tempstring = Integer.toString(periodforreals);
        TextView textView1 = findViewById(R.id.textView1);
        textView1.setText(tempstring);

    }
//Public Vars
    char dayLetter = 'z';
    //TimeZone tz;
    Calendar cal = Calendar.getInstance();
    int currentHour = cal.get(Calendar.HOUR_OF_DAY);
    //int currentHour = 8;
    int currentMinute = cal.get(Calendar.MINUTE);
    //int currentMinute = 15;
    int currentSecond = cal.get(Calendar.SECOND);
    //
    /*
    dayLetter = 'z';
    cal = Calendar.getInstance();
    currentHour = cal.get(Calendar.HOUR_OF_DAY);
    //int currentHour = 8;
    currentMinute = cal.get(Calendar.MINUTE);
    //int currentMinute = 15;
    currentSecond = cal.get(Calendar.SECOND);
    int temp = PeriodNumber();
    */




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
            return null;
        }
    }

    boolean NormalDay(){// REPLACE WITH A SYNC ADAPTER TO DO THIS REMOTELY IN THE FUTURE. HARDCODED DATES
        int[] scheduleChanges = {}; //format MDDYY
        //int currentMonth = Calendar.get(Calendar.MONTH);
        //for( i = 0 ; i<scheduleChanges.length(); i++){
        return true;
    }
    int PeriodNumber(){
        int i = 0; //array position
        boolean passingTime = false;

        if((currentHour<7)||((currentHour==7)&&(currentMinute<30))||(currentHour>14)||((currentHour == 14)&&(currentMinute>=15))){
            return -1;
        }

        int[][] periodTimes = //CHANGE BELOW TIMES WHEN SCHEDULE CHANGES
                {{ 7, 8, 9,10,12,13},//START HOUR
                 {30,25,50,45,30,25},//START MINUTE

                 { 8, 9,10,12,13,14},//END HOUR
                 {25,45,40,25,20,15}};//END MINUTE

        while(true){
            if ((currentHour > periodTimes[2][i])){
                i++;
            }else if ((currentHour == periodTimes[2][i])&&(currentMinute > periodTimes[3][i])){
                i++;
            }else if((currentHour == periodTimes[0][i])&&(currentMinute > periodTimes[1][i])) {
                break;
            }else if(currentHour > periodTimes[0][i]){
                break;
            }else{
                passingTime = true;
                break;
            }
        }

        if(passingTime){
            return -1;
        }
        else{
            return (i+1);

        }
    }
}

