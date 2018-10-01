package seigneur.gauvain.mycourt.ui.shots.list.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;

public class ShotViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.shot_image)
    ImageView shotImage;

    private ShotViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindTo(Shot shot) {
        //shotname.setText(shot.getTitle());
        Glide.with(itemView.getContext())
                .load(shot.getImageUrl())
                //.placeholder(R.mipmap.ic_launcher)
                .into(shotImage);
        //siteAdminIcon.setVisibility(user.isSiteAdmin() ? View.VISIBLE : View.GONE);
    }

    public static ShotViewHolder create(ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_item_shot, parent, false);
        return new ShotViewHolder(view);
    }

}
