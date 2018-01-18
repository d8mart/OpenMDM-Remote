package com.openmdmremote.ui.main;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.openmdmremote.BuildConfig;
import com.openmdmremote.R;
import com.openmdmremote.harbor.settings.HarborAuthSettings;
import com.openmdmremote.service.dto.User;
import com.openmdmremote.service.services.UsersDataSource;
import com.openmdmremote.ui.typefaces.FontSetter;
import com.openmdmremote.ui.typefaces.MyArrayAdapter;
import com.openmdmremote.ui.typefaces.MyTextView;

import java.util.ArrayList;
import java.util.List;


public class FragmentUserSettings extends Fragment implements AdapterView.OnItemLongClickListener, CompoundButton.OnCheckedChangeListener {
    private Context context;
    private HarborAuthSettings harborHarborAuthSettings;

    private User selectedUser;
    private ArrayAdapter<User> userListAdapter;
    List<User> users = new ArrayList<>();
    ListView lv;
    LinearLayout adminPanel;
    SwitchCompat adminSwitch;
    MyTextView adminNick;

    public FragmentUserSettings() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        harborHarborAuthSettings = new HarborAuthSettings(getActivity());
        loadLocalUsers();

        if (harborHarborAuthSettings.isRegisteredToHarbor()) {
            updateAdminDetails();
            adminPanel.setVisibility(View.VISIBLE);
        } else {
            adminPanel.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);

        lv = (ListView) view.findViewById(R.id.user_list);
        lv.setOnItemLongClickListener(this);

        adminPanel = (LinearLayout) view.findViewById(R.id.admin_users);
        adminSwitch = (SwitchCompat) view.findViewById(R.id.remote_switch);
        adminNick = (MyTextView) view.findViewById(R.id.remote_nick);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                showAddUserDialog();
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        selectedUser = (User) adapterView.getItemAtPosition(i);
        createDeleteDialog();
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        harborHarborAuthSettings.setRemoteAccess(b);
    }

    private void updateAdminDetails() {
        adminSwitch.setChecked(harborHarborAuthSettings.isRemoteAccessEnabled());
        adminSwitch.setOnCheckedChangeListener(this);
        if(!isFleeted()) {
            adminNick.setText(harborHarborAuthSettings.getAccountName());
        } else {
            adminNick.setText(getResources().getString(R.string.users_remoteuser_nick));
        }
    }

    private boolean isFleeted() {
        if(BuildConfig.FLEED_ID == null) {
            return false;
        } else {
            return true;
        }
    }

    private void createDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = builder.setMessage(getResources().getString(R.string.txt_delete_approve) + selectedUser.toString())
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();

        Button negative = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
        Button positive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setTextColor(getResources().getColor(R.color.colorAccent));
        negative.setTextColor(getResources().getColor(R.color.colorAccent));

        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        FontSetter fontSetter = new FontSetter(context);
        fontSetter.updateFontToLight(textView);
    }

    private void showAddUserDialog() {
        UserAddDialog userAddDialog = new UserAddDialog();
        userAddDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                loadLocalUsers();
            }
        });
        FragmentManager fm = getActivity().getFragmentManager();
        userAddDialog.show(fm, "dialog");
    }

    private void loadLocalUsers() {
        UsersDataSource userDataSource = new UsersDataSource(context);
        users = userDataSource.getAllUsers();
        userDataSource.close();
        userListAdapter = new MyArrayAdapter(context, android.R.layout.simple_list_item_1, android.R.id.text1, users);
        userListAdapter.notifyDataSetChanged();
        lv.setAdapter(userListAdapter);
        lv.invalidate();
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    userListAdapter.remove(selectedUser);
                    UsersDataSource userDataSource = new UsersDataSource(context);
                    userDataSource.deleteUser(selectedUser);
                    userDataSource.close();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };
}
