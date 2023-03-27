package com.qiscus.sdk.chat.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class InstrumentationBaseTest {

    protected Context context;
    protected Application application;
    protected Integer roomId = 10185397;
    protected String roomUniqId = "8d412fdd3411f5f261f8f30e0f90ff60";

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        application = (Application) context.getApplicationContext();
    }

    @After
    public void tearDown() throws Exception {
        context = null;
        application = null;
    }

    public Activity getActivity(ActivityScenarioRule<?> scenarioRule) {
        AtomicReference<Activity> resultActivity = new AtomicReference<>();
        scenarioRule.getScenario().onActivity(resultActivity::set);
        return resultActivity.get();
    }

    public void setupEngine() {
        QiscusCore.setup(application, "sdksample");
        QiscusCore.setUser("arief92", "arief92")
                .withUsername("arief92")
                .withAvatarUrl("https://")
                .withExtras(null)
                .save(new QiscusCore.SetUserListener() {
                    @Override
                    public void onSuccess(QiscusAccount qiscusAccount) {
                        //on success
                        QiscusCore.updateUser("testing", "https://", new JSONObject());

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        //on error
                    }
                });

        QiscusApi.getInstance().reInitiateInstance();
        QiscusCore.getChatConfig().enableDebugMode(false);
    }


    public Field extractField(Object o, String name) {
        Field f = null; //NoSuchFieldException
        List<Integer> viewTypes = new ArrayList<>();
        try {
            f = o.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Method extractMethode(Object o, String name, Class<?>... parameterTypes) {
        try {
            Method m = o.getClass().getDeclaredMethod(name, parameterTypes);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}