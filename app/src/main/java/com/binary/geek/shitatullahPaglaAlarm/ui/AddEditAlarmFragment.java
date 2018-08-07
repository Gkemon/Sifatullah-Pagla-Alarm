package com.binary.geek.shitatullahPaglaAlarm.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.binary.geek.shitatullahPaglaAlarm.R;
import com.binary.geek.shitatullahPaglaAlarm.data.DatabaseHelper;
import com.binary.geek.shitatullahPaglaAlarm.model.Alarm;
import com.binary.geek.shitatullahPaglaAlarm.service.AlarmReceiver;
import com.binary.geek.shitatullahPaglaAlarm.service.LoadAlarmsService;
import com.binary.geek.shitatullahPaglaAlarm.util.AlarmUtils;
import com.binary.geek.shitatullahPaglaAlarm.util.ViewUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public final class AddEditAlarmFragment extends Fragment {

    private TimePicker mTimePicker;
    private EditText mLabel;
    private CheckBox mMon, mTues, mWed, mThurs, mFri, mSat, mSun;
    private AdView mAdView;
    InterstitialAd mInterstitialAd;

    public static AddEditAlarmFragment newInstance(Alarm alarm) {

        Bundle args = new Bundle();
        args.putParcelable(AddEditAlarmActivity.ALARM_EXTRA, alarm);

        AddEditAlarmFragment fragment = new AddEditAlarmFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_add_edit_alarm, container, false);

        setHasOptionsMenu(true);

        final Alarm alarm = getAlarm();

        mTimePicker = (TimePicker) v.findViewById(R.id.edit_alarm_time_picker);
        ViewUtils.setTimePickerTime(mTimePicker, alarm.getTime());

        mLabel = (EditText) v.findViewById(R.id.edit_alarm_label);
        mLabel.setText(alarm.getLabel());

        mMon = (CheckBox) v.findViewById(R.id.edit_alarm_mon);
        mMon.setChecked(true);

        mTues = (CheckBox) v.findViewById(R.id.edit_alarm_tues);
        mTues.setChecked(true);

        mWed = (CheckBox) v.findViewById(R.id.edit_alarm_wed);
        mWed.setChecked(true);

        mThurs = (CheckBox) v.findViewById(R.id.edit_alarm_thurs);
        mThurs.setChecked(true);

        mFri = (CheckBox) v.findViewById(R.id.edit_alarm_fri);
        mFri.setChecked(true);

        mSat = (CheckBox) v.findViewById(R.id.edit_alarm_sat);
        mSat.setChecked(true);

        mSun = (CheckBox) v.findViewById(R.id.edit_alarm_sun);
        mSun.setChecked(true);

        setDayCheckboxes(alarm);

        setBannerAD(v);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_alarm_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                //setfullScreenAD();
                save();
                break;
            case R.id.action_delete:
                delete();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Alarm getAlarm() {
        return getArguments().getParcelable(AddEditAlarmActivity.ALARM_EXTRA);
    }

    private void setDayCheckboxes(Alarm alarm) {



        mMon.setChecked(alarm.getDay(Alarm.MON));
        mTues.setChecked(alarm.getDay(Alarm.TUES));
        mWed.setChecked(alarm.getDay(Alarm.WED));
        mThurs.setChecked(alarm.getDay(Alarm.THURS));
        mFri.setChecked(alarm.getDay(Alarm.FRI));
        mSat.setChecked(alarm.getDay(Alarm.SAT));
        mSun.setChecked(alarm.getDay(Alarm.SUN));



    }

    private void save() {



        Log.d("GK","SAVE");

        final Alarm alarm = getAlarm();

        final Calendar time = Calendar.getInstance();

        time.set(Calendar.MINUTE, ViewUtils.getTimePickerMinute(mTimePicker));
        time.set(Calendar.HOUR_OF_DAY, ViewUtils.getTimePickerHour(mTimePicker));

        Log.d("GK","Current min:"+ViewUtils.getTimePickerMinute(mTimePicker));


        alarm.setTime(time.getTimeInMillis());



        alarm.setLabel(mLabel.getText().toString());

        alarm.setDay(Alarm.MON, mMon.isChecked());
        alarm.setDay(Alarm.TUES, mTues.isChecked());
        alarm.setDay(Alarm.WED, mWed.isChecked());
        alarm.setDay(Alarm.THURS, mThurs.isChecked());
        alarm.setDay(Alarm.FRI, mFri.isChecked());
        alarm.setDay(Alarm.SAT, mSat.isChecked());
        alarm.setDay(Alarm.SUN, mSun.isChecked());

        final int rowsUpdated = DatabaseHelper.getInstance(getActivity()).updateAlarm(alarm);
        final int messageId = (rowsUpdated == 1) ? R.string.update_complete : R.string.update_failed;

        Toast.makeText(getActivity(), messageId, Toast.LENGTH_SHORT).show();

        AlarmReceiver.setReminderAlarm(getActivity(), alarm);


        setfullScreenAD();
       // getActivity().finish();

    }

    private void delete() {

        final Alarm alarm = getAlarm();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DeleteAlarmDialogTheme);
        builder.setTitle(R.string.delete_dialog_title);
        builder.setMessage(R.string.delete_dialog_content);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Cancel any pending notifications for this alarm
                AlarmReceiver.cancelReminderAlarm(getContext(), alarm);

                final int rowsDeleted = DatabaseHelper.getInstance(getContext()).deleteAlarm(alarm);
                int messageId;
                if(rowsDeleted == 1) {
                    messageId = R.string.delete_complete;
                   // Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();
                    LoadAlarmsService.launchLoadAlarmsService(getContext());
                    getActivity().finish();
                } else {
                    messageId = R.string.delete_failed;
                   // Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();

    }


    public void setBannerAD(final View v){
        FirebaseDatabase.getInstance().getReference().child("admob flag").child("banner").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("GK","DataSnapshot : banner id : "+dataSnapshot.getValue(String.class));

                mAdView = new AdView(getActivity());
                mAdView.setAdUnitId(dataSnapshot.getValue(String.class));
                mAdView.setAdSize(AdSize.SMART_BANNER);

                final LinearLayout adLinLay = (LinearLayout) v.findViewById(R.id.adlayout);
                adLinLay.addView(mAdView);


                AdRequest adRequest = new AdRequest.Builder()
                        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        // Check the LogCat to get your test device ID
                        .addTestDevice("FF88E90D3CCDA42AD566D477B7B63645")
                        .build();

                mAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {

                    }


                    @Override
                    public void onAdClosed() {

                       // Toast.makeText(getActivity(), "Ad is closed!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        mAdView.setVisibility(View.GONE);
                       // Toast.makeText(getActivity(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdLeftApplication() {
                        Toast.makeText(getActivity(), "Ad left application!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdOpened() {
                        super.onAdOpened();
                    }


                });

                mAdView.loadAd(adRequest);


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                mAdView.setVisibility(View.GONE);
                Log.d("GK","DataSnapshot : databaseError"+databaseError.getMessage());
            }
        });
    }


    //for add mob
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    //for add mob

    public void setfullScreenAD(){

        Toast.makeText(getActivity(),"Please wait for an ad",Toast.LENGTH_LONG).show();

        FirebaseDatabase.getInstance().getReference().child("admob flag").child("fullscreen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("GK","DataSnapshot : fullscreen ad id : "+dataSnapshot.getValue(String.class));

                mInterstitialAd = new InterstitialAd(getActivity());

                // set the ad unit ID
                mInterstitialAd.setAdUnitId(dataSnapshot.getValue(String.class));

                AdRequest adRequest = new AdRequest.Builder()
                        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        // Check the LogCat to get your test device ID
                        .addTestDevice("FF88E90D3CCDA42AD566D477B7B63645")

                        .build();

                // Load ads into Interstitial Ads
                mInterstitialAd.loadAd(adRequest);

                mInterstitialAd.setAdListener(new AdListener() {
                    public void onAdLoaded() {
                        showInterstitial();
                    }

                    @Override
                    public void onAdClosed() {
                        getActivity().finish();

                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        getActivity().finish();
                        //  Toast.makeText(getActivity(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdLeftApplication() {


                    }

                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                mAdView.setVisibility(View.GONE);

                Log.d("GK","DataSnapshot : databaseError"+databaseError.getMessage());
            }
        });


    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

}
