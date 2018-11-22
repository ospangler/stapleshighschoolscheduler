package owenspangler.stapleshighschoolscheduler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.Nullable;

//splash screen tutorial https://medium.com/@ssaurel/create-a-splash-screen-on-android-the-right-way-93d6fb444857

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.splash_screen);
        Intent intent = new Intent(this, MainPage.class);
        startActivity(intent);
        finish();
    }

}
