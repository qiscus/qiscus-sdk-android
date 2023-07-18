package com.qiscus.sdk.chat.core.util;

import static org.junit.Assert.*;

import android.util.Log;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.qiscus.sdk.chat.core.data.model.urlsextractor.PreviewData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import rx.Observable;
import rx.schedulers.Schedulers;

@RunWith(AndroidJUnit4ClassRunner.class)
public class UrlsExtractorUtilsTest {

    private UrlsExtractorUtils extractor;

    @Before
    public void setUp() throws Exception {
        extractor = new UrlsExtractorUtils.Builder()
                .scheduler(Schedulers.io())
                .build();
    }

    @After
    public void tearDown() throws Exception {
        extractor = null;
    }

    private void generatePreview(String url, Type type) {
        final Observable<PreviewData> observable = extractor.generatePreview(url);

        if (type == Type.SUCCESS) {
            assertNotNull(
                    observable.toBlocking().single()
            );
        } else if (type == Type.EMPTY) {
            assertTrue(
                    observable.isEmpty().toBlocking().single()
            );
        } else {
            assertNull(
                    observable.onErrorReturn(null).toBlocking().single()
            );
        }

        observable.subscribe(
                previewData -> print(previewData.toString()),
                throwable -> print(throwable.getMessage()),
                () -> print(type.toString())
        );
    }

    @Test
    public void generatePreviewEmptyTest() {
        String url = "";
        Type type = Type.ERROR;

        generatePreview(url, type);
    }

    @Test
    public void generatePreviewHtmlTest() {
        String url = "https://www.multichannel.com";
        Type type = Type.SUCCESS;

        generatePreview(url, type);
    }

    @Test
    public void generatePreviewHtmlEmptyTest() {
        String url = "https://www.tidakpernahada-harusnya.com";
        Type type = Type.ERROR;

        generatePreview(url, type);
    }

    @Test
    public void generatePreviewImageTest() {
        String url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSlB6dfZVkKvk3HXFAo9zGp2rMFh7gmQL8m6JRdl03W_76v4AkyLy4P4xxFbLDC6p4XUko&usqp=CAU";
        Type type = Type.SUCCESS;

        generatePreview(url, type);
    }

    @Test
    public void generatePreviewDocTest() {
        String url = "https://filesamples.com/samples/document/txt/sample3.txt";
        Type type = Type.EMPTY;

        generatePreview(url, type);
    }

    @Test
    public void generatePreviewVideoTest() {
        String url = "https://dnlbo7fgjcc7f.cloudfront.net/weapr-01wjzygbxjmeosf/video/upload/683YWsiYHR/A297_Nuraniah_QP901-b(1).mp4";
        Type type = Type.EMPTY;

        generatePreview(url, type);
    }

    void print(String message) {
        Log.d("testResult", message);
    }

    enum Type {
        SUCCESS, ERROR, EMPTY
    }
}
