package top.lemonsoda.arsenalnews.view.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.lemonsoda.arsenalnews.R;
import top.lemonsoda.arsenalnews.bean.NewItem;

/**
 * Created by Chuan on 6/6/16.
 */
public class FavoriteNewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<NewItem> newItems;
    private Context context;
    private LayoutInflater layoutInflater;
    private OnFavoriteItemClickListener favoriteItemClickListener;

    public FavoriteNewsListAdapter(Context cxt, List<NewItem> items){
        this.context = cxt;
        this.newItems = items;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_favorite_news_list, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FavoriteViewHolder viewHolder = (FavoriteViewHolder)holder;
        viewHolder.tvTitle.setText(newItems.get(position).getHeader());
        viewHolder.tvSubTitle.setText(newItems.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return newItems.size();
    }

    public void setFavoriteItemClickListener(OnFavoriteItemClickListener listener){
        favoriteItemClickListener = listener;
    }

    public interface OnFavoriteItemClickListener{
        void onItemClick(int pos);
    }

    public class FavoriteViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tv_title)
        TextView tvTitle;

        @BindView(R.id.tv_subtitle)
        TextView tvSubTitle;

        @OnClick(R.id.cv_favorites_container)
        void onClick(){
            if (favoriteItemClickListener != null) {
                favoriteItemClickListener.onItemClick(getLayoutPosition());
            }
        }

        public FavoriteViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
