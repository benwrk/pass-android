package co.bwsc.pass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import co.bwsc.pass.R;
import co.bwsc.pass.service.PassService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ensureServiceStarted();
        finish();
    }

    private void ensureServiceStarted() {
        startService(new Intent(this, PassService.class));
    }




}
