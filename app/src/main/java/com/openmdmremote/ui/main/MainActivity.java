package com.openmdmremote.ui.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.openmdmremote.R;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.client.ConnectionStateNotifier.OnHarborConnectionListener;
import com.openmdmremote.harbor.settings.HarborAuthSettings;
import com.openmdmremote.service.BackgroundService;
import com.openmdmremote.service.services.Settings;
import com.openmdmremote.ui.registration.RegistrationActivity;

public class MainActivity extends AppCompatActivity {

    private ViewPager pager;
    private MyAdapter myAdapter;
    private TabLayout tabLayout;
    private View splash;
    private ConnectionIndicator connectionIndicator;

    private OnHarborConnectionListener onHarborConnectionListener;

    private LocationPermissionManager locationPermMgm;

    private Settings settings;

    private BackgroundService service;
    boolean isBound = false;

    private int[] tabIcons = {
            R.drawable.home,
            R.drawable.user,
            R.drawable.settings,
            R.drawable.about
    };

    private int[] tabIconsActive = {
            R.drawable.home_active,
            R.drawable.user_active,
            R.drawable.settings_active,
            R.drawable.about_active
    };


    private ServiceConnection serviceIPC = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            WebkeyApplication.log("MainActivity", "onServiceConnected");
            BackgroundService.MyLocalBinder mBinder = (BackgroundService.MyLocalBinder) binder;
            service = mBinder.getService();
            isBound = true;
            service.addHarborConnectionListener(onHarborConnectionListener);
            initFragments();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebkeyApplication.log("MainActivity", "onCreate");
        //System.setProperty("http.keepAlive", "false");
        setContentView(R.layout.activity_main);
        settings = new Settings(this);
        locationPermMgm = new LocationPermissionManager(this);


        // Inflate the actionbar view
        LayoutInflater inflater = LayoutInflater.from(this);
        View header = inflater.inflate(R.layout.actionbar, null);

        // Aet the action bar.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setCustomView(header);
        actionBar.setDisplayShowCustomEnabled(true);

        connectionIndicator = new ConnectionIndicator(header);
        onHarborConnectionListener = new OnHarborConnectionListener() {
            @Override
            public void onHarborConnectionChanged(final ConnectionStates connectionStates) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        switch (connectionStates) {
                            case DISCONNECTED:
                                connectionIndicator.setOffile();
                                break;
                            case CONNECTED:
                                connectionIndicator.setOnline();
                                break;
                            case CONNECTING:
                                connectionIndicator.setConnecting();
                                break;
                        }
                    }
                });
            }
        };


        pager = (ViewPager) findViewById(R.id.pager_main);

        splash = findViewById(R.id.splash);

        tabLayout = (TabLayout) findViewById(R.id.tabs);


        locationPermMgm.checkPermission();
        //checkRegistration();  Aqui se verifica si necesita registrarse
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        WebkeyApplication.log("MainActivity", "onResuem");
        Intent intent = new Intent(this, BackgroundService.class);
        try {
            // if (!isBound) {

          //  stopService(intent);

          //  if (settings.getFirstStart()) {
                intent.setAction("webkey.intent.action.START");  // intent.setAction("webkey.intent.action.START");
                startService(intent);
           //     settings.setFirstStart(false);
          //  }


            // } else {
             // refreshServerConnectionindicator();
            // }
        }finally {
          //  service = this.getService();
          //  if(service!=null)
            //service.startWebServices();
            //startService(intent);
            try{

               // bindService(intent, serviceIPC, Context.BIND_NOT_FOREGROUND);
            }catch (Exception e){e.printStackTrace();}
        }
        WebkeyApplication.log("MainActivity", "onResume exit");
    }

    @Override
    public synchronized void onDestroy() {
        WebkeyApplication.log("MainActivity", "onDestroy");
        if (isBound) {
          //  service.removeHarborConnectionListener(onHarborConnectionListener);
          //  service.leftActivity();
           // unbindService(serviceIPC);
        }
        isBound = false;
        super.onDestroy();
    }

    public synchronized void resetService(){
        //Intent intent = new Intent(this, BackgroundService.class);
        try {
                try{unbindService(serviceIPC);}catch (Exception e){e.printStackTrace();}
                //stopService(intent);
        }finally {
            /*try {
                //Intent intent = new Intent(this, BackgroundService.class);
                startService(intent);
                try {
                    bindService(intent, serviceIPC, Context.BIND_NOT_FOREGROUND);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }finally {
                ScreencapHandler.backRerunning=0;
            }*/
        }

    }

    private void checkRegistration() {
        if (shouldStartRegistartionActivity()) {
            Intent intent = new Intent(this, RegistrationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private boolean shouldStartRegistartionActivity() {
        Settings settings = new Settings(this);
        if(settings.isFleeted()) {
            return false;
        }

        HarborAuthSettings harborAuthSettings = new HarborAuthSettings(this);
        if (!harborAuthSettings.isRegisteredToHarbor()) {
            return true;
        }
        return false;
    }

    private synchronized void initFragments() {
        myAdapter = new MyAdapter(getSupportFragmentManager());
        pager.setAdapter(myAdapter);
        tabLayout.setupWithViewPager(pager);
        setupTabsIcons();
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                tab.setIcon(tabIconsActive[pos]);
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                tab.setIcon(tabIcons[pos]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        splash.setVisibility(View.GONE);
    }

    private void setupTabsIcons() {
        tabLayout.getTabAt(0).setIcon(tabIconsActive[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
    }

    public void refreshServerConnectionindicator() {
        onHarborConnectionListener.onHarborConnectionChanged(service.getHarborConnectionState());
    }



    @Override
    public void onBackPressed() {
        pager.setCurrentItem(0);
    }

    public class MyAdapter extends FragmentPagerAdapter {
        private final int NUMOFFRAGMENTS = 4;

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUMOFFRAGMENTS;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FragmentMain();
                case 1:
                   // return new FragmentUserSettings();
                   return new FragmentMain();
                case 2:
                   // return new FragmentSettings();
                   return new FragmentMain();
                case 3:
                   // return new FragmentAbout();
                   return new FragmentMain();
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    public BackgroundService getService () {
        return service;
    }
}
