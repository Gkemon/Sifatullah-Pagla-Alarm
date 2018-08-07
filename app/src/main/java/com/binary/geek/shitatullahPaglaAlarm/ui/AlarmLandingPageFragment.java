package com.binary.geek.shitatullahPaglaAlarm.ui;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.binary.geek.shitatullahPaglaAlarm.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public final class AlarmLandingPageFragment extends Fragment implements View.OnClickListener {

    private AdView mAdView;
    InterstitialAd mInterstitialAd;
    boolean canIshowAd=false;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_alarm_landing_page, container, false);

        final Button launchMainActivityBtn = (Button) v.findViewById(R.id.load_main_activity_btn);
        final Button dismiss = (Button) v.findViewById(R.id.dismiss_btn);

        launchMainActivityBtn.setOnClickListener(this);
        dismiss.setOnClickListener(this);

        checkAdActiveOrNot(v);


        VideoView view = (VideoView)v.findViewById(R.id.video_player);
        String path = "android.resource://" +"com.binary.geek.shitatullahPaglaAlarm" + "/" + R.raw.videoplayback;
        view.setVideoURI(Uri.parse(path));
        view.start();

        view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });


        return v;
    }



    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.load_main_activity_btn:
                startActivity(new Intent(getContext(), MainActivity.class));
                getActivity().finish();
                break;
            case R.id.dismiss_btn:

                if(canIshowAd)
                setfullScreenAD();
                break;
        }

    }

    public void setfullScreenAD(){



        Toast.makeText(getActivity(),"Please wait for an ad",Toast.LENGTH_LONG).show();

        FirebaseDatabase.getInstance().getReference().child("admob flag").child("fullscreen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("GK","DataSnapshot : fullscreen ad id : "+dataSnapshot.getValue(String.class));


                Context context=getActivity();
                try {
                    context=getContext();
                }catch (Exception e){
                    try {
                        context=AddEditAlarmActivity.context ;
                    }
                    catch (Exception a){
                    }
                }

                mInterstitialAd = new InterstitialAd(context);

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

    public void checkAdActiveOrNot(final View view){
        FirebaseDatabase.getInstance().getReference().child("admob flag").child("show ad").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("GK","DataSnapshot : banner id : "+dataSnapshot.getValue(Boolean.class));

                    canIshowAd=dataSnapshot.getValue(Boolean.class);
                    if(dataSnapshot.getValue(Boolean.class))setBannerAD(view);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

                canIshowAd=false;
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
}
