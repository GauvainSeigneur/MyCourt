package seigneur.gauvain.mycourt.ui.shots.list.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import butterknife.BindView;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder;

public class ShotViewHolder extends BaseViewHolder implements View.OnClickListener {

    @BindView(R.id.shot_image)
    ImageView shotImage;

    private ShotItemCallback mShotItemCallback;

    private ShotViewHolder(View itemView,ShotItemCallback shotItemCallback) {
        super(itemView);
        this.mShotItemCallback=shotItemCallback;
        shotImage.setOnClickListener(this);
    }

    public void bindTo(Shot shot) {
        //shotname.setText(shot.getTitle());
        Glide.with(itemView.getContext())
                .load(shot.getImageUrl())
                //.placeholder(R.mipmap.ic_launcher)
                .into(shotImage);
        //gifIcon.setVisibility(user.isSiteAdmin() ? View.VISIBLE : View.GONE);
    }

    public static ShotViewHolder create(ViewGroup parent, ShotItemCallback shotItemCallback) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_item_shot, parent, false);
        return new ShotViewHolder(view, shotItemCallback);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.shot_image:
                mShotItemCallback.onShotClicked(getAdapterPosition());
                break;
        }
    }

}
