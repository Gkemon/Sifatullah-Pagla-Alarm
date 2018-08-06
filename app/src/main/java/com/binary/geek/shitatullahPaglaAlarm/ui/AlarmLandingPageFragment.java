package com.binary.geek.shitatullahPaglaAlarm.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.VideoView;

import com.binary.geek.shitatullahPaglaAlarm.R;

public final class AlarmLandingPageFragment extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_alarm_landing_page, container, false);

        final Button launchMainActivityBtn = (Button) v.findViewById(R.id.load_main_activity_btn);
        final Button dismiss = (Button) v.findViewById(R.id.dismiss_btn);

        launchMainActivityBtn.setOnClickListener(this);
        dismiss.setOnClickListener(this);


        VideoView view = (VideoView)v.findViewById(R.id.video_player);
        String path = "android.resource://" +"com.binary.geek.shitatullahPaglaAlarm" + "/" + R.raw.videoplayback;
        view.setVideoURI(Uri.parse(path));
        view.start();


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
                getActivity().finish();
                break;
        }

    }
}