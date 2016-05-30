package top.lemonsoda.arsenalnews.domain.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import top.lemonsoda.arsenalnews.bean.User;

/**
 * Created by Chuan on 5/17/16.
 */
public class UserInfoKeeper {

    private static final String PREFERENCES_NAME = "pref_user";

    private static final String KEY_UID = "uid";
    private static final String KEY_USER_SCREEN_NAME = "screen_name";
    private static final String KEY_USER_AVATAR_URL = "avatar_url";

    /**
     * 保存 User 对象到 SharedPreferences。
     *
     * @param context 应用程序上下文环境
     * @param user    User 对象
     */
    public static void writeUserInfo(Context context, User user) {
        if (null == context || null == user) {
            return;
        }

        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(KEY_UID, user.getId());
        editor.putString(KEY_USER_SCREEN_NAME, user.getScreen_name());
        editor.putString(KEY_USER_AVATAR_URL, user.getProfile_image_url());
        editor.commit();
    }


    /**
     * 从 SharedPreferences 读取 User 信息。
     *
     * @param context 应用程序上下文环境
     * @return 返回 Token 对象
     */
    public static User readUserInfo(Context context) {
        if (null == context) {
            return null;
        }

        User user = new User();
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        user.setId(pref.getLong(KEY_UID, 0));
        user.setScreen_name(pref.getString(KEY_USER_SCREEN_NAME, ""));
        user.setProfile_image_url(pref.getString(KEY_USER_AVATAR_URL, ""));
        return user;
    }


    /**
     * 清空 SharedPreferences 中 User信息。
     *
     * @param context 应用程序上下文环境
     */
    public static void clear(Context context) {
        if (null == context) {
            return;
        }

        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();

    }
}
