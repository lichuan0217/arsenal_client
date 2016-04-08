package top.lemonsoda.arsenalnews.view.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    private Context mContext;
    private List<NewItem> mNewsList;
    private LayoutInflater mLayoutInflater;
    private OnNewsItemClickListener mOnNewsItemClickListener;

    public NewsListAdapter(Context context, List<NewItem> newsList){
        this.mContext = context;
        this.mNewsList = newsList;
        mLayoutInflater = LayoutInflater.from(context);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.item_news_list, parent, false);
        RecyclerView.ViewHolder viewHolder = new NewsItemViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NewsItemViewHolder viewHolder = (NewsItemViewHolder)holder;
        viewHolder.title.setText(mNewsList.get(position).getHeader());
        viewHolder.src.setText(mNewsList.get(position).getSource());
        Glide.with(mContext)
                .load(mNewsList.get(position).getThumbnail())
                .centerCrop()
                .placeholder(R.mipmap.placeholder)
                .thumbnail(0.5f)
                .into(viewHolder.photo);
    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }

    public class NewsItemViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView src;
        private ImageView photo;
        private CardView container;
        public NewsItemViewHolder(View itemView){
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.tv_news_list_title);
            src = (TextView)itemView.findViewById(R.id.tv_news_list_src);
            photo = (ImageView)itemView.findViewById(R.id.iv_news_list_photo);
            container = (CardView)itemView.findViewById(R.id.cv_news_list_container);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnNewsItemClickListener.onItemClick(v, getLayoutPosition());
                }
            });
        }
    }

    public void setOnNewsItemClickListener(OnNewsItemClickListener listener){
        this.mOnNewsItemClickListener = listener;
    }

    public interface OnNewsItemClickListener{
        public void onItemClick(View view, int pos);
    }
}
