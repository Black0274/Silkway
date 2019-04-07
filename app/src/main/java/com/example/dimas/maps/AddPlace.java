package com.example.dimas.maps;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddPlace extends AppCompatDialogFragment /*implements TextView.OnEditorActionListener*/{

    private TextView textViewName;
    private TextView textViewDescription;
    private EditText editTextName;
    private EditText editTextDescription;
    private Button okButton;
    private String TAG = "add_place_tag";

    public AddPlace(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_add_place, container);
        //getDialog().setTitle("Введите данные");

        editTextName = view.findViewById(R.id.editTextName);
        editTextName.requestFocus();

        /*try {
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }*/

        editTextDescription = view.findViewById(R.id.editTextDescription);
        okButton = view.findViewById(R.id.OK_Button);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPlaceInterface activity = (AddPlaceInterface) getActivity();
                activity.addPlaceText(editTextName.getText().toString(), editTextDescription.getText().toString());
                dismiss();
            }
        });

        return view;
    }

/*    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId){

            return true;
        }

        return false;
    }*/
}
