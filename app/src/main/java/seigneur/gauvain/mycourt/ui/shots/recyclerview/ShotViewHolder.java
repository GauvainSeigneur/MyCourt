package seigneur.gauvain.mycourt.ui.shots.recyclerview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import butterknife.BindView;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder;


public class ShotViewHolder extends BaseViewHolder {
    @BindView(R.id.shot_image) public ImageView image;
    @BindView(R.id.shot_gif_label) public TextView gifLabel;

    public ShotViewHolder(View itemView) {super(itemView);}
}
