package com.example.dimas.maps.view.windows;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.dimas.maps.R;
import com.example.dimas.maps.rest.type.PlaceType;

import java.util.HashSet;
import java.util.Set;

public class SettingsWindow extends AppCompatDialogFragment implements Settings {

    private RadioButton rb1;
    private RadioButton rb2;
    private TextView countKm;
    private SeekBar seekBar;
    private Switch switchWidget;
    private CheckBox checkBoxMonument;
    private CheckBox checkBoxNature;
    private CheckBox checkBoxView;
    private CheckBox checkBoxOther;
    private Button okButton;
//    private CheckBox checkBox;
    private String TAG = "sett_tag";

    private int count;

    private boolean rbChanger;
    private boolean switchChanger;
    private Set<PlaceType> checkedBoxes;
//    private boolean optChecked;

    public SettingsWindow(){}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_settings, container);

        countKm = view.findViewById(R.id.countKm);
        String text = String.valueOf(count) + " км";
        countKm.setText(text);

        seekBar = view.findViewById(R.id.seekBar);
        seekBar.setProgress(count);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                count = seekBar.getProgress();
                String text = String.valueOf(count) + " км";
                countKm.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        rb1 = view.findViewById(R.id.RB_1);
        rb2 = view.findViewById(R.id.RB_2);
        if (rbChanger)
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
                rbChanger = true;
            }
        });

        rb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countKm.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                rbChanger = false;
            }
        });

//        checkBox = view.findViewById(R.id.checkBox);
//        checkBox.setChecked(optChecked);

        switchWidget = view.findViewById(R.id.settingsSwitch);
        switchWidget.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchChanger = isChecked;
                setVisibleCheckBoxes();
            }
        });

        checkBoxMonument = view.findViewById(R.id.settingsCheckBoxMonument);
        checkBoxMonument.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkedBoxes.add(PlaceType.MONUMENT);
                } else {
                    checkedBoxes.remove(PlaceType.MONUMENT);
                }
            }
        });

        checkBoxNature = view.findViewById(R.id.settingsCheckBoxNature);
        checkBoxNature.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkedBoxes.add(PlaceType.NATURE);
                } else {
                    checkedBoxes.remove(PlaceType.NATURE);
                }
            }
        });

        checkBoxView = view.findViewById(R.id.settingsCheckBoxView);
        checkBoxView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkedBoxes.add(PlaceType.BEAUTIFUL_VIEW);
                } else {
                    checkedBoxes.remove(PlaceType.BEAUTIFUL_VIEW);
                }
            }
        });

        checkBoxOther = view.findViewById(R.id.settingsCheckBoxOther);
        checkBoxOther.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkedBoxes.add(PlaceType.OTHER);
                } else {
                    checkedBoxes.remove(PlaceType.OTHER);
                }
            }
        });

        setVisibleCheckBoxes();
        setChecks();

        okButton = view.findViewById(R.id.settingsOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        saveSettings();
    }

    @Override
    public void actSettings(boolean rbChanger, int count, boolean switchChanger, Set<PlaceType> checkedBoxes) {
        this.rbChanger = rbChanger;
        this.count = count;
        this.switchChanger = switchChanger;
        this.checkedBoxes = checkedBoxes != null ? checkedBoxes : new HashSet<PlaceType>();
    }

    private void setVisibleCheckBoxes() {
        if (switchChanger) {
            checkBoxMonument.setVisibility(View.VISIBLE);
            checkBoxNature.setVisibility(View.VISIBLE);
            checkBoxView.setVisibility(View.VISIBLE);
            checkBoxOther.setVisibility(View.VISIBLE);
        } else {
            checkBoxMonument.setVisibility(View.GONE);
            checkBoxNature.setVisibility(View.GONE);
            checkBoxView.setVisibility(View.GONE);
            checkBoxOther.setVisibility(View.GONE);
        }
    }

    private void setChecks() {
        switchWidget.setChecked(switchChanger);

        if (checkedBoxes.contains(PlaceType.MONUMENT)) {
            checkBoxMonument.setChecked(true);
        }
        if (checkedBoxes.contains(PlaceType.NATURE)) {
            checkBoxNature.setChecked(true);
        }
        if (checkedBoxes.contains(PlaceType.BEAUTIFUL_VIEW)) {
            checkBoxView.setChecked(true);
        }
        if (checkedBoxes.contains(PlaceType.OTHER)) {
            checkBoxOther.setChecked(true);
        }
    }

    private void saveSettings() {
        Settings activity = (Settings) getActivity();
        try {
            activity.actSettings(rbChanger, count, switchChanger, checkedBoxes);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
