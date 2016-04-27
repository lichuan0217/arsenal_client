package top.lemonsoda.arsenalnews.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import top.lemonsoda.arsenalnews.R;
import top.lemonsoda.arsenalnews.bean.NewItem;
import top.lemonsoda.arsenalnews.domain.db.ItemDatabaseManager;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

//        initData();
        mNewsListSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_news_item);
        mNewsListRecyclerView = (RecyclerView) findViewById(R.id.rv_news_list);

        mNewsListSwipeRefreshLayout.setOnRefreshListener(this);

        mLayoutManager = new LinearLayoutManager(this);
        mNewsListRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new NewsListAdapter(this, mNewsList);
        mAdapter.setOnNewsItemClickListener(this);
        mNewsListRecyclerView.setAdapter(mAdapter);

        mItemDatabaseManager = new ItemDatabaseManager(this);

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void saveData(List<NewItem> newItems){
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
}
