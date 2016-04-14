package top.lemonsoda.arsenalnews.view.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.sufficientlysecure.htmltextview.HtmlTextView;
import org.w3c.dom.Text;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import top.lemonsoda.arsenalnews.R;
import top.lemonsoda.arsenalnews.bean.NewDetail;
import top.lemonsoda.arsenalnews.net.NetworkManager;

public class ArticalActivity extends AppCompatActivity {

    private static final String TAG = ArticalActivity.class.getCanonicalName();

    private String mHeader;
    private String mArticalId;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ImageView mArticalImage;
    private TextView mArticalContent;
    private TextView mArticalSource;
    private TextView mArticalDate;
    private TextView mArticalEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artical);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (getIntent() != null) {
            mHeader = getIntent().getStringExtra("Header");
            mArticalId = getIntent().getStringExtra("ArticalId");
        }

        mArticalContent = (TextView)findViewById(R.id.tv_artical);
        mArticalSource = (TextView)findViewById(R.id.tv_artical_source);
        mArticalEditor = (TextView)findViewById(R.id.tv_artical_editor);
        mArticalDate = (TextView)findViewById(R.id.tv_artical_date);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar_layout);
        mArticalImage = (ImageView)findViewById(R.id.iv_artical_header);


        mCollapsingToolbarLayout.setTitle(mHeader);
        loadArtical(mArticalId);
    }


    private void loadArtical(String id){
        Log.d(TAG, "load artical" + id);
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
                                mArticalContent.setText(Html.fromHtml(newDetail.getContent(), new ImageGetter(mArticalContent, ArticalActivity.this), null));
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

    private class ImageGetter implements Html.ImageGetter{

        TextView container;
        Context context;
        int width;

        public ImageGetter(TextView t, Context c){
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

                            float scaleWidth = ((float)width)/resource.getWidth();
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

}
