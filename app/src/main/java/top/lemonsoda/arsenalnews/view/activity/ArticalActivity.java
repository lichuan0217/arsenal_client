package top.lemonsoda.arsenalnews.view.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import top.lemonsoda.arsenalnews.R;
import top.lemonsoda.arsenalnews.bean.NewDetail;
import top.lemonsoda.arsenalnews.bean.RequestFavorite;
import top.lemonsoda.arsenalnews.bean.RequestUser;
import top.lemonsoda.arsenalnews.bean.ResponseFavorite;
import top.lemonsoda.arsenalnews.bean.User;
import top.lemonsoda.arsenalnews.domain.application.App;
import top.lemonsoda.arsenalnews.domain.preferences.UserInfoKeeper;
import top.lemonsoda.arsenalnews.domain.utils.Constants;
import top.lemonsoda.arsenalnews.domain.utils.ShareUtils;
import top.lemonsoda.arsenalnews.net.NetworkManager;

public class ArticalActivity extends AppCompatActivity {

    private static final String TAG = ArticalActivity.class.getCanonicalName();

    private String mHeader;
    private String mArticleId;

    private net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout mMultiLineToolbarLayout;
    private ImageView mArticalImage;
    private TextView mArticalContent;
    private TextView mArticalSource;
    private TextView mArticalDate;
    private TextView mArticalEditor;


    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artical);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setImageResource(R.mipmap.ic_favorite_white);
                Snackbar.make(view, R.string.favorite_prompt, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                User user = UserInfoKeeper.readUserInfo(ArticalActivity.this);
                final RequestFavorite requestFavorite = new RequestFavorite();
                requestFavorite.setUser_id(user.getId()+"");
                requestFavorite.setArticle_id(mArticleId);
                NetworkManager.newsItemService.postFavorite(requestFavorite)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<ResponseFavorite>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(ResponseFavorite responseFavorite) {
                                if (responseFavorite.getResponse_code() == 201){
                                    Log.d(TAG, "success");
                                } else if (responseFavorite.getResponse_code() >= 400) {
                                    Log.d(TAG, "wrong");
                                }
                            }
                        });
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (getIntent() != null) {
            mHeader = getIntent().getStringExtra(Constants.INTENT_EXTRA_HEADER);
            mArticleId = getIntent().getStringExtra(Constants.INTENT_EXTRA_ARTICLE_ID);
        }

        mArticalSource = (TextView) findViewById(R.id.tv_artical_source);
        mArticalEditor = (TextView) findViewById(R.id.tv_artical_editor);
        mArticalDate = (TextView) findViewById(R.id.tv_artical_date);
        mMultiLineToolbarLayout =
                (net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout)
                        findViewById(R.id.collapsing_toolbar_layout);
        mArticalImage = (ImageView) findViewById(R.id.iv_artical_header);

        mWebView = (WebView) findViewById(R.id.wv_artical);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        webSettings.setUseWideViewPort(true);
