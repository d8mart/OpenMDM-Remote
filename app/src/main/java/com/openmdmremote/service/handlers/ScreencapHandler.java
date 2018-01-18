package com.openmdmremote.service.handlers;

import android.content.ComponentCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.client.ConnectionStateNotifier.OnHarborConnectionListener;
import com.openmdmremote.nativ.handlers.ScreenCap;
import com.openmdmremote.net.visitor.WebkeyVisitor;
import com.openmdmremote.service.BackgroundService;
import com.openmdmremote.service.dto.Message;
import com.openmdmremote.service.dto.ScreenAck;
import com.openmdmremote.service.dto.ScreenOptionsPayload;
import com.openmdmremote.service.handlers.interfaces.IScreenCap;
import com.openmdmremote.service.handlers.interfaces.MessageHandler;
import com.openmdmremote.service.services.Settings;
import com.openmdmremote.ui.main.ConnectionIndicator;
import com.openmdmremote.ui.main.LocationPermissionManager;
import com.openmdmremote.ui.main.MainActivity;

import java.io.IOException;


public class ScreencapHandler implements MessageHandler, ComponentCallbacks {
    private static final String LOGTAG = "handler ScreenCap";

    private final Gson gson = new GsonBuilder().create();
    private ImageSendWorker sender;

    private  IScreenCap screenCap;
    private final JitterControl jitterControl;

    private boolean hasNavBar = true;
    private Context mContext;

    private WebkeyVisitor mWebkeyVisitor;

    public static int backRerunning=0;


    public ScreencapHandler(final Context context, WebkeyVisitor webkeyVisitor) {
        Log.i("ScreencapHandler", "ScreencapHandler Constructor");
        mContext = context;
        mWebkeyVisitor = webkeyVisitor;

        screenCap = new ScreenCap(context);
        jitterControl = new JitterControl(screenCap);

        detectSoftKey();

        sendScreenOptionsToBrowser();

        startImageSenderStream();

        mContext.registerComponentCallbacks(this);
    }

    private void detectSoftKey(){
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        hasNavBar = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
        WebkeyApplication.log(LOGTAG,"Has navbar: "+hasNavBar);
    }

    @Override
    public void onData(Message msg) {
        switch(msg.type){
            case SCREEN_START:
                startImageSenderStream();
                break;
            case SCREEN_STOP:
                stopImageSenderStream();
                break;
            case SCREEN_OPTIONS:
                ScreenOptionsPayload opts = gson.fromJson(msg.payload, ScreenOptionsPayload.class);
                if (opts.isValid()) {
                    jitterControl.setResolution(opts.screenX, opts.screenY);
                }
                break;
            case SCREEN_ACK:
                ScreenAck ack = gson.fromJson(msg.payload, ScreenAck.class);
                jitterControl.receivedAck((byte) ack.sequenceNumber, ack.timestamp);
        }
    }

    @Override
    public void onLeftUser(WebkeyVisitor webkeyVisitor) {
        // Stop the screen sender thread.
        stopImageSenderStream();
        mContext.unregisterComponentCallbacks(this);
    }

