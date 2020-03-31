package com.example.air_sunny.blue;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class AboutActivity extends SwipeBackActivity {

    //使用SwipeBackActivity达到右划返回的功能
    private SwipeBackLayout mSwipeBackLayout;

    //About us 数据
    private String[] data = {"ABOUT US", "PROGRAMMER", "Yu.Jiang", "Haorui.Li", "Junkun.Jiang"
            , "UI DESIGN", "Junkun.Jiang", "Other", "Junkun.Jiang"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        //添加关于我们 名单
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                AboutActivity.this, R.layout.about_us, data);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        //右划返回
        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
    }
}
