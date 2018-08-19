package seigneur.gauvain.mycourt.ui.shotDraft.view;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.utils.Constants;

/**
 * ShotDraft RecyclerView adapter
 */
public class ShotDraftsListAdapter extends RecyclerView.Adapter<ShotDraftViewHolder> {

    private List<ShotDraft> data;
    private Context context;
    public ShotDraftViewHolder shotDraftViewHolder;
    public ShotDraftListCallback mCallback;

    /**
     * Constructor
     * @param context   - activity
     * @param data      - list of ShotDraft object
     * @param callback  - ShotDraftListCallback implementation
     */
    public ShotDraftsListAdapter(Context context, @NonNull List<ShotDraft> data, ShotDraftListCallback callback) {
        this.context = context;
        this.data = data;
        this.mCallback=callback;
    }

    @Override
    public ShotDraftViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_post, parent, false);
            return new ShotDraftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ShotDraftViewHolder holder, final int position) {
        ShotDraft item = data.get(position);
        holder.shotDraftTitle.setText(item.getTitle());
        if (item.getDraftType()== Constants.EDIT_MODE_NEW_SHOT) {
            holder.shotDraftType.setText("NEW");
        } else {
            holder.shotDraftType.setText("UPDATE");
        }
        if (data.get(position).getImageUrl()!=null) {
            Glide
                    .with(context)
                    .asDrawable()
                    .load(getImageUri(item)/*new File(item.getImageUrl())*/)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE) //disk Strategy to load new image and put another in Cache
                            //.override(80, 80) //dimension in Pixel
                            //.placeholder(R.mipmap.ic_launcher) //Drawables that are shown while a request is in progres
                            .error(R.mipmap.ic_launcher)//Drawables that are shown wif the image is not loaded
                    )
                    .into(holder.shotDraftImage);
        } else {
            holder.shotDraftImage.setImageResource(R.drawable.dribbble_ball_intro);
        }
        holder.shotDraftLayout.setOnClickListener(v -> {
            mCallback.onShotDraftClicked(data.get(position),position);
            }
        );

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void clear() {
        while (getItemCount() > 0) {
            data.clear();
        }
    }

    /**
     * get imageUri from ShotDraft object. Can be an HTTP URL for published shot or Absolute URL
     * for new shot project
     * @param item - shotDraft item
     * @return uri of the image
     */
    private Uri getImageUri(ShotDraft item){
        if(item.getImageUrl()!=null)
                if (item.getDraftType()==Constants.EDIT_MODE_NEW_SHOT)
                    return FileProvider.getUriForFile(context,
                            context.getString(R.string.file_provider_authorities),
                            new File(item.getImageUrl()));
                else
                    return Uri.parse(item.getImageUrl());
            else
                return null;
    }

}
