package com.openmdmremote.service.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;

import com.openmdmremote.R;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.service.dto.User;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class LocalAuthService extends BroadcastReceiver {

	private Context mContext;

	private int lastNotifId = 0;
	private int pIntentId = 0;

	private static Map<Integer, Credentials> pendingSignUps = new HashMap<>();

	public LocalAuthService() {
	}

	public LocalAuthService(Context c){
		this.mContext = c;
	}

    @Override
    public void onReceive(Context context, Intent intent) {
        Integer id = intent.getIntExtra("signup_id", -1);
        Boolean confirm = intent.getBooleanExtra("confirm", false);

        if(confirm){
            Credentials c = pendingSignUps.remove(id);
            UsersDataSource dataSource = new UsersDataSource(context);
            dataSource.createUser(c.getUserName(), computeSHAHash(c.getPassword()));
            dataSource.close();
            WebkeyApplication.log("Authservice", "sign up accepted by:" + c.getUserName());
        } else {
            WebkeyApplication.log("Authservice", "sign up denied by user");
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

	public boolean authorize(Credentials credentials) {
		synchronized (pendingSignUps) {
			UsersDataSource dataSource = new UsersDataSource(mContext);
			try {
				String hash = computeSHAHash(credentials.getPassword());
				User found = dataSource.getUserByUsernameAndPassword(credentials.getUserName(), hash);
				if (found == null) {
					return false;
				}

				return true;
			} finally {
				dataSource.close();
			}
		}
	}

	public boolean signUpLocalUser(String username, String password) {
		UsersDataSource dataSource = new UsersDataSource(mContext);
		try {
			User old = dataSource.getUserByUsername(username);
			if (old != null) {
				return false;
			}
			dataSource.createUser(username, computeSHAHash(password));
			return true;
		} finally {
			dataSource.close();
		}
	}

    public void cleanUsers() {
        UsersDataSource dataSource = new UsersDataSource(mContext);
        try {
            dataSource.cleanUsers();
        } finally {
            dataSource.close();
        }
    }

	public boolean signUpFromBrowser(Credentials credentials){
		UsersDataSource dataSource = new UsersDataSource(mContext);
        String nick = credentials.getUserName();

        if (nick.isEmpty()) {
            return false;
        }

        if (dataSource.getUserByUsername(nick) != null) {
            return false;
        }

		try {
			for (Credentials c : pendingSignUps.values()) {
				if (c.getUserName() == nick) {
					return false;
				}
			}

			Intent intentConfirm = new Intent(mContext, LocalAuthService.class);
			intentConfirm.setAction("confirm");
			intentConfirm.putExtra("signup_id", lastNotifId);
			intentConfirm.putExtra("confirm", true);
			PendingIntent pIntentConfirm = PendingIntent.getBroadcast(mContext, pIntentId++, intentConfirm, PendingIntent.FLAG_CANCEL_CURRENT);

			Intent intentDeny = new Intent(mContext, LocalAuthService.class);
			intentConfirm.setAction("deny");
			intentDeny.putExtra("signup_id", lastNotifId);
			intentDeny.putExtra("confirm", false);
			PendingIntent pIntentDeny = PendingIntent.getBroadcast(mContext, pIntentId++, intentDeny, PendingIntent.FLAG_CANCEL_CURRENT);

			pendingSignUps.put(lastNotifId, credentials);
			Notification n = new Notification.Builder(mContext)
					.setContentTitle(mContext.getResources().getString(R.string.txt_signup) + " " + nick)
					.setSmallIcon(R.drawable.notification)
					.addAction(R.drawable.ic_action_accept, mContext.getResources().getString(R.string.btn_confirm), pIntentConfirm)
					.addAction(R.drawable.ic_action_cancel, mContext.getResources().getString(R.string.btn_deny), pIntentDeny).build();

			NotificationManager notificationManager =
					(NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);

			notificationManager.notify(lastNotifId++, n);
			return true;
		} finally {
			dataSource.close();
		}
	}

	private static String convertToHex(byte[] data) throws java.io.IOException{
		StringBuffer sb = new StringBuffer();
		String hex;

		hex= Base64.encodeToString(data, 0, data.length, 0);

		sb.append(hex);

		return sb.toString();
	}

	private String computeSHAHash(String password) {
		MessageDigest mdSha1 = null;
		String hash = null;
		try{
			mdSha1 = MessageDigest.getInstance("SHA-1");
			mdSha1.update(password.getBytes("ASCII"));
			byte[] data = mdSha1.digest();
			hash=convertToHex(data);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if(hash == null){
			// TODO: ?
			hash = password;
		}
		return hash;
	}
}
