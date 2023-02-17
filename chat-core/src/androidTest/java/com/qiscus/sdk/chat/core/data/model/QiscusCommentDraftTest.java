package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusCommentDraftTest extends InstrumentationBaseTest {
    QiscusCommentDraft draft ;
    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();
        draft = new QiscusCommentDraft("test");
    }

    @Test
    public void getMessage(){
        draft.getMessage();
    }

    @Test
    public void testToString(){
        draft.toString();
    }


}