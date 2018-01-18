package com.openmdmremote.nativ.binary;

import android.content.Context;
import android.content.res.AssetManager;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.service.services.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Installer {
    private final String LOGTAG = "Installer";

    private enum ARCH_TYPES {
        ARM, X86
    }
    private final static String CPU_ARCH_ARM = "arm";
    private final static String CPU_ARCH_ARMV8 = "aarch64";
    private final static String CPU_ARCH_X86 = "x86";

    private final static String ARCH_FOLDER_ARM = "arm";
    private final static String ARCH_FOLDER_X86 = "x86";

    private ARCH_TYPES arch;

    Settings settings;

    Context mContext;
    String DST_BIN_UPDATE = "webkeynative_update";
    String DST_LIB_PROTOBUF = "libprotobuf-cpp-2.6.0-full.so";

    String ASSETS_BIN_FOLDER = "bin";
    String ASSETS_PREFIX = "webkeynative_";
    private final String VERSION_FILE_PATH = ASSETS_BIN_FOLDER + "/version_code.txt";

    String ASSETS_MISC_FOLDER = "misc";
    String ASSETS_LIB_PROTOBUF = "libprotobuf-cpp-2.6.0-full.so"; // for 2.6

    private AssetsFile binary;
    private AssetsFile libProtobuf;

    public Installer(Context context) {
        mContext = context;
        settings = new Settings(context);

        arch = archType();

        binary = new AssetsFile(context, getNativeFilePath(), DST_BIN_UPDATE);
        libProtobuf = new AssetsFile(context, getProtobufFilePath(), DST_LIB_PROTOBUF);
    }

    public void checkUpdate() {
        int prevCode = settings.getSavedVersionCode();
        int newCode = getPackageVersionCode();
        boolean doUpdate = false;

        if (prevCode != newCode) {
            doUpdate = true;
        }

        if (!binary.dstExists()) {
            doUpdate = true;
        }

        if (doUpdate) {
            doUpdate(prevCode, newCode);
        }
    }

    private void doUpdate(int prevCode, int newCode) {
        if (unpackFiles()) {
            try {
                writeOutVersion(newCode);
                settings.updatePackageVersionCode(newCode);
                WebkeyApplication.log(LOGTAG, "Update done from: " + prevCode + " to: " + newCode + ", ");
            } catch (FileNotFoundException e) {
                WebkeyApplication.log(LOGTAG, "Failed to write out the version code");
            }
        }
    }

    private boolean unpackFiles() {
        boolean result = true;
        try {
            binary.unpackFile();
        } catch (IOException e) {
            result = false;
            WebkeyApplication.log(LOGTAG, "Failed to unzip: " + binary.getDst() + " " + e);
        }

        try {
            unpackLibProto();
        } catch (IOException e) {
            result = false;
            WebkeyApplication.log(LOGTAG, "Failed to unzip: " + libProtobuf.getDst() + " " + e);
        }

        return result;
    }

    private void unpackLibProto() throws IOException {
        if (23 <= android.os.Build.VERSION.SDK_INT) {
            libProtobuf.unpackFile();
            deleteOldLib();
        }
    }

    // After 401 version code the lib name has been changed. SONAME issue.
    private void deleteOldLib() {
        AssetsFile f = new AssetsFile(mContext, null, "libprotobuf-26-cpp-full.so");
        f.delete();
    }

    private String getNativeFilePath() {
        String assets_file;
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        String currentVersionString = android.os.Build.VERSION.RELEASE;

        WebkeyApplication.log(LOGTAG, "Android versions: " + currentVersionString + "(" + currentapiVersion + ")");
        if (isAPI_19()) {
            assets_file = ASSETS_PREFIX + "19_4.4.3";
        } else if (isAPI_25()) {
            assets_file = ASSETS_PREFIX + "24";
        } else {
            assets_file = ASSETS_PREFIX+Integer.toString(currentapiVersion);
        }

        switch (arch) {
            case ARM:
                assets_file = ASSETS_BIN_FOLDER + "/" + ARCH_FOLDER_ARM + "/" + assets_file;
                break;
            case X86:
                assets_file = ASSETS_BIN_FOLDER + "/" + ARCH_FOLDER_X86 + "/" + assets_file;
                break;
        }
        return assets_file;
    }

    private boolean isAPI_19() {
        int currentAPIVersion = android.os.Build.VERSION.SDK_INT;
        String currentVersionString = android.os.Build.VERSION.RELEASE;

        if (currentAPIVersion == 19 && (currentVersionString.contains("4.4.3") || currentVersionString.contains("4.4.4"))) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isAPI_25() {
        if (android.os.Build.VERSION.SDK_INT == 25) {
            return true;
        } else {
            return false;
        }
    }


    private String getProtobufFilePath() {
        switch (arch) {
            case X86:
                return ASSETS_MISC_FOLDER + "/" + ARCH_FOLDER_X86 + "/" + ASSETS_LIB_PROTOBUF;
            default:
                return ASSETS_MISC_FOLDER + "/" + ARCH_FOLDER_ARM + "/" + ASSETS_LIB_PROTOBUF;
        }
    }

    private ARCH_TYPES archType() {
        String arch = System.getProperty("os.arch");
        WebkeyApplication.log(LOGTAG, "CPU arch is: " + arch);
        String arc = arch.toLowerCase();
        if (arc.contains(CPU_ARCH_ARM)) {
            return ARCH_TYPES.ARM;
        } else if (arc.contains(CPU_ARCH_ARMV8)) {
            return ARCH_TYPES.ARM;
        } else {
            return ARCH_TYPES.X86;
        }
    }

    private void writeOutVersion(int vcode) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(mContext.getFilesDir() + "/" + DST_BIN_UPDATE + ".txt");
        out.write(Integer.toString(vcode));
        out.close();
        grantPermission(mContext.getFilesDir() + "/" + DST_BIN_UPDATE + ".txt");
    }

    private void grantPermission(String path) {
        try {
            String[] cmd = {"sh","-c","chmod 704 "+path};
            File file = new File("/");
            Process process = Runtime.getRuntime().exec(cmd, null, file);
            process.waitFor();
        } catch (Exception e) {
            WebkeyApplication.log(LOGTAG, "Failed to grant permission: " + path + e.toString());
        }
    }

    private int getPackageVersionCode() {
        InputStream in = null;
        BufferedReader reader = null;
        AssetManager assetManager = mContext.getAssets();
        int versionCode = 1;

        try {
            in = assetManager.open(VERSION_FILE_PATH);
            reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            if(line != null) {
                versionCode = Integer.parseInt(line);
            }
        } catch (IOException e) {
            versionCode = 1;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return versionCode;
    }
}
