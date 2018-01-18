package com.openmdmremote.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.openmdmremote.R;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.settings.HarborAuthSettings;
import com.openmdmremote.nativ.binary.Installer;
import com.openmdmremote.net.visitor.BrowserInfo;
import com.openmdmremote.net.visitor.VisitorChangesListener;
import com.openmdmremote.service.BackgroundService;
import com.openmdmremote.service.OnBackgroundServiceListener;
import com.openmdmremote.service.services.Settings;
import com.openmdmremote.ui.typefaces.MyArrayAdapter;
import com.openmdmremote.ui.views.RunButton;
import com.openmdmremote.ui.views.VisitorView;

import java.util.ArrayList;
import java.util.List;

public class FragmentMain extends Fragment implements OnBackgroundServiceListener, VisitorChangesListener {

    View view;

    ArrayAdapter<String> addressListAdapter;
    List<String> addresses = new ArrayList<>();

    BackgroundService mService;

    Settings settings;
    HarborAuthSettings harborAuth;

    RunButton btnRun;

    TextView txtHarboraddress;

    LinearLayout addressHolder;
    VisitorView visitorView;

    private LocationPermissionManager locationPermMgm;


    public FragmentMain() {
    }

    @Override
    public synchronized void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(getActivity());
        harborAuth = new HarborAuthSettings(getActivity());
        locationPermMgm = new LocationPermissionManager(getActivity());
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        WebkeyApplication.log("FragmentMain", "onResuemFragMain");

        mService = ((MainActivity) getActivity()).getService();

        /*
         * Workaround egy erdekes hibara. A backend leallitasat kovetoen lefut egy full lifecircle
         * (pause, resuem). Letrejon egy uj instance de meg az elott, hogy az activity csatlakozott
         * volna a servicehez vagy peldanyositotta volna a fragmentet.
         */


        if (mService == null) {
           // WebkeyApplication.log("FragmentMain", "remove this fragment");
          //  getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            return;
        }
        mService.addBackgroundServiceListener(FragmentMain.this);
        mService.addVisitorChangesListener(FragmentMain.this);

        try{
            //stopService();
          //  unbindService();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //startService();
           // bindService();
        }
    }

    @Override
    public synchronized View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        this.view = view;

        addressHolder = (LinearLayout) view.findViewById(R.id.device_addresses);
        visitorView = (VisitorView) view.findViewById(R.id.visitor);

        // Set listView.
        addressListAdapter = new MyArrayAdapter(view.getContext(), R.layout.text_middle, android.R.id.text1, addresses);
       // ((ListView) view.findViewById(R.id.list_addresses)).setAdapter(addressListAdapter);

        // The buttons are disabled until service connection does not establish.
        btnRun = (RunButton) view.findViewById(R.id.btn_run);
        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BTN","CLICK");
              //  if (mService.webServiceIsAlive()) {
                mService = ((MainActivity) getActivity()).getService();

               // stopService();
                   // unbindService();
              //  } else {
               //     locationPermMgm.checkPermission();
                   // startService();
                   // bindService();
              //  }

                //((MainActivity) getActivity()).resetService();

                try {
                    mService.closeConns();
                }finally {
                    mService.startWebServices();
                }
            }
        });

        txtHarboraddress = (TextView) view.findViewById(R.id.txt_webkey_address);
        if (harborAuth.isRegisteredToHarbor()) {
            txtHarboraddress.setVisibility(View.VISIBLE);
        } else {
            txtHarboraddress.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onPause() {
        // For workaround need to check
        if (mService != null) {
            mService.removeBackgroundServiceListener();
            mService.removeVisitorChangesListener(this);
        }
        super.onPause();
    }

    @Override
    public void serviceStarted() {
        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
              //  txtHarboraddress.setText(mService.getWebkeyNickAddress());
                txtHarboraddress.setText(R.string.intro_opendmmr);
                btnRun.setOnState();
                setViewsToRunningState();
                mService.getAddresses(addresses);
                addressListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void serviceStopped() {
        if(getActivity() == null ) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                txtHarboraddress.setText(mService.getWebkeyNickAddress());
                btnRun.setOffState();
                setViewsToNotRunningState();
            }
        });
    }

    @Override
    public void onLeftLastVisitor() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                visitorView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onNewVisitor(final String username, final BrowserInfo browserInfo) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                visitorView.setVisibility(View.VISIBLE);
                visitorView.setNewVisitor(username, browserInfo);
            }
        });
    }

    private void startService() {
       // btnRun.setPengingState();
      //  setViewsToNotRunningState();
        mService.initHarborConnection(); //agregado para empezar a enviar imgs con el boton run
        mService.startWebServices();
    }

    private void stopService() {
       // btnRun.setPengingState();
        mService.stopWebServices();
    }

    private void setViewsToNotRunningState() {
        addressHolder.setVisibility(View.GONE);
        visitorView.setVisibility(View.GONE);
    }

    private void setViewsToRunningState() {
        mService.getAddresses(addresses);
        addressListAdapter.notifyDataSetChanged();
        addressHolder.setVisibility(View.VISIBLE);
    }

    public void bindService(){
        Intent intent = new Intent(getActivity(), BackgroundService.class);
        getActivity().startService(intent);
    }

    public void unbindService(){
        Intent intent = new Intent(getActivity(), BackgroundService.class);
        getActivity().stopService(intent);
    }
}
