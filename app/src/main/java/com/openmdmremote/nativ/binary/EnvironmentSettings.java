package com.openmdmremote.nativ.binary;

import android.content.Context;
import com.openmdmremote.BuildConfig;
import java.io.File;

public class EnvironmentSettings {

    private final String assets_path;

    private static final String RUNNING_PATH_ROOTED = "/dev";
    private final String RUNNING_PATH_SYSTEM_SIGNED;

    private static final String BIN_FILE_NAME = "webkeynative";
    private static final String BIN_LOCK_FILE_NAME = BIN_FILE_NAME+".lock";
    private static final String BIN_UPDATE_FILE_NAME = "webkeynative_update";


    private static final String[] SU_PLACES = {
            "/sbin/",
            "/system/bin/",
            "/system/xbin/",
            "/data/local/xbin/",
            "/data/local/bin/",
            "/data/bin/",
            "/system/sd/xbin/",
            "/system/bin/failsafe/",
            "/data/local/"};

    EnvironmentSettings(Context context) {
        assets_path = context.getFilesDir().getAbsolutePath();
        RUNNING_PATH_SYSTEM_SIGNED = assets_path;
    }

    String getInterpreterPath() {
        if(isSystemSigned()) {
            return "sh";
        } else {
            return getSuPath();
        }

    }

    File getWKBinary() {
        return new File(getRunningPath() + "/" + BIN_FILE_NAME);
    }

    File getLockFile() {
        return new File(getRunningPath()+"/"+BIN_LOCK_FILE_NAME);
    }

    File getUpdateFile() {
        return new File(assets_path + "/" + BIN_UPDATE_FILE_NAME);
    }

    String getLdLibraryPath() {
        return assets_path;
    }

    File getRunningDirectory() {
        return new File(assets_path);
    }

    public static boolean checkRoot() {
        boolean rooted = false;
        for (String where : SU_PLACES) {
            if ( new File( where + "su" ).exists() ) {
                rooted = true;
                break;
            }
        }
        return rooted;
    }

    private String getRunningPath() {
        if (isSystemSigned()) {
            return RUNNING_PATH_SYSTEM_SIGNED;
        } else {
            return RUNNING_PATH_ROOTED;
        }
    }

    private static boolean isSystemSigned() {
        if (BuildConfig.FLAVOR.equals("systemsign")) {
            return true;
        } else {
            return false;
        }
    }

    private static String getSuPath() {
        for (String p : SU_PLACES) {
            File su = new File(p + "su");
            if (su.exists()) {
                return su.getAbsolutePath();
            }
        }
        return "su";
    }
}
