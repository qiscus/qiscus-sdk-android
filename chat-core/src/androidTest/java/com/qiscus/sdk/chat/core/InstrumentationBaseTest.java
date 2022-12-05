package com.qiscus.sdk.chat.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class InstrumentationBaseTest {

    protected Context context;
    protected Application application;

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
}