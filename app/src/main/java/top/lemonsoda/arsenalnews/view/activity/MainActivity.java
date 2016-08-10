package top.lemonsoda.arsenalnews.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import rx.Observer;
import top.lemonsoda.arsenalnews.R;
import top.lemonsoda.arsenalnews.bean.NewItem;
import top.lemonsoda.arsenalnews.bean.User;
import top.lemonsoda.arsenalnews.domain.application.App;
import top.lemonsoda.arsenalnews.domain.db.ItemDatabaseManager;
import top.lemonsoda.arsenalnews.domain.preferences.UserInfoKeeper;
import top.lemonsoda.arsenalnews.domain.utils.BitmapSaver;
import top.lemonsoda.arsenalnews.domain.utils.Constants;
import top.lemonsoda.arsenalnews.net.NetworkManager;
import top.lemonsoda.arsenalnews.view.adapter.NewsListAdapter;

public class MainActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener,
        NewsListAdapter.OnNewsItemClickListener,
        NewsListAdapter.OnLoadMoreListener {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private LinearLayoutManager mLayoutManager;
    private List<NewItem> mNewsList = new ArrayList<>();
    private NewsListAdapter mAdapter;
    private RecyclerView mNewsListRecyclerView;
    private SwipeRefreshLayout mNewsListSwipeRefreshLayout;
    private int mItemPage;
    private ItemDatabaseManager mItemDatabaseManager;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private View mHeaderView;
    private ImageView mImgAvatar;
    private TextView mTextViewUserName;
    private Toolbar mToolbar;
    private ProgressBar mProgressBarLoading;

    private IntentFilter mIntentFilter;
    private LoginEventReceiver mReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;

    private Observer<List<NewItem>> newsItemObserver;
    private Observer<List<NewItem>> newsItemLoadMoreObserver;

    private BitmapSaver mBitmapSaver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mBitmapSaver = BitmapSaver.getInstance(this);
        mBitmapSaver.setFileName(Constants.AVATAR_NAME);
        mNewsListSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_news_item);
        mNewsListRecyclerView = (RecyclerView) findViewById(R.id.rv_news_list);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mHeaderView = mNavigationView.getHeaderView(0);
        mImgAvatar = (ImageView) mHeaderView.findViewById(R.id.img_avatar);
        mTextViewUserName = (TextView) mHeaderView.findViewById(R.id.tv_user_name);
        mProgressBarLoading = (ProgressBar) findViewById(R.id.pb_loading_news_list);
        setupNavigationView();
        setupToggle();

        mNewsListSwipeRefreshLayout.setOnRefreshListener(this);
        mItemDatabaseManager = new ItemDatabaseManager(this);

        initData();
        switchEmptyView();

        initObserver();

        mLayoutManager = new LinearLayoutManager(this);
        mNewsListRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new NewsListAdapter(this, mNewsList, mNewsListRecyclerView);
        mAdapter.setOnNewsItemClickListener(this);
        mAdapter.setOnLoadMoreListener(this);
        mNewsListRecyclerView.setAdapter(mAdapter);

        // Refresh the data list
        if (!mNewsListSwipeRefreshLayout.isRefreshing()) {
            mNewsListSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mNewsListSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }
        onRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
    }

    @Override
    protected void onDestroy() {
        mAdapter.stopBannerScrollController();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            Log.d(TAG, "Action Refresh");
            mLayoutManager.scrollToPositionWithOffset(0, 0);
            mNewsListSwipeRefreshLayout.setRefreshing(true);
            onRefresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setupNavigationView() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.LOGIN_EVENT_INTENT_ACTION);
        mReceiver = new LoginEventReceiver();
        mLocalBroadcastManager.registerReceiver(mReceiver, mIntentFilter);
        final User user = UserInfoKeeper.readUserInfo(this);
        if (!TextUtils.isEmpty(user.getScreen_name())) {
            mTextViewUserName.setText(user.getScreen_name());
        }

        if (mBitmapSaver.exists()) {
            mImgAvatar.setImageBitmap(mBitmapSaver.load());
        }

        mImgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchActivity(LoginActivity.class);
            }
        });
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        mDrawerLayout.closeDrawers();
                        switch (item.getItemId()) {
                            case R.id.nav_favorite:
                                if (!App.getInstance().isUserLogin()) {
                                    Snackbar.make(
                                            mNavigationView,
                                            R.string.favorite_login_prompt,
                                            Snackbar.LENGTH_SHORT)
                                            .setAction("Action", null).show();
                                    return true;
                                }
                                Bundle bundle = new Bundle();
                                bundle.putString(Constants.INTENT_EXTRA_USER_ID, user.getId() + "");
                                return switchActivity(FavoriteActivity.class, bundle);
                            case R.id.nav_about:
                                return switchActivity(AboutActivity.class);
                            case R.id.nav_settings:
                                return switchActivity(SettingsActivity.class);
                        }
                        return true;
                    }
                });
    }

    private void setupToggle() {
        // Initializing Drawer Layout and ActionBarToggle
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we don't want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we don't want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    private void initData() {
        mItemDatabaseManager.getItemFromDB(mNewsList);
    }

    private void initObserver() {
        newsItemObserver = new Observer<List<NewItem>>() {
            @Override
            public void onCompleted() {
                mNewsListSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                mNewsListSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(
                        MainActivity.this,
                        R.string.info_news_list_load_error,
                        Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onNext(List<NewItem> newItems) {
                if (!mNewsList.isEmpty()) {
                    mNewsList.clear();
                }
                saveData(newItems);
                mNewsList.addAll(newItems);
                switchEmptyView();
                mAdapter.notifyDataSetChanged();
                mItemPage++;
            }
        };

        newsItemLoadMoreObserver = new Observer<List<NewItem>>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "onComplete()");
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError()");
                mNewsList.remove(mNewsList.size() - 1);
                mAdapter.notifyItemRemoved(mNewsList.size());
                Toast.makeText(
                        MainActivity.this,
                        R.string.info_news_list_load_error,
                        Toast.LENGTH_SHORT)
                        .show();
                mAdapter.setLoaded();
            }

            @Override
            public void onNext(List<NewItem> newItems) {
                Log.d(TAG, "onNext()");
                mNewsList.remove(mNewsList.size() - 1);
                mAdapter.notifyItemRemoved(mNewsList.size());
                if (newItems.size() == 0) {
                    Toast.makeText(
                            MainActivity.this,
                            getString(R.string.info_news_list_load_no_more),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mNewsList.addAll(newItems);
                mItemPage++;
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh()");
        mItemPage = 0;
        NetworkManager.getInstance().getNewsItem(newsItemObserver, mItemPage);
    }

    private void saveData(List<NewItem> newItems) {
        mItemDatabaseManager.saveItemsToDB(newItems);
    }

    @Override
    public void onItemClick(View view, int pos) {
        String header = mNewsList.get(pos).getHeader();
        String articleId = mNewsList.get(pos).getArticalId();
        Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_HEADER, header);
        intent.putExtra(Constants.INTENT_EXTRA_ARTICLE_ID, articleId);
        startActivity(intent);
    }

    @Override
    public void onItemClick(NewsListAdapter.NewsItemViewHolder holder, int pos) {
        String header = mNewsList.get(pos).getHeader();
        String articleId = mNewsList.get(pos).getArticalId();
        Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_HEADER, header);
        intent.putExtra(Constants.INTENT_EXTRA_ARTICLE_ID, articleId);

        startActivity(intent);
    }

    private boolean switchActivity(Class clazz) {
        startActivity(new Intent(MainActivity.this, clazz));
        return true;
    }

    private boolean switchActivity(Class clazz, Bundle bundle) {
        Intent intent = new Intent(MainActivity.this, clazz);
        intent.putExtras(bundle);
        startActivity(intent);
        return true;
    }

    @Override
    public void onLoadMore() {
        mNewsList.add(null);
        mAdapter.notifyItemInserted(mNewsList.size() - 1);

        //Load Data
        Log.d(TAG, "onLoadMore: page " + mItemPage);
        NetworkManager.getInstance().getNewsItem(
                newsItemLoadMoreObserver, mItemPage);
    }

    private void switchEmptyView() {
        if (mNewsList.size() == 0) {
            mNewsListRecyclerView.setVisibility(View.GONE);
            mProgressBarLoading.setVisibility(View.VISIBLE);
        } else {
            mNewsListRecyclerView.setVisibility(View.VISIBLE);
            mProgressBarLoading.setVisibility(View.GONE);
        }
    }

    private class LoginEventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isLogin = intent.getBooleanExtra(Constants.INTENT_LOGIN_EXTRA_KEY, false);
            if (isLogin) {
                Toast.makeText(MainActivity.this, "login", Toast.LENGTH_SHORT).show();
                User user = UserInfoKeeper.readUserInfo(MainActivity.this);
                mTextViewUserName.setText(user.getScreen_name());
                Glide.with(MainActivity.this)
                        .load(user.getProfile_image_url())
                        .asBitmap()
                        .centerCrop()
                        .into(mImgAvatar);
            } else {
                mImgAvatar.setImageResource(R.mipmap.avatar);
                mTextViewUserName.setText("Android Studio");
                Toast.makeText(MainActivity.this, "logout", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
