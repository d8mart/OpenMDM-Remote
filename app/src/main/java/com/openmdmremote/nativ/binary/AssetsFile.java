package com.openmdmremote.nativ.binary;

import android.content.Context;
import android.content.res.AssetManager;

import com.openmdmremote.WebkeyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetsFile {
    private AssetManager assetManager;
    private String src;
    private File dst;
    private String sDst;

    public AssetsFile(Context context, String src, String dst) {
        assetManager = context.getAssets();
        File root = context.getFilesDir();

        this.src = src;
        this.dst = new File(root, dst);

        sDst = dst; // just for logging
    }

    public boolean dstExists() {
        return dst.exists();
    }

    public void unpackFile() throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = assetManager.open(src);
            out = new FileOutputStream(dst);
            copyFile(in, out);
            grantPermission();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public String getDst() {
        return sDst;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void grantPermission() {
        String path = dst.getAbsolutePath();
        try {
            String[] cmd = {"sh", "-c", "chmod 704 " + path};
            File file = new File("/");
            Process process = Runtime.getRuntime().exec(cmd, null, file);
            process.waitFor();
        } catch (Exception e) {
            WebkeyApplication.log("AssetsFile", "failed to grant permission: " + path + e.toString());
        }
    }

    public void delete() {
        try {
            dst.delete();
        } catch (Exception e) {
        }
    }
}
