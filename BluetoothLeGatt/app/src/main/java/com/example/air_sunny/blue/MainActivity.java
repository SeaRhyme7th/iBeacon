package com.example.air_sunny.blue;

/**
 * Created by LeLe on 2016/11/6.
 */

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;

import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    ImageButton locateButton;
    ImageButton helpButton;
    ImageButton moreButton;
    ImageButton aboutButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //国际化
        locale();
        //定位 按钮
        addLocateButton();
        //帮助 按钮
        addHelpButton();
        //详细信息 按钮
        addMoreButton();
        //关于我们 按钮
        addAboutButton();
    }

    private void locale() {
        Resources resources = getResources();//获得res资源对象
        Configuration config = resources.getConfiguration();//获得设置对象
        DisplayMetrics dm = resources.getDisplayMetrics();//获得屏幕参数：主要是分辨率，像素等。
        config.locale = Locale.ENGLISH; //美式英语
        resources.updateConfiguration(config, dm);
    }

    private void addLocateButton() {
        locateButton = (ImageButton) findViewById(R.id.locateButton);
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LocateActivity.class);

                startActivity(intent);
            }
        });
    }

    private void addHelpButton() {
        helpButton = (ImageButton) findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addMoreButton() {
        moreButton = (ImageButton) findViewById(R.id.moreButton);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeviceScanActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addAboutButton() {
        aboutButton = (ImageButton) findViewById(R.id.aboutButton);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }

}
