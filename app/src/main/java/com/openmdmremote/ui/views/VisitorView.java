package com.openmdmremote.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.openmdmremote.R;
import com.openmdmremote.net.visitor.BrowserInfo;
import com.openmdmremote.ui.typefaces.MyTextView;

import java.util.concurrent.TimeUnit;

public class VisitorView extends LinearLayout {

    private final String elapsedTimeFormat = "%02d:%02d".toLowerCase();

    private MyTextView nickname;
    private MyTextView agent;
    private MyTextView time;
    private ImageView platform;

    public VisitorView(Context context) {
        super(context);
    }

    private void setPlatformImg(BrowserInfo browserInfo) {
        switch (browserInfo.getPlatform()) {
            case WIN:
                platform.setImageResource(R.drawable.windows);
                break;
            case MAC:
                platform.setImageResource(R.drawable.mac);
                break;
            case LINUX:
                platform.setImageResource(R.drawable.linux);
                break;
            default:
                platform.setImageResource(R.drawable.windows);
                break;
        }
    }

    private void setTime(BrowserInfo browserInfo) {
        /*
        private final DateFormat df = new SimpleDateFormat("HH:mm");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String since = df.format(elapsedTime);
        */

        long now = System.currentTimeMillis();
        long elapsedTime = now - browserInfo.getloginTime();
        long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
                - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTime));
        String s = String.format(elapsedTimeFormat, hours, minutes);
        time.setText(s);
    }

    public VisitorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_visitor, this, true);

        nickname = (MyTextView) findViewById(R.id.visitor_nick);
        agent = (MyTextView) findViewById(R.id.visitor_agent);
        time = (MyTextView) findViewById(R.id.logintime);
        platform = (ImageView) findViewById(R.id.visitor_platform);
    }

    public VisitorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setNewVisitor(String username, BrowserInfo browserInfo) {
        nickname.setText(username);
        agent.setText(browserInfo.getAgent());
        setPlatformImg(browserInfo);
        setTime(browserInfo);
    }
}
