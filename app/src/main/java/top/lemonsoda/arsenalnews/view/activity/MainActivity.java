package top.lemonsoda.arsenalnews.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import top.lemonsoda.arsenalnews.R;
import top.lemonsoda.arsenalnews.bean.NewItem;
import top.lemonsoda.arsenalnews.bean.User;
import top.lemonsoda.arsenalnews.domain.db.ItemDatabaseManager;
import top.lemonsoda.arsenalnews.domain.preferences.UserInfoKeeper;
import top.lemonsoda.arsenalnews.domain.utils.Constants;
import top.lemonsoda.arsenalnews.net.NetworkManager;
import top.lemonsoda.arsenalnews.view.adapter.NewsListAdapter;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, NewsListAdapter.OnNewsItemClickListener {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private LinearLayoutManager mLayoutManager;
    private List<NewItem> mNewsList = new ArrayList<NewItem>();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mNewsListSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_news_item);
        mNewsListRecyclerView = (RecyclerView) findViewById(R.id.rv_news_list);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mHeaderView = mNavigationView.getHeaderView(0);
        mImgAvatar = (ImageView) mHeaderView.findViewById(R.id.img_avatar);
        mTextViewUserName = (TextView) mHeaderView.findViewById(R.id.tv_user_name);
        setupNavigationView();
        setupToggle();

        mNewsListSwipeRefreshLayout.setOnRefreshListener(this);
        mItemDatabaseManager = new ItemDatabaseManager(this);
        initData();

        mLayoutManager = new LinearLayoutManager(this);
        mNewsListRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new NewsListAdapter(this, mNewsList);
        mAdapter.setOnNewsItemClickListener(this);
        mNewsListRecyclerView.setAdapter(mAdapter);


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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Bitmap getLocalBitmap(String file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            return BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setupNavigationView() {
        User user = UserInfoKeeper.readUserInfo(this);
        if (!TextUtils.isEmpty(user.getScreen_name())) {
            mTextViewUserName.setText(user.getScreen_name());
        }

        File avatar_file = new File(Constants.AVATAR_FILE);
        if (avatar_file.exists()) {
            Bitmap bitmap = getLocalBitmap(Constants.AVATAR_FILE);
            if (bitmap != null) {
                mImgAvatar.setImageBitmap(bitmap);
            }
        }

        mImgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                switchActivity(LoginActivity.class);
            }
        });
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        if (item.isChecked()) {
                            item.setChecked(false);
                        } else {
                            item.setChecked(true);
                        }
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        switch (item.getItemId()) {
                            case R.id.nav_about:
                                return switchActivity(AboutActivity.class);
                        }
                        return true;
                    }
                });
    }

    private void setupToggle() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();
    }

    private void initData() {
        mItemDatabaseManager.getItemFromDB(mNewsList);
    }

    @Override
    public void onRefresh() {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mNewsListSwipeRefreshLayout.setRefreshing(false);
//            }
//        }, 3000);

        mItemPage = 0;
        NetworkManager.newsItemService.getNewsItem(mItemPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<List<NewItem>>() {
                            @Override
                            public void call(List<NewItem> newItems) {
                                if (!mNewsList.isEmpty()) {
                                    mNewsList.clear();
                                }
                                saveData(newItems);
                                mNewsList.addAll(newItems);
                                mAdapter.notifyDataSetChanged();
                                mItemPage++;
                                mNewsListSwipeRefreshLayout.setRefreshing(false);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                mNewsListSwipeRefreshLayout.setRefreshing(false);
                                Toast.makeText(
                                        MainActivity.this,
                                        R.string.info_news_list_load_error,
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                );
    }

    private void saveData(List<NewItem> newItems) {
        mItemDatabaseManager.saveItemsToDB(newItems);
    }

    @Override
    public void onItemClick(View view, int pos) {
        String header = mNewsList.get(pos).getHeader();
        String articalId = mNewsList.get(pos).getArticalId();
        Intent intent = new Intent(MainActivity.this, ArticalActivity.class);
        intent.putExtra("Header", header);
        intent.putExtra("ArticalId", articalId);
        startActivity(intent);
    }

    private boolean switchActivity(Class clazz) {
        startActivity(new Intent(MainActivity.this, clazz));
        return true;
    }
}
