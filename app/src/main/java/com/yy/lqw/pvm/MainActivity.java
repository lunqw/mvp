package com.yy.lqw.pvm;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yy.lqw.pvm.annotations.PVM;
import com.yy.lqw.pvm.annotations.PVMSink;

@PVM(presenter = MainPresenter.class)
public class MainActivity extends AppCompatActivity {
    private MainPresenter mPresenter;
    private ImageView mPortraitView;
    private TextView mNickText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPortraitView = (ImageView) findViewById(R.id.iv_portrait);
        mNickText = (TextView) findViewById(R.id.tv_nick);
        mPresenter = (MainPresenter) PVManager.INSTANCE.bind(this);
    }

    public void onClick(View v) {
        mPresenter.fetchUserInfo(1001);
    }

    @PVMSink
    void onFetchUserInfo(long uid, Uri portraitUrl, String nick) {
        mNickText.setText(nick);
    }

    @PVMSink
    void onUserInfoChanged(long uid, Uri portraitUrl, String nick) {
        mNickText.setText(nick);
    }
}
