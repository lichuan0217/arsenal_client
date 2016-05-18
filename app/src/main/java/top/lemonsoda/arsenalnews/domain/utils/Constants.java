package top.lemonsoda.arsenalnews.domain.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by Chuan on 5/16/16.
 */
public class Constants {
    public static final String APP_KEY = "2163958370";
    public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    public static final String SCOPE =
            "email,direct_messages_read,direct_messages_write,"
                    + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
                    + "follow_app_official_microblog," + "invitation_write";

    public static final String STORAGE_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator
                    + "arsenalnews"
                    + File.separator
                    + "img"
                    + File.separator;

    public static final String AVATAR_NAME = "avatar.png";

    public static final String AVATAR_FILE = STORAGE_PATH + AVATAR_NAME;

}
