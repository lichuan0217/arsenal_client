package top.lemonsoda.arsenalnews.view.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import top.lemonsoda.arsenalnews.R;
import top.lemonsoda.arsenalnews.bean.NewItem;
import top.lemonsoda.arsenalnews.domain.utils.Utils;

/**
 * Created by chuanl on 4/5/16.
 */
public class NewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = NewsListAdapter.class.getCanonicalName();
    private static final int UPDATE_VIEWPAGER = 0;


    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final int VIEW_TYPE_HEADER = 2;

    private static final int BANNER_COUNT = 5;
    private List<ImageView> imageViewContainer = null;
    private int preDotPos = 0;
    private long scrollTimeOffset = 5000;
    private BannerScrollController controller;

    private Context mContext;
    private List<NewItem> mNewsList;
    private LayoutInflater mLayoutInflater;
    private RecyclerView mRecyclerView;
    private OnNewsItemClickListener mOnNewsItemClickListener;
    private OnLoadMoreListener mOnLoadMoreListener;

    private boolean isLoading;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;

    public NewsListAdapter(Context context, List<NewItem> newsList, RecyclerView recyclerView) {
        this.mContext = context;
        this.mNewsList = newsList;
        this.mRecyclerView = recyclerView;
        mLayoutInflater = LayoutInflater.from(context);

        final LinearLayoutManager linearLayoutManager =
                (LinearLayoutManager) this.mRecyclerView.getLayoutManager();
        this.mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LOADING) {
            View view = mLayoutInflater.inflate(R.layout.loading_item_news_list, parent, false);
            return new LoadingViewHolder(view);
        } else if (viewType == VIEW_TYPE_ITEM) {
            View view = mLayoutInflater.inflate(R.layout.item_news_list, parent, false);
            return new NewsItemViewHolder(view);
        } else if (viewType == VIEW_TYPE_HEADER) {
            View view = mLayoutInflater.inflate(R.layout.header_item_news_list, parent, false);
            return new HeaderViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NewsItemViewHolder) {
            Log.d(TAG, "NewsItem ViewHolder ........" + position);
            NewsItemViewHolder viewHolder = (NewsItemViewHolder) holder;
            viewHolder.title.setText(mNewsList.get(position - 1).getHeader());
            viewHolder.src.setText(mNewsList.get(position - 1).getSource());
            Glide.with(mContext)
                    .load(mNewsList.get(position - 1).getThumbnail())
                    .centerCrop()
                    .placeholder(R.mipmap.placeholder)
                    .thumbnail(0.5f)
                    .into(viewHolder.photo);
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder viewHolder = (LoadingViewHolder) holder;
            viewHolder.progressBar.setIndeterminate(true);
        } else if (holder instanceof HeaderViewHolder) {
            Log.d(TAG, "Header ViewHolder .........." + position);
            stopBannerScrollController();
            HeaderViewHolder viewHolder = (HeaderViewHolder) holder;
            initHeaderView(viewHolder);
        }

    }

    @Override
    public int getItemCount() {
        return mNewsList.size() + 1;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_TYPE_HEADER;
        return mNewsList.get(position - 1) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }


    public void setOnNewsItemClickListener(OnNewsItemClickListener listener) {
        this.mOnNewsItemClickListener = listener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mOnLoadMoreListener = listener;
    }

    public interface OnNewsItemClickListener {
        void onItemClick(View view, int pos);
        void onItemClick(NewsItemViewHolder holder, int pos);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void setLoaded() {
        isLoading = false;
    }

    public class NewsItemViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView src;
        public ImageView photo;
        public CardView container;

        public NewsItemViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_news_list_title);
            src = (TextView) itemView.findViewById(R.id.tv_news_list_src);
            photo = (ImageView) itemView.findViewById(R.id.iv_news_list_photo);
            container = (CardView) itemView.findViewById(R.id.cv_news_list_container);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    mOnNewsItemClickListener.onItemClick(v, getLayoutPosition() - 1);
                    mOnNewsItemClickListener.onItemClick(NewsItemViewHolder.this,
                            getLayoutPosition() - 1);
                }
            });
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {

        private ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        private ViewPager viewPager;
        private LinearLayout llDotGroup;
        private TextView tvBannerTextDesc;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            viewPager = (ViewPager) itemView.findViewById(R.id.vp_banner);
            llDotGroup = (LinearLayout) itemView.findViewById(R.id.ll_dot_group);
            tvBannerTextDesc = (TextView) itemView.findViewById(R.id.tv_banner_text_desc);
        }
    }

    public void stopBannerScrollController() {
        if (controller != null) {
            controller.stopBannerScrollThread();
            controller = null;
        }
    }

    private void initHeaderView(final HeaderViewHolder holder) {
        Log.d(TAG, "Init Header View.....");

        imageViewContainer = new ArrayList<>();
        ImageView imageView = null;
        View dot = null;
        LinearLayout.LayoutParams params = null;
        holder.llDotGroup.removeAllViews();
        for (int i = 0; i < BANNER_COUNT; ++i) {
            imageView = new ImageView(mContext);
            imageView.setBackgroundResource(R.mipmap.placeholder);
            imageViewContainer.add(imageView);

            if (mNewsList.size() > 0) {
                Glide.with(mContext)
                        .load(Utils.getImageFromThumbnailUrl(mNewsList.get(i).getThumbnail()))
                        .asBitmap()
                        .centerCrop()
                        .into(imageViewContainer.get(i));
            }

            // 添加dot到线性布局中
            dot = new View(mContext);
            dot.setBackgroundResource(R.drawable.banner_dot_bg_selector);
            params = new LinearLayout.LayoutParams(5, 5);
            params.leftMargin = 10;
            dot.setEnabled(false);
            dot.setLayoutParams(params);
            holder.llDotGroup.addView(dot);
        }

        holder.viewPager.setAdapter(new BannerAdapter());
        holder.viewPager.addOnPageChangeListener(new BannerPagerChangerListener(holder));

        if (mNewsList.size() > 0) {
            holder.tvBannerTextDesc.setText(mNewsList.get(0).getHeader());
        } else {
            holder.tvBannerTextDesc.setText("");
        }
        holder.llDotGroup.getChildAt(0).setEnabled(true);
        holder.viewPager.setCurrentItem(0);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_VIEWPAGER:
//                        Log.d(TAG, "Update ViewPager ...");
                        int newIndex = holder.viewPager.getCurrentItem() + 1;
                        holder.viewPager.setCurrentItem(newIndex);
                }
            }
        };
        if (controller == null) {
            controller = new BannerScrollController(handler);
        }
        controller.startBannerScrollThread();
    }

    private class BannerAdapter extends PagerAdapter {
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(imageViewContainer.get(position % imageViewContainer.size()));
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = imageViewContainer.get(position % imageViewContainer.size());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnNewsItemClickListener.onItemClick(v, position % imageViewContainer.size());
                }
            });

            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private class BannerPagerChangerListener implements ViewPager.OnPageChangeListener {
        private HeaderViewHolder holder;

        public BannerPagerChangerListener(HeaderViewHolder viewHolder) {
            holder = viewHolder;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            int pos = position % imageViewContainer.size();
//            Log.d(TAG, "onPageSelected.... Current Pos: " + pos + " Pre: " + preDotPos);
            holder.tvBannerTextDesc.setText(mNewsList.get(pos).getHeader());
            holder.llDotGroup.getChildAt(preDotPos).setEnabled(false);
            holder.llDotGroup.getChildAt(pos).setEnabled(true);
            preDotPos = pos;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private class BannerScrollController {
        private boolean stop = false;
        private Handler handler;

        public BannerScrollController(Handler handler) {
            this.handler = handler;
        }

        public void startBannerScrollThread() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!stop) {
                        //每个两秒钟发一条消息到主线程，更新viewpager界面
                        SystemClock.sleep(scrollTimeOffset);
                        if (!stop) {
//                            Log.d(TAG, "Send Update Message........");
                            Message msg = handler.obtainMessage(UPDATE_VIEWPAGER);
                            handler.sendMessage(msg);
                        }
                    }
                }
            }).start();
        }

        public void stopBannerScrollThread() {
            this.stop = true;
        }
    }
}
