package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import android.content.Intent;
import android.os.Bundle;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusContactTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();
    }

    @Test
    public void contactTest(){
        QiscusContact contact = new QiscusContact("name","value", "type");

        contact.setName("new name");
        contact.setValue("new value");
        contact.setType("new type");
        contact.getType();
        contact.toString();
        contact.describeContents();

    }
}