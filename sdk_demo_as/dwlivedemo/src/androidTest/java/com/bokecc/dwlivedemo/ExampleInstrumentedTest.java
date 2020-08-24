package com.bokecc.dwlivedemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {


    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.bokecc.dwlivemoduledemo", appContext.getPackageName());
    }


    // 直播测试极速动画
    @Test
    public void testLive() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SharedPreferences preferences = appContext.getSharedPreferences("live_login_info", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("liveuid", "35BF2C2678E055D3");
        editor.putString("liveroomid", "8385201E0476D7659C33DC5901307461");
        editor.putString("liveusername", "test_dds");
        editor.putString("livepassword", "");
        editor.apply();
    }


    //  回放测试文档一直转的问题
    @Test
    public void testReplay() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SharedPreferences preferences = appContext.getSharedPreferences("live_login_info", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("replayuid", "8B9A47EE93296A8A");
        editor.putString("replayroomid", "4ADAC84FE1A6A3A39C33DC5901307461");
        editor.putString("replayliveid", "EA9D580452D92DF0");
        editor.putString("replayrecordid", "357DDD5832FA2E24");
        editor.putString("replayusername", "dds");
        editor.putString("replaypassword", "");
        editor.apply();
    }


    // 回放测试文档一直转的问题
    @Test
    public void testReplay1() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SharedPreferences preferences = appContext.getSharedPreferences("live_login_info", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("replayuid", "465B84A127085429");
        editor.putString("replayroomid", "4E488807F8FE91DC9C33DC5901307461");
        editor.putString("replayliveid", "CDED98B6B02E0713");
        editor.putString("replayrecordid", "7B02AABB572B2C3E");
        editor.putString("replayusername", "13000000000");
        editor.putString("replaypassword", "000000");
        editor.apply();
    }
}