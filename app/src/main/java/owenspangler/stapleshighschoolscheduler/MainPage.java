package owenspangler.stapleshighschoolscheduler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Arrays;


public class MainPage extends AppCompatActivity {
    //Button click;
    //public  static TextView data;
    //MyAsyncTask asyncTask = new MyAsyncTask();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        JSONfetcher process = new JSONfetcher();
        process.execute();
        //String tempstring = jsonDayLetter;
        //Arrays.toString(process.getNewscheduleformat());
        TextView textView1 = findViewById(R.id.textView1);
        //textView1.setText(1);
////

    }

    //protected void onPostExecute()

}
