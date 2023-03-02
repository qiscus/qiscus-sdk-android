package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class QiscusPhotoTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();
    }

    @Test
    public void photoTest(){
        QiscusPhoto qiscusPhoto = new QiscusPhoto(new File("/storage/emulated/0/Android/data/com.qiscus.dragonfly/files/Pictures/DragonFly/DragonFly Images/IMG-20230214-WA0000.jpg"));
        qiscusPhoto.getPhotoFile();

        QiscusPhoto qiscusPhoto2 = new QiscusPhoto(new File("/storage/emulated/0/Android/data/com.qiscus.dragonfly/files/Pictures/DragonFly/DragonFly Images/IMG-20230214-WA0000.jpg"), false);
        qiscusPhoto2.getPhotoFile();
        qiscusPhoto2.setPhotoFile(new File("/storage/emulated/0/Android/data/com.qiscus.dragonfly/files/Pictures/DragonFly/DragonFly Images/IMG-20230214-WA0000.jpg"));
        qiscusPhoto2.isSelected();
        qiscusPhoto2.setSelected(false);
        qiscusPhoto2.describeContents();
    }

}