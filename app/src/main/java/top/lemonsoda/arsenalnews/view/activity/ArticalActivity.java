package top.lemonsoda.arsenalnews.view.activity;

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
                                mArticalContent.setText(Html.fromHtml(newDetail.getContent()));
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
