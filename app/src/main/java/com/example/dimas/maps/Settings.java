package com.example.dimas.maps;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Dimas on 31.03.2019.
 */

public class Settings extends AppCompatDialogFragment implements SettingsInterface {

    private RadioButton rb1;
    private RadioButton rb2;
    private TextView countKm;
    private SeekBar seekBar;
    private Button okButton;
    private int count;
    private String TAG = "sett_tag";
    private boolean rb_changer = true;

    public Settings(){}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup, container);

        countKm = (TextView) view.findViewById(R.id.countKm);
        String text = String.valueOf(count) + " км";
        countKm.setText(text);

        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setProgress(count);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                count = seekBar.getProgress();
                String text = String.valueOf(count) + " км";
                countKm.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        rb1 = (RadioButton) view.findViewById(R.id.RB_1);
        rb2 = (RadioButton) view.findViewById(R.id.RB_2);
        if (rb_changer)
            rb1.setChecked(true);
        else {
            rb2.setChecked(true);
            countKm.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
        }

        rb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countKm.setVisibility(View.GONE);
                seekBar.setVisibility(View.GONE);
                rb_changer = true;
            }
        });

        rb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countKm.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                rb_changer = false;
            }
        });


        okButton = (Button) view.findViewById(R.id.OK_Button);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsInterface activity = (SettingsInterface) getActivity();
                try {
                    activity.stopSettings(rb_changer, count);
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }
                dismiss();
            }
        });

        return view;
    }


    @Override
    public void stopSettings(boolean rb_changerI, int countI) {
        rb_changer = rb_changerI;
        count = countI;
    }
}
