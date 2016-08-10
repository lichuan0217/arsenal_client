package top.lemonsoda.arsenalnews.domain.utils;

/**
 * Created by Chuan on 6/15/16.
 */
public class Utils {


    public static String getImageFromThumbnailUrl(String thumbnail) {
        //http://c2.hoopchina.com.cn/uploads/star/event/images/160613/thumbnail-2b8555950f110a3da73ee28f914a0a184ac0bdfc.png
        String[] array = thumbnail.split("thumbnail-");
        StringBuilder sb = new StringBuilder();
        for (String str : array) {
            sb.append(str);
        }
        if (array == null || array.length == 0)
            return thumbnail;
        else
            return sb.toString();
    }
}
