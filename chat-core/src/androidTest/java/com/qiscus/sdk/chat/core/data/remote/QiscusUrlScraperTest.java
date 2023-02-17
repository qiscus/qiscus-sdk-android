package com.qiscus.sdk.chat.core.data.remote;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QiscusUrlScraperTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();

    }

    @Test
    public void generatePreviewData(){
        QiscusUrlScraper.getInstance()
                .generatePreviewData("https://qiscus.com")
                .doOnNext(previewData -> previewData.setUrl("https://qiscus.com"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(previewData -> {

                }, Throwable::printStackTrace);
    }
}