package top.lemonsoda.arsenalnews.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import top.lemonsoda.arsenalnews.R;
import top.lemonsoda.arsenalnews.bean.User;
import top.lemonsoda.arsenalnews.domain.application.App;
import top.lemonsoda.arsenalnews.domain.preferences.AccessTokenKeeper;
import top.lemonsoda.arsenalnews.domain.preferences.UserInfoKeeper;
import top.lemonsoda.arsenalnews.domain.utils.BitmapSaver;
import top.lemonsoda.arsenalnews.domain.utils.Constants;
import top.lemonsoda.arsenalnews.net.WeiboNetworkManager;

public class LoginActivity extends AppCompatActivity {

    Button mWeiboLogin;
    Button mWeiboLogout;
    TextView mUserName;
    CircleImageView mAvatar;

    private AuthInfo mAuthInfo;
    private Oauth2AccessToken mAccessToken;
    private SsoHandler mSsoHandler;

    private LocalBroadcastManager mLocalBroadcastManager;
    private Subscriber<User> userSubscriber;

    private BitmapSaver mBitmapSaver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserName = (TextView) findViewById(R.id.tv_login_username);
        mAvatar = (CircleImageView) findViewById(R.id.img_login_avatar);
        mWeiboLogin = (Button) findViewById(R.id.btn_login_weibo);
        mWeiboLogout = (Button) findViewById(R.id.btn_logout_weibo);

        mWeiboLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSsoHandler.authorize(new AuthListener());
            }
        });

        mWeiboLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Constants.LOGIN_EVENT_INTENT_ACTION);
                intent.putExtra(Constants.INTENT_LOGIN_EXTRA_KEY, false);
                mLocalBroadcastManager.sendBroadcast(intent);

                App.getInstance().setUserLogin(false);
                AccessTokenKeeper.clear(getApplicationContext());
                UserInfoKeeper.clear(getApplicationContext());
                mAccessToken = new Oauth2AccessToken();
                updateLoginView(false);
            }
        });

        initSubscriber();

        mBitmapSaver = BitmapSaver.getInstance(this);
        mBitmapSaver.setFileName(Constants.AVATAR_NAME);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mSsoHandler = new SsoHandler(LoginActivity.this, mAuthInfo);

        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        updateLoginView(mAccessToken.isSessionValid());
    }

    private void initSubscriber() {
        userSubscriber = new Subscriber<User>() {
            @Override
            public void onCompleted() {
                Intent intent = new Intent(Constants.LOGIN_EVENT_INTENT_ACTION);
                intent.putExtra(Constants.INTENT_LOGIN_EXTRA_KEY, true);
                mLocalBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(
                        LoginActivity.this,
                        "Wrong in get user profile",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(User user) {
                UserInfoKeeper.writeUserInfo(LoginActivity.this, user);
                mUserName.setText(user.getScreen_name());
                Glide.with(LoginActivity.this)
                        .load(user.getProfile_image_url())
                        .asBitmap()
                        .centerCrop()
                        .into(target);
            }
        };
    }

    private void setupLoginView(boolean isLogin) {
        if (isLogin) {
            mWeiboLogout.setVisibility(View.VISIBLE);
            mUserName.setVisibility(View.VISIBLE);
            mAvatar.setVisibility(View.VISIBLE);
            mWeiboLogin.setVisibility(View.GONE);
        } else {
            mWeiboLogout.setVisibility(View.GONE);
            mUserName.setVisibility(View.GONE);
            mAvatar.setVisibility(View.GONE);
            mWeiboLogin.setVisibility(View.VISIBLE);
        }
    }

    private void updateLoginView(boolean isLogin) {
        setupLoginView(isLogin);
        if (!isLogin) {
            // Remove the avatar file
            mBitmapSaver.delete();
        } else {
            User user = UserInfoKeeper.readUserInfo(this);

            if (!TextUtils.isEmpty(user.getScreen_name()) && mBitmapSaver.exists()) {
                mUserName.setText(user.getScreen_name());
                mAvatar.setImageBitmap(mBitmapSaver.load());
            } else {
                final long uid = Long.parseLong(mAccessToken.getUid());
                WeiboNetworkManager.getInstance().getUser(
                        userSubscriber, uid, mAccessToken.getToken());
            }
        }
    }

    private SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
        @Override
        public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
            mAvatar.setImageBitmap(bitmap);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mBitmapSaver.save(bitmap);
                }
            }).start();
        }
    };


    /**
     * 当 SSO 授权 Activity 退出时，该函数被调用。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }

    }

    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     * 该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                updateLoginView(true);
                App.getInstance().setUserLogin(true);
                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(LoginActivity.this, mAccessToken);
                Toast.makeText(LoginActivity.this,
                        "success!!!", Toast.LENGTH_SHORT).show();
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = "Auth Failed.";
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(LoginActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(LoginActivity.this,
                    "Auth Cancel...", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
