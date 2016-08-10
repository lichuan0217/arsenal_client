package top.lemonsoda.arsenalnews.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observer;
import top.lemonsoda.arsenalnews.R;
import top.lemonsoda.arsenalnews.bean.NewItem;
import top.lemonsoda.arsenalnews.domain.application.App;
import top.lemonsoda.arsenalnews.domain.utils.Constants;
import top.lemonsoda.arsenalnews.net.NetworkManager;
import top.lemonsoda.arsenalnews.view.adapter.FavoriteNewsListAdapter;

public class FavoriteActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, FavoriteNewsListAdapter.OnFavoriteItemClickListener {

    private static final String TAG = FavoriteActivity.class.getCanonicalName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.srl_favorite_news_item)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.rv_favorite_news_list)
    RecyclerView mFavoriteRecyclerView;

    private List<NewItem> mNewsItems = new ArrayList<>();
    private FavoriteNewsListAdapter mAdapter;
    private Observer<List<NewItem>> favoritesObserver;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            mUserId = bundle.getString(Constants.INTENT_EXTRA_USER_ID);
            Log.d(TAG, "userId: " + mUserId);
        }

        initObserver();
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mFavoriteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new FavoriteNewsListAdapter(this, mNewsItems);
        mAdapter.setFavoriteItemClickListener(this);
        mFavoriteRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setRefreshing(true);
        onRefresh();
    }

    @Override
    public void onRefresh() {
        if (!App.getInstance().isUserLogin()) {
            Snackbar.make(
                    mSwipeRefreshLayout,
                    R.string.favorite_login_prompt,
                    Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }
        NetworkManager.getInstance().getFavorites(favoritesObserver, mUserId);
    }

    private void initObserver() {
        favoritesObserver = new Observer<List<NewItem>>() {
            @Override
            public void onCompleted() {
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(
                        FavoriteActivity.this,
                        R.string.info_news_list_load_error,
                        Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onNext(List<NewItem> items) {
                if (!mNewsItems.isEmpty()) {
                    mNewsItems.clear();
                }
                mNewsItems.addAll(items);
                mAdapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    public void onItemClick(int pos) {
        String header = mNewsItems.get(pos).getHeader();
        String articleId = mNewsItems.get(pos).getArticalId();
        Intent intent = new Intent(FavoriteActivity.this, ArticleActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_HEADER, header);
        intent.putExtra(Constants.INTENT_EXTRA_ARTICLE_ID, articleId);
        startActivity(intent);
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
