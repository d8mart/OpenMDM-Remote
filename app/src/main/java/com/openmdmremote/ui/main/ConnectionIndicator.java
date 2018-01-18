package com.openmdmremote.ui.main;

import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.openmdmremote.R;

public class ConnectionIndicator {

    TextView networkIndicatorText;
    ImageView networkIndicator;
    final Handler handler;

    private enum States {
        OFFLINE, CONNECTING, ONLINE
    }

    private States lastState;

    public ConnectionIndicator(View header) {
        networkIndicator = (ImageView) header.findViewById(R.id.network_indicator);
        networkIndicatorText = (TextView) header.findViewById(R.id.network_indicator_txt);
        lastState = States.OFFLINE;
        handler = new Handler();
    }

    public void setConnecting() {
        lastState = States.CONNECTING;
        handler.removeCallbacksAndMessages(null);
        updateIndicator();
    }

    public void setOffile() {
        lastState = States.OFFLINE;
        handler.removeCallbacksAndMessages(null);
        scheduleIndicator();
    }


    public void setOnline() {
        lastState = States.ONLINE;
        handler.removeCallbacksAndMessages(null);
        updateIndicator();
    }

    private void scheduleIndicator() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateIndicator();
            }
        }, 400);
    }

    private void updateIndicator() {
        switch (lastState) {
            case OFFLINE:
                networkIndicator.setImageResource(R.drawable.indicator_offline);
                networkIndicatorText.setText(R.string.actionbar_txt_offline);
                break;
            case ONLINE:
                networkIndicator.setImageResource(R.drawable.indicator_online);
                networkIndicatorText.setText(R.string.actionbar_txt_online);
                break;
            case CONNECTING:
                networkIndicator.setImageResource(R.drawable.indicator_connecting);
                networkIndicatorText.setText(R.string.actionbar_txt_connecting);
                break;
        }
    }
}
