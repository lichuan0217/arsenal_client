package top.lemonsoda.arsenalnews.domain.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import top.lemonsoda.arsenalnews.domain.application.App;

/**
 * Created by Chuan on 4/21/16.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    public static int VERSION = 1;
    public static String DATABASE_NAME = "arsenal.db";
    public static String ITEM_TABLE_NAME = "item";

    public static String CREATE_NEWS_ITEM_TABLE = "create table "
            + ITEM_TABLE_NAME
            + " (id text primary key,"
            + " header text,"
            + " thumbnail text,"
            + " source text,"
            + " content text,"
            + " url text)";

    public SQLiteHelper() {
        super(App.getContext(), DATABASE_NAME, null, VERSION);
    }

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DB", "Create Database");
        db.execSQL(CREATE_NEWS_ITEM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + ITEM_TABLE_NAME);
        onCreate(db);
    }
}
