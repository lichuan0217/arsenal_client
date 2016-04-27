package top.lemonsoda.arsenalnews.domain.db;

import android.content.ContentValues;
import android.content.Context;
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
        for (NewItem item: newItems) {
            saveItemToDB(item);
        }
    }

    public void saveItemToDB(NewItem newItem){
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
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
        db.close();
    }
}
