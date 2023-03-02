package com.qiscus.sdk.chat.core.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.google.android.gms.common.util.IOUtils;
import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class QiscusFileUtilTest extends InstrumentationBaseTest {

    private File file =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "ig_post-1-_nov__monthly_hiring_poster.png");
    private Uri uri = Uri.fromFile(file);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    @Override
    public void setupEngine() {
        super.setupEngine();
    }

    @Test
    public void fromNullTest() {
        try {
            QiscusFileUtil.from(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void fromTest() {
        try {
            QiscusFileUtil.from(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getFileNameTest() {
        QiscusFileUtil.getFileName(uri);
    }

    @Test
    public void commonTest() {
        QiscusFileUtil.getExtension(file.getName());
        QiscusFileUtil.getExtension(file);

        QiscusFileUtil.isContains(file.getPath());

        QiscusFileUtil.isImage(file.getName());
        QiscusFileUtil.isImage("ok.ok");
        QiscusFileUtil.isImage("image.png");

        QiscusFileUtil.getEnvironment(file.getName());
        QiscusFileUtil.getEnvironment("ok.ok");
        QiscusFileUtil.getEnvironment("image.png");
        QiscusFileUtil.getEnvironment("image.mp4");
        QiscusFileUtil.getEnvironment("image.mp3");

        QiscusFileUtil.generateFilePath("image.mp3", "mp3");

        QiscusFileUtil.rename(file, "newName.txt");
        QiscusFileUtil.rename(file, "sample3.txt");

        QiscusFileUtil.splitFileName("sample3.txt");

        QiscusFileUtil.getRealPathFromURI(uri);

        QiscusFileUtil.saveFile(file);
    }

    @Test
    public void fromSecondTest() {
        try {
            InputStream in = new FileInputStream(file);
            QiscusFileUtil.from(in, "sample3.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createTimestampFileNameTest() {
        QiscusFileUtil.createTimestampFileName("txt");
    }

    @Test
    public void notifySystemTest() {
        QiscusFileUtil.notifySystem(file);
    }

    @Test
    public void getThumbnailURLNullTest() {
        String url = null;
        QiscusFileUtil.getThumbnailURL(url);
    }

    @Test
    public void getThumbnailURLTest() {
        String url = "https://dnlbo7fgjcc7f.cloudfront.net/weapr-01wjzygbxjmeosf/image/upload/w_320,h_320,c_limit/adlRs_Uurx/image-(1).png";
        QiscusFileUtil.getThumbnailURL(url);
    }

    @Test
    public void getThumbnailURLBlurTest() {
        String url = "https://dnlbo7fgjcc7f.cloudfront.net/weapr-01wjzygbxjmeosf/image/upload/w_320,h_320,c_limit/adlRs_Uurx/image-(1).png";
        QiscusFileUtil.getBlurryThumbnailURL(url);
    }
}