package top.lemonsoda.arsenalnews.view.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import top.lemonsoda.arsenalnews.R;
import top.lemonsoda.arsenalnews.bean.NewItem;

/**
 * Created by chuanl on 4/5/16.
 */
public class NewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = NewsListAdapter.class.getCanonicalName();

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

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
        if (viewType == VIEW_TYPE_LOADING){
            View view = mLayoutInflater.inflate(R.layout.loading_item_news_list, parent, false);
            return new LoadingViewHolder(view);
        } else if (viewType == VIEW_TYPE_ITEM){
            View view = mLayoutInflater.inflate(R.layout.item_news_list, parent, false);
            return new NewsItemViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NewsItemViewHolder){
            NewsItemViewHolder viewHolder = (NewsItemViewHolder) holder;
            viewHolder.title.setText(mNewsList.get(position).getHeader());
            viewHolder.src.setText(mNewsList.get(position).getSource());
            Glide.with(mContext)
                    .load(mNewsList.get(position).getThumbnail())
                    .centerCrop()
                    .placeholder(R.mipmap.placeholder)
                    .thumbnail(0.5f)
                    .into(viewHolder.photo);
        } else if (holder instanceof LoadingViewHolder){
            LoadingViewHolder viewHolder = (LoadingViewHolder)holder;
            viewHolder.progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return mNewsList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public class NewsItemViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView src;
        private ImageView photo;
        private CardView container;

        public NewsItemViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_news_list_title);
            src = (TextView) itemView.findViewById(R.id.tv_news_list_src);
            photo = (ImageView) itemView.findViewById(R.id.iv_news_list_photo);
            container = (CardView) itemView.findViewById(R.id.cv_news_list_container);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnNewsItemClickListener.onItemClick(v, getLayoutPosition());
                }
            });
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {

        private ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar)itemView.findViewById(R.id.progressBar1);
        }
    }

    public void setOnNewsItemClickListener(OnNewsItemClickListener listener) {
        this.mOnNewsItemClickListener = listener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mOnLoadMoreListener = listener;
    }

    public interface OnNewsItemClickListener {
        void onItemClick(View view, int pos);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void setLoaded(){
        isLoading = false;
    }
}
