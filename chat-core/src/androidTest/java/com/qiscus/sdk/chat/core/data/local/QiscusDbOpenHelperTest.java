package com.qiscus.sdk.chat.core.data.local;

import static org.junit.Assert.*;

import android.database.sqlite.SQLiteDatabase;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class QiscusDbOpenHelperTest extends InstrumentationBaseTest {

    private QiscusDbOpenHelper helper;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();
        helper = new QiscusDbOpenHelper(application);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void onCreateTest() {
        SQLiteDatabase sqLiteReadDatabase = helper.getReadableDatabase();
        helper.onCreate(sqLiteReadDatabase);

        SQLiteDatabase sqLiteWriteDatabase = helper.getWritableDatabase();
        helper.onCreate(sqLiteWriteDatabase);
    }

    @Test
    public void onOpen() {
        SQLiteDatabase sqLiteWriteDatabase = helper.getWritableDatabase();
        helper.onOpen(sqLiteWriteDatabase);
    }

    @Test
    public void onUpgrade() {
        SQLiteDatabase sqLiteReadDatabase = helper.getReadableDatabase();
        helper.onUpgrade(sqLiteReadDatabase, 1, 2);
    }
}