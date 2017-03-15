package com.yy.lqw.pvm.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.yy.lqw.pvm.PVManager;
import com.yy.lqw.pvm.annotations.PVM;
import com.yy.lqw.pvm.annotations.PVMSink;

@PVM(presenter = LoginPresenter.class)
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    LoginPresenter mPresenter;
    private EditText mPassport;
    private EditText mPassword;
    private TextView mErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mPresenter = (LoginPresenter) PVManager.INSTANCE.bind(this,
                getWindow().getDecorView());
        mPassport = (EditText) findViewById(R.id.et_passport);
        mPassword = (EditText) findViewById(R.id.et_password);
        mErrorText = (TextView) findViewById(R.id.tv_error_tips);
    }

    @Override
    public void onClick(View v) {
        final String passport = mPassport.getText().toString();
        final String password = mPassword.getText().toString();
        mPresenter.login(passport, password);
    }

    @PVMSink
    void onLoginSuccess(String token) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @PVMSink
    void onLoginFailed(int code, String message) {
        final String error = String.format("%d: %s", code, message);
        mErrorText.setText(error);
    }
}
