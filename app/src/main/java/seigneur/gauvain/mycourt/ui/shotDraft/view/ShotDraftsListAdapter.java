package seigneur.gauvain.mycourt.ui.shotDraft.view;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.utils.Constants;


public class ShotDraftsListAdapter extends RecyclerView.Adapter<ShotDraftViewHolder> {

    private List<ShotDraft> data;
    private Context context;
    public ShotDraftViewHolder shotDraftViewHolder;
    public ShotDraftListCallback mCallback;

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
                    .load(Uri.parse(data.get(position).getImageUrl()))
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

        holder.shotDraftLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onShotDraftClicked(data.get(position),position);
            }
        });

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
}