    @Override
    public void onLeftAllUsers() {
        stopImageSenderStream();
        mContext.unregisterComponentCallbacks(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        sendScreenOptionsToBrowser();
    }

    @Override
    public void onLowMemory() {

    }

    private void sendScreenOptionsToBrowser() {
        // Todo: add the orientation variable
        ScreenCap.BrowserScreenMetrics metrics = screenCap.getScreenMetrics();
        mWebkeyVisitor.sendGson(new Message("1", Message.Type.SCREEN_OPTIONS, new ScreenOptionsPayload(
                metrics.getWidth(),
                metrics.getHeight(),
                metrics.getRotation(),
                hasNavBar
        )));
    }


    private synchronized void startImageSenderStream () {
        Log.i("ScrencapHandler","startImageSenderStream");
        stopImageSenderStream();
        sender = new ImageSendWorker();
        new Thread(sender).start();
    }

    private synchronized void stopImageSenderStream () {
        if(sender != null)
            sender.stop();
    }

     public  class ImageSendWorker implements Runnable{

        private boolean running = false;

        @Override
        public void run() {
            Thread.currentThread().setName("Image sender thread");
            running = true;
            Log.i("ScrencapHandler","Image sender thread");
            jitterControl.reset();

            try {
                // Just for gnuplot logging.
                // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                while(running){
                    byte[] b = screenCap.getScreen();

                    if(b==null && !running){
                        Log.i("ScrencapHandler","Image sender thread break");
                        break;
                    } else if(b==null) {
                        Log.i("ScrencapHandler","Image sender thread continue");
                       // screenCap = new ScreenCap(mContext);
                        if(backRerunning==0) {
                            backRerunning=1;
                            //WIPC.getInstance(mContext).runBackend();
                           // new MainActivity().resetService();
                          /*  Intent intent = new Intent(mContext, BackgroundService.class);

                            try {
                                mContext.unbindService(serviceIPC);
                                mContext.stopService(intent);
                            }finally {
                                mContext.startService(intent);
                                try{mContext.bindService(intent, serviceIPC, Context.BIND_NOT_FOREGROUND);
                                }catch (Exception e){e.printStackTrace();}finally {
                                    backRerunning=0;

                                }

                            }*/

                        }
                        continue;
                    }

                    handleImage(b);
                }
            } catch (Exception e) {
                running = false;
                Log.i("ScrencapHandler","Image sender thread catch false");
            }
        }

        private void handleImage(byte[] b) throws IOException {
            // Add unixtimestamp end of the image data.
            Log.i("ScrencapHandler","handleImage");
            long utime = System.currentTimeMillis();
            byte[] unixTime = toByteArray(utime);
            int start = b.length - unixTime.length - 1; //-1 for the seqnum
            for (int i = 0; i < unixTime.length; i++) {
                b[start + i] = unixTime[i];
            }

            if (jitterControl.channelIsFree()) {
                int seq = jitterControl.sendt(utime, b.length);

                // Add seq number.
                b[b.length - 1] = (byte) seq;

                // Send out the data to the network.
                Log.i("ScrencapHandler","Send out the data to the network");
                mWebkeyVisitor.send(b);
            }
        }

        public void stop(){
            running = false;
        }

        /**
         * FROM GUAVA
         * Returns a big-endian representation of {@code value} in an 8-element byte
         * array; equivalent to {@code ByteBuffer.allocate(8).putLong(value).array()}.
         * For example, the input value {@code 0x1213141516171819L} would yield the
         * byte array {@code {0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19}}.
         */
        private byte[] toByteArray(long value) {
            // Note that this code needs to stay compatible with GWT, which has known
            // bugs when narrowing byte casts of long values occur.
            byte[] result = new byte[8];
            for (int i = 7; i >= 0; i--) {
                result[i] = (byte) (value & 0xffL);
                value >>= 8;
            }
            return result;
        }

         private ViewPager pager;
         private MainActivity.MyAdapter myAdapter;
         private TabLayout tabLayout;
         private View splash;
         private ConnectionIndicator connectionIndicator;

         private OnHarborConnectionListener onHarborConnectionListener;

         private LocationPermissionManager locationPermMgm;

         private Settings settings;

         private BackgroundService service;
         boolean isBound = false;

         private ServiceConnection serviceIPC = new ServiceConnection() {
             @Override
             public void onServiceConnected(ComponentName className, IBinder binder) {
                 WebkeyApplication.log("MainActivity", "onServiceConnected");
                 BackgroundService.MyLocalBinder mBinder = (BackgroundService.MyLocalBinder) binder;
                 service = mBinder.getService();
                 isBound = true;
                 //service.addHarborConnectionListener(onHarborConnectionListener);

             }

             @Override
             public void onServiceDisconnected(ComponentName name) {
                 isBound = false;
             }
         };
    }
}