//        if (PrefUtils.isEnableCache()) {
//            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
//            webSettings.setAppCacheEnabled(true);
//            webSettings.setDatabaseEnabled(true);
//        }
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDefaultTextEncodingName("utf-8");


        mMultiLineToolbarLayout.setTitle(mHeader);
        loadArticle(mArticleId);
    }


    private void loadArticle(String id) {
        Log.d(TAG, "load article" + id);
        if (App.getInstance().isUserLogin()){
            User user = UserInfoKeeper.readUserInfo(this);
            RequestUser requestUser = new RequestUser();
            requestUser.setUser_id(user.getId()+"");
            Log.d(TAG, "User Login. ID: " + user.getId());
            NetworkManager.newsItemService.getArticleWithUserId(id, requestUser)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Action1<NewDetail>() {
                                @Override
                                public void call(NewDetail newDetail) {
                                    Log.d(TAG, newDetail.getPicture_src());
                                    Log.d(TAG, newDetail.getContent());
                                    Log.d(TAG, "type: " + newDetail.getType());
                                    Log.d(TAG, "video: " + newDetail.getVideo());
                                    Log.d(TAG, "favorite: " + newDetail.getFavorite());
                                    mWebView.loadData(buildHtmlContent(newDetail.getContent()), "text/html; charset=uft-8", "utf-8");
                                    mArticalSource.setText("来源 " + newDetail.getSource());
                                    mArticalDate.setText(newDetail.getDate());
                                    mArticalEditor.setText(newDetail.getEditor());
                                    Glide.with(ArticalActivity.this)
                                            .load(newDetail.getPicture_src())
                                            .centerCrop()
                                            .thumbnail(0.5f)
                                            .into(mArticalImage);
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    Toast.makeText(
                                            ArticalActivity.this,
                                            R.string.info_news_list_load_error,
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                    );
        } else {
            Log.d(TAG, "User Not Login");
            NetworkManager.newsItemService.getArtical(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Action1<NewDetail>() {
                                @Override
                                public void call(NewDetail newDetail) {
                                    Log.d(TAG, newDetail.getPicture_src());
                                    Log.d(TAG, newDetail.getContent());
                                    Log.d(TAG, "type: " + newDetail.getType());
                                    Log.d(TAG, "video: " + newDetail.getVideo());
                                    Log.d(TAG, "favorite: " + newDetail.getFavorite());
                                    mWebView.loadData(buildHtmlContent(newDetail.getContent()), "text/html; charset=uft-8", "utf-8");
                                    mArticalSource.setText("来源 " + newDetail.getSource());
                                    mArticalDate.setText(newDetail.getDate());
                                    mArticalEditor.setText(newDetail.getEditor());
                                    Glide.with(ArticalActivity.this)
                                            .load(newDetail.getPicture_src())
                                            .centerCrop()
                                            .thumbnail(0.5f)
                                            .into(mArticalImage);
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    Toast.makeText(
                                            ArticalActivity.this,
                                            R.string.info_news_list_load_error,
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                    );
        }

    }

    private class ImageGetter implements Html.ImageGetter {

        TextView container;
        Context context;
        int width;

        public ImageGetter(TextView t, Context c) {
            container = t;
            context = c;
            width = c.getResources().getDisplayMetrics().widthPixels;
        }

        @Override
        public Drawable getDrawable(String source) {
            Log.d(TAG, "url: " + source);

            final URLDrawable urlDrawable = new URLDrawable();
//            Drawable drawable = getResources().getDrawable(R.mipmap.placeholder);
//            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//            urlDrawable.drawable = drawable;

            Glide.with(ArticalActivity.this)
                    .load(source)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                            float scaleWidth = ((float) width) / resource.getWidth();
                            Matrix matrix = new Matrix();
                            matrix.postScale(scaleWidth, scaleWidth);
                            resource = Bitmap.createBitmap(resource, 0, 0, resource.getWidth(), resource.getHeight(), matrix, true);
                            urlDrawable.bitmap = resource;
                            urlDrawable.setBounds(0, 0, resource.getWidth(), resource.getHeight());

                            container.invalidate();
                            container.setText(container.getText());
                        }
                    });


            return urlDrawable;
        }
    }

    private class URLDrawable extends BitmapDrawable {
        protected Bitmap bitmap;

        @Override
        public void draw(Canvas canvas) {
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, 0, 0, getPaint());
            }
        }
    }


    private String buildHtmlContent(String content) {
        String head = "<head>\n" +
                "<meta charset=\"utf-8\">\n" +
                "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\n" +
                "<title>" + mHeader + "</title>\n" +
                "<meta name=\"viewport\" content=\"user-scalable=no, width=device-width\">\n" +
                "<style type=\"text/css\">" +
                "img{" +
                "display: inline;" +
                "max-width:100%;" +
                "height: auto" +
                "}" +
                "</style>\n" +
                "<base target=\"_blank\">\n" +
                "</head>";
        String bodyStart = "<body>";
        String bodyEnd = "</body>";

        return head + bodyStart + content.replaceAll("<div class=\"img-place-holder\"></div>", "") + bodyEnd;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_share:
                ShareUtils.share(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
