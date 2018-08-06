package com.binary.geek.shitatullahPaglaAlarm.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.binary.geek.shitatullahPaglaAlarm.R;
import com.binary.geek.shitatullahPaglaAlarm.adapter.AlarmsAdapter;
import com.binary.geek.shitatullahPaglaAlarm.model.Alarm;
import com.binary.geek.shitatullahPaglaAlarm.service.LoadAlarmsReceiver;
import com.binary.geek.shitatullahPaglaAlarm.service.LoadAlarmsService;
import com.binary.geek.shitatullahPaglaAlarm.util.AlarmUtils;
import com.binary.geek.shitatullahPaglaAlarm.view.DividerItemDecoration;
import com.binary.geek.shitatullahPaglaAlarm.view.EmptyRecyclerView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public final class MainFragment extends Fragment
        implements LoadAlarmsReceiver.OnAlarmsLoadedListener {
    public static FirebaseDatabase firebaseDatabase;
    public static DatabaseReference databaseReference;

    private LoadAlarmsReceiver mReceiver;
    private AlarmsAdapter mAdapter;
    private AdView mAdView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new LoadAlarmsReceiver(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_main, container, false);

        final EmptyRecyclerView rv = (EmptyRecyclerView) v.findViewById(R.id.recycler);
        mAdapter = new AlarmsAdapter();
        rv.setEmptyView(v.findViewById(R.id.empty_view));
        rv.setAdapter(mAdapter);
        rv.addItemDecoration(new DividerItemDecoration(getContext()));
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setItemAnimator(new DefaultItemAnimator());



        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();


        setPublisherID();
        setBannerAD(v);



        final FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                final Intent intentForOpenLandingPage = new Intent(getContext(), AlarmLandingPageActivity.class);
//                intentForOpenLandingPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                getContext().startActivity(intentForOpenLandingPage);


                AlarmUtils.checkAlarmPermissions(getActivity());
                final Intent i =
                        AddEditAlarmActivity.buildAddEditAlarmActivityIntent(
                                getContext(), AddEditAlarmActivity.ADD_ALARM
                        );
                startActivity(i);



            }
        });

        return v;

    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(LoadAlarmsService.ACTION_COMPLETE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, filter);
        LoadAlarmsService.launchLoadAlarmsService(getContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onAlarmsLoaded(ArrayList<Alarm> alarms) {
        mAdapter.setAlarms(alarms);
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


    public void setPublisherID(){
        databaseReference.child("admob flag").child("publisher id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("GK","DataSnapshot : publisher id : "+dataSnapshot.getValue(String.class));
                MobileAds.initialize(getContext(), dataSnapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d("GK","DataSnapshot : databaseError in publisher id :"+databaseError.getMessage());
            }
        });

    }
    public void setBannerAD(final View v){
        databaseReference.child("admob flag").child("banner").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("GK","DataSnapshot : banner id : "+dataSnapshot.getValue(String.class));

                mAdView = (AdView) v.findViewById(R.id.adView);
                mAdView.setVisibility(View.VISIBLE);

                mAdView.setAdUnitId(dataSnapshot.getValue(String.class));
                mAdView.setAdSize(AdSize.SMART_BANNER);

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
                        Toast.makeText(getContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        Toast.makeText(getContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdLeftApplication() {
                        Toast.makeText(getContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
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
}


