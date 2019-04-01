package com.example.dimas.maps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Dimas on 31.03.2019.
 */

public class Settings extends AppCompatDialogFragment {

    private RadioButton rb1;
    private RadioButton rb2;
    private TextView countKm;
    private SeekBar seekBar;

    public Settings(){}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup, container);
        rb1 = (RadioButton) view.findViewById(R.id.RB_1);
        rb2 = (RadioButton) view.findViewById(R.id.RB_2);
        countKm = (TextView) view.findViewById(R.id.countKm);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);

        rb1.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       countKm.setVisibility(View.GONE);
                                       seekBar.setVisibility(View.GONE);
                                   }
                               }

        );

        rb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countKm.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.VISIBLE);
            }
        }
        );

        return view;
    }

    /*@NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstantState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup, null);
        builder.setView(view);

        return builder.create();
    }*/
}
