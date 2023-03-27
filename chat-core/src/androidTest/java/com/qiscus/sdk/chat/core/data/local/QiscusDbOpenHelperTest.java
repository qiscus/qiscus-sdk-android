package com.qiscus.sdk.chat.core.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;


@RunWith(AndroidJUnit4ClassRunner.class)
public class QiscusDbOpenHelperTest extends InstrumentationBaseTest {

    private QiscusDbOpenHelper helper;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setupEngine();
        helper = new QiscusDbOpenHelper(application);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        helper.close();
    }

    /*@Test
    public void onCreateTest() {
        SQLiteDatabase sqLiteReadDatabase = helper.getReadableDatabase();
        helper.onCreate(sqLiteReadDatabase);

        SQLiteDatabase sqLiteWriteDatabase = helper.getWritableDatabase();
        helper.onCreate(sqLiteWriteDatabase);
    }*/

    @Test
    public void onOpenTest() {
        SQLiteDatabase sqLiteWriteDatabase = helper.getWritableDatabase();
        helper.onOpen(sqLiteWriteDatabase);
    }

    @Test
    public void onUpgradeTest() {
        SQLiteDatabase sqLiteWriteDatabase = helper.getWritableDatabase();
        helper.onUpgrade(sqLiteWriteDatabase, 1, 2);
    }

    @Test
    public void onUpgradeVersionAbove19Test() {
        SQLiteDatabase sqLiteWriteDatabase = helper.getWritableDatabase();
        helper.onUpgrade(sqLiteWriteDatabase, 20, 21);
    }

    private void readAndExecSQL(SQLiteDatabase sqLiteDb, String migrationName) {
        try {
            extractMethode(helper, "readAndExecSQL", SQLiteDatabase.class, Context.class, String.class)
                    .invoke(helper, sqLiteDb, application, migrationName);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // ignored
        }
    }

    @Test
    public void readAndExecSQLTest() {
        SQLiteDatabase sqLiteWriteDatabase = helper.getWritableDatabase();
        readAndExecSQL(sqLiteWriteDatabase, "qiscus.db_from_18_to_19.sql");
    }

    @Test
    public void readAndExecSQLMigrationNameEmptyTest() {
        SQLiteDatabase sqLiteWriteDatabase = helper.getWritableDatabase();
        readAndExecSQL(sqLiteWriteDatabase, "");
    }

    @Test
    public void execSQLScriptTest() {
        SQLiteDatabase sqLiteWriteDatabase = helper.getWritableDatabase();

        String test = "test;";
        Reader inputString = new StringReader(test);
        BufferedReader reader = new BufferedReader(inputString);
        execSQLScript(sqLiteWriteDatabase, reader);
    }

    private void execSQLScript(SQLiteDatabase sqLiteDb, BufferedReader reader) {
        try {
            extractMethode(helper, "execSQLScript", SQLiteDatabase.class, BufferedReader.class)
                    .invoke(helper, sqLiteDb, reader);
        } catch (IllegalAccessException | InvocationTargetException e) {
           // ignored
        }
    }

}