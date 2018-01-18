package com.openmdmremote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import com.openmdmremote.service.BackgroundService;
import com.openmdmremote.ui.main.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

public class PrincipalActivity extends Activity {

    Timer timer;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_principal);
        Log.i("PrincipalActivity", "onCreate");
        context=this;
        Intent intent = new Intent(this, BackgroundService.class);
        try{stopService(intent);}catch (Exception e){e.printStackTrace();}
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (CheckRootAccess.checkRootAccess()) {
                    timer.cancel();
                    cancel();
                    Log.i("Root", "concedido");
                    Intent i = new Intent(context, MainActivity.class);
                    context.startActivity(i);

                } else {
                    Log.i("Root", "No concedido");


                }
            }


        }, 0, 3000);


    }

}
