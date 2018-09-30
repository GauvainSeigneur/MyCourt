package seigneur.gauvain.mycourt.ui.shots.recyclerview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ShotListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    public static final int ITEM = 0;
    public static final int LOADING = 1;
    private List<Shot> shotsResults;
    private Context context;
    //private boolean isLoadingAdded = true;
    private boolean retryPageLoad = false;
    private boolean isEndListReached = false;
    private ShotListCallback mCallback;

    private String errorMsg;
    private String endMessage;

    public ShotListAdapter(Context context, ShotListCallback callback) {
        this.context = context;
        this.mCallback = callback;
        shotsResults = new ArrayList<Shot>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.list_item_shot, parent, false);
                viewHolder = new ShotViewHolder(viewItem, mCallback);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingViewHolder(viewLoading,mCallback);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //isLoadingAdded = true;
        switch (getItemViewType(position)) {
            case ITEM:
                Shot shotItem = shotsResults.get(position); //shots
                final ShotViewHolder shotViewHolder = (ShotViewHolder) holder;
                //shotViewHolder.title.setText(shotList.getTitle());
                //todo - if animated - set label "GIF"
                Glide
                        .with(context)
                        .load(Uri.parse(shotItem.getImageUrl()))
                        .transition(withCrossFade())
                        .apply(new RequestOptions()
                                .dontAnimate()
                                .diskCacheStrategy(DiskCacheStrategy.ALL) //disk Strategy to load new image and put another in Cache
                                //.override(80, 80) //dimension in Pixel
                                //.placeholder(R.drawable.dribbble_ball_intro) //Drawables that are shown while a request is in progres //todo - must be 4/3 format too
                                .error(R.mipmap.ic_launcher)//Drawables that are shown wif the image is not loaded //todo - must be 4/3 format too
                        )
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(shotViewHolder.image);
                break;

            case LOADING:
                LoadingViewHolder loadingVH = (LoadingViewHolder) holder;
                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mEndListlayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);
                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));
                } else if (isEndListReached)  {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mEndListlayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);
                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mEndListlayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        //return shotsResults == null ? 0 : shotsResults.size();
        // If no items are present, there's no need for loader
        if (shotsResults == null || shotsResults.size() == 0) {
            return 0;
        }
        // +1 for loader
        return shotsResults.size() + 1;
    }


    @Override
    public int getItemViewType(int position) {
        //return (position == shotsResults.size() -1)? LOADING : ITEM;
        //return (position == shotsResults.size() -1)? LOADING : ITEM; //+1 for loading view
        if (position != 0 && position == getItemCount() - 1) {
            return LOADING;
        }

        return ITEM;

    }

    /****************************************************************************
     * Pagination
     * *************************************************************************/
    public void add(Shot r) {
        shotsResults.add(r);
        notifyItemInserted(shotsResults.size());
    }

    public void addAll(List<Shot> moveResults) {
        for (Shot result : moveResults) {
            add(result);
        }
    }

    public void remove(Shot r) {
        int position = shotsResults.indexOf(r);
        if (position > -1) {
            shotsResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        //isLoadingAdded = false;
        while (getItemCount() > 1) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public Shot getItem(int position) {
        return shotsResults.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        //notifyItemChanged(shotsResults.size() - 1);
        notifyItemChanged(shotsResults.size());
        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    /**
     * Displays Pagination end list
     * @param show
     */
    public void showEndListMessage(boolean show) {
        isEndListReached = show;
        //notifyDataSetChanged();
        notifyItemChanged(shotsResults.size());
    }

}
