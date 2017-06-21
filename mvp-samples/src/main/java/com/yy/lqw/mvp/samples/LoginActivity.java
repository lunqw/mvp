package com.yy.lqw.mvp.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.yy.lqw.mvp.MVPManager;
import com.yy.lqw.mvp.annotations.MVP;
import com.yy.lqw.mvp.annotations.MVPSink;

@MVP(presenters = {LoginPresenter.class})
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private LoginPresenter mLoginPresenter = new LoginPresenter();
    private EditText mPassport;
    private EditText mPassword;
    private TextView mErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MVPManager.bind(this, mLoginPresenter, getWindow().getDecorView());
        mPassport = (EditText) findViewById(R.id.et_passport);
        mPassword = (EditText) findViewById(R.id.et_password);
        mErrorText = (TextView) findViewById(R.id.tv_error_tips);
    }

    @Override
    public void onClick(View v) {
        final String passport = mPassport.getText().toString();
        final String password = mPassword.getText().toString();
        mLoginPresenter.login(passport, password);
    }

    @MVPSink
    void onLoginSuccess(String token) {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @MVPSink
    void onLoginFailed(int code, String message) {
        final String error = String.format("%d: %s", code, message);
        mErrorText.setText(error);
    }
}
