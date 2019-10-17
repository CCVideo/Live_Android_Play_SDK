package com.bokecc.dwlivemoduledemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;


public class TestActivity extends AppCompatActivity {
    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        listView = findViewById(R.id.listview);

////        //构建传递参数
//        Bundle bundle = new Bundle();
//        //绑定主内容编辑框
//        bundle.putBoolean(EmotionMainFragment.BIND_TO_EDITTEXT, true);
//        //隐藏控件
//        bundle.putBoolean(EmotionMainFragment.HIDE_BAR_EDITTEXT_AND_BTN, false);
//
//        emotionMainFragment = EmotionMainFragment.newInstance(EmotionMainFragment.class, bundle);
//        emotionMainFragment.bindToContentView(listView);
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.layout_emotion, emotionMainFragment);
//        transaction.addToBackStack(null);
//        //提交修改
//        transaction.commit();
    }
}
