package top.lemonsoda.arsenalnews.domain.application;

import android.app.Application;
import android.content.Context;

import cn.jpush.android.api.JPushInterface;
import top.lemonsoda.arsenalnews.bean.User;
import top.lemonsoda.arsenalnews.domain.preferences.UserInfoKeeper;

/**
 * Created by Chuan on 4/19/16.
 */
public class App extends Application {

    private static Context mContext;
    private boolean isUserLogin;
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        instance = this;
        JPushInterface.setDebugMode(true);    // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);            // 初始化 JPush

        initLogin();
    }

    public static Context getContext() {
        return mContext;
    }

    public static App getInstance(){
        return instance;
    }

    public boolean isUserLogin() {
        return isUserLogin;
    }

    public void setUserLogin(boolean userLogin) {
        isUserLogin = userLogin;
    }

    private void initLogin(){
        isUserLogin = false;
        User user = UserInfoKeeper.readUserInfo(this);
        if (user.getId() != 0) {
            isUserLogin = true;
        }
    }


}
