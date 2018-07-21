package seigneur.gauvain.mycourt.ui.shots.recyclerview;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    public static final int ITEM = 0;
    public static final int LOADING = 1;

    private List<Shot> shotsResults;
    private Context context;
    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;

    public PaginationAdapter(Context context, PaginationAdapterCallback callback) {
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
                viewHolder = new ShotViewHolder(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Shot shotItem = shotsResults.get(position); //shots
        switch (getItemViewType(position)) {
            case ITEM:
                final ShotViewHolder shotViewHolder = (ShotViewHolder) holder;
                //shotViewHolder.title.setText(shotList.getTitle());
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
                        .into(shotViewHolder.image);
                //todo - if animated - set label "GIF"
                shotViewHolder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallback.onShotClicked(shotItem, position);
                    }
                });
                break;

            case LOADING:
                LoadingVH loadingVH = (LoadingVH) holder;
                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);

                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));

                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return shotsResults == null ? 0 : shotsResults.size();
    }


    @Override
    public int getItemViewType(int position) {
        return (position == shotsResults.size() - 1 && isLoadingAdded) ? LOADING : ITEM;

    }

    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    public void add(Shot r) {
        shotsResults.add(r);
        notifyItemInserted(shotsResults.size() - 1);
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
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Shot());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;
        int position = shotsResults.size() - 1;
        Shot result = getItem(position);
        if (result != null) {
            shotsResults.remove(position);
            notifyItemRemoved(position);
        }
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
        notifyItemChanged(shotsResults.size() - 1);
        if (errorMsg != null) this.errorMsg = errorMsg;
    }

   /*
   View Holders
   _________________________________________________________________________________________________
    */
    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private ImageButton mRetryBtn;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;

        public LoadingVH(View itemView) {
            super(itemView);

            mProgressBar = (ProgressBar) itemView.findViewById(R.id.loadmore_progress);
            mRetryBtn = (ImageButton) itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt = (TextView) itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout = (LinearLayout) itemView.findViewById(R.id.loadmore_errorlayout);

            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:
                    showRetry(false, null);
                    mCallback.retryPageLoad();
                    break;
            }
        }
    }

}
