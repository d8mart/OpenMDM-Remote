package com.openmdmremote.net.visitor;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.openmdmremote.R;
import com.openmdmremote.service.BackgroundService;
import com.openmdmremote.ui.main.MainActivity;

import java.util.LinkedHashSet;
import java.util.Set;

public class VisitorNotifier {

    private final int NOTIFY_ID = 56;
    private final Context context;
    Set<VisitorChangesListener> listeners = new LinkedHashSet<>();
    private final Object listenerLock = new Object();

    private String lastUsername = "";

    private WebkeyVisitor lastVisitor;


    public VisitorNotifier(Context context) {
        this.context = context;
    }


    public void hideForeground() {
        if (BackgroundService.mainService != null) {
            BackgroundService.mainService.stopForeground(true);
        }

        lastUsername = "";
    }

    public void showAnonymousNotification() {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notif = builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_foreground_open_rmeote)
                .setTicker(context.getText(R.string.visitormanager_notif_ticker))
                .setContentTitle(context.getText(R.string.app_name_2))
                .setContentText(context.getText(R.string.visitormanager_notif_text))
                .build();

        if (BackgroundService.mainService != null) {
            BackgroundService.mainService.startForeground(NOTIFY_ID, notif);
        }
    }

    public void showNotification(String username) {
        if (lastUsername.equals(username)) {
            return;
        }

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notif = builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_foreground_open_rmeote)
                .setTicker(context.getText(R.string.visitormanager_online_notif_ticker) + " " + username)
                .setContentTitle(context.getText(R.string.app_name_2))
                .setContentText(context.getText(R.string.visitormanager_online_notif_text) + " " + username)
                .setVibrate(new long[]{100, 90, 80, 400})
                .build();
        BackgroundService.mainService.startForeground(NOTIFY_ID, notif);
        lastUsername = username;
    }

    public void addVisitorChangesListener(VisitorChangesListener visitorChangesListener) {
        synchronized (listenerLock) {
            if (lastVisitor != null) {
                visitorChangesListener.onNewVisitor(lastVisitor.getUsername(), lastVisitor.getBrowserInfo());
            }
            listeners.add(visitorChangesListener);
        }
    }

    public void removeVisitorChangesListener(VisitorChangesListener visitorChangesListener) {
        synchronized (listenerLock) {
            listeners.remove(visitorChangesListener);
        }
    }

    public void newVisitor(WebkeyVisitor neu) {
        synchronized (listenerLock) {
            lastVisitor = neu;
            for (VisitorChangesListener l : listeners) {
                l.onNewVisitor(neu.getUsername(), neu.getBrowserInfo());
            }
        }
    }

    public void leftLastVisitor() {
        synchronized (listenerLock) {
            lastVisitor = null;
            for (VisitorChangesListener l : listeners) {
                l.onLeftLastVisitor();
            }
        }
    }
}
