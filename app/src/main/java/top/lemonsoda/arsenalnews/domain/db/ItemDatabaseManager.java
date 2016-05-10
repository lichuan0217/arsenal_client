package top.lemonsoda.arsenalnews.domain.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;

import top.lemonsoda.arsenalnews.bean.NewItem;

/**
 * Created by Chuan on 4/21/16.
 */
public class ItemDatabaseManager {

    private final SQLiteHelper sqLiteHelper;

    public ItemDatabaseManager(Context context){
        Log.d("DB", "create ItemDatabaseManager");
        sqLiteHelper = new SQLiteHelper(context);
    }

    public void saveItemsToDB(List<NewItem> newItems) {
        Log.d("DB", "Save Items to DB");
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        db.delete(sqLiteHelper.ITEM_TABLE_NAME, null, null);
        for (NewItem item: newItems) {
            saveItemToDB(item, db);
        }
        db.close();
    }

    public void saveItemToDB(NewItem newItem, SQLiteDatabase db){
        Log.d("DB", "store item: " + newItem.getHeader());

        ContentValues values = new ContentValues();
        values.put("id", newItem.getArticalId());
        values.put("header", newItem.getHeader());
        values.put("thumbnail", newItem.getThumbnail());
        values.put("source", newItem.getSource());
        values.put("content", newItem.getContent());
        values.put("url", newItem.getFullTextUrl());
        db.insertWithOnConflict(
                SQLiteHelper.ITEM_TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    public void getItemFromDB(List<NewItem> items) {
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + sqLiteHelper.ITEM_TABLE_NAME + "", null);
        while (cursor.moveToNext()) {
            NewItem item =  new NewItem();
            item.setArticalId(cursor.getString(cursor.getColumnIndex("id")));
            item.setHeader(cursor.getString(cursor.getColumnIndex("header")));
            item.setThumbnail(cursor.getString(cursor.getColumnIndex("thumbnail")));
            item.setSource(cursor.getString(cursor.getColumnIndex("source")));
            item.setContent(cursor.getString(cursor.getColumnIndex("content")));
            item.setFullTextUrl(cursor.getString(cursor.getColumnIndex("url")));
            items.add(item);
        }
        cursor.close();
        db.close();
    }
}
