package com.yy.lqw.pvm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yy.lqw.pvm.annotations.UIEventSink;

@UIEventSink
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
