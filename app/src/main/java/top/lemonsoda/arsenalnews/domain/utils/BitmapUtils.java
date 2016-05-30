package top.lemonsoda.arsenalnews.domain.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Chuan on 5/18/16.
 */
public class BitmapUtils {

    public static Bitmap getLocalBitmap(String file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            return BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
