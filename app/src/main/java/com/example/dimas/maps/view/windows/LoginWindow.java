package com.example.dimas.maps.view.windows;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dimas.maps.R;
import com.example.dimas.maps.service.AuthService;

/**
 * Created by Dimas on 09.05.2020.
 */

public class LoginWindow extends AppCompatDialogFragment {

    private TextView mainTextView;
    private TextView errorTextView;
    private EditText editTextName;
    private EditText editTextPassword;
    private TextView modeTextView;
    private Button okButton;

    private enum Mode { LOGIN, REGISTER }
    private Mode currentMode = Mode.LOGIN;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_login, container);

        mainTextView = view.findViewById(R.id.loginTextViewMain);
        errorTextView = view.findViewById(R.id.loginTextViewError);
        errorTextView.setVisibility(View.GONE);

        editTextName = view.findViewById(R.id.loginEditTextName);
        editTextName.requestFocus();

        editTextPassword = view.findViewById(R.id.loginEditTextPassword);

        modeTextView = view.findViewById(R.id.loginTextViewMode);
        modeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMode == Mode.LOGIN) {
                    currentMode = Mode.REGISTER;

                    mainTextView.setText("Регистрация");
                    modeTextView.setText(R.string.loginText);
                    errorTextView.setVisibility(View.GONE);

                    editTextName.getText().clear();
                    editTextName.requestFocus();
                    editTextPassword.getText().clear();
                } else {
                    currentMode = Mode.LOGIN;

                    mainTextView.setText("Вход");
                    modeTextView.setText(R.string.registerText);
                    errorTextView.setVisibility(View.GONE);

                    editTextName.getText().clear();
                    editTextName.requestFocus();
                    editTextPassword.getText().clear();
                }
            }
        });

        okButton = view.findViewById(R.id.loginOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextName.getText().toString().length() == 0 || editTextPassword.getText().toString().length() == 0) {
                    errorTextView.setText(" Введите логин и пароль");
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }

                String response = currentMode == Mode.LOGIN ?
                        AuthService.signIn(editTextName.getText().toString(), editTextPassword.getText().toString()) :
                        AuthService.signUp(editTextName.getText().toString(), editTextPassword.getText().toString());

                switch (response) {
                    case "NOT_AUTHORIZED": {
                        errorTextView.setText(" Неверный логин или пароль");
                        editTextName.getText().clear();
                        editTextName.requestFocus();
                        editTextPassword.getText().clear();
                        errorTextView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case "USERNAME_IS_ALREADY_TAKEN": {
                        errorTextView.setText("Данный логин уже занят");
                        editTextName.getText().clear();
                        editTextName.requestFocus();
                        editTextPassword.getText().clear();
                        errorTextView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case "UNEXPECTED_ERROR": {
                        errorTextView.setText(" Неизвестная ошибка");
                        errorTextView.setVisibility(View.VISIBLE);
                        break;
                    }
                    default: {
                        Login activity = (Login) getActivity();
                        activity.addLogin(response, editTextName.getText().toString());
                        dismiss();
                        break;
                    }
                }


            }
        });
        return view;
    }
}
