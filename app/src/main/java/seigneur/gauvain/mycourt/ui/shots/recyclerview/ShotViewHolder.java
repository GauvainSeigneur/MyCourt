package seigneur.gauvain.mycourt.ui.shots.recyclerview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import butterknife.BindView;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder;


public class ShotViewHolder extends BaseViewHolder implements View.OnClickListener  {

    private ShotListCallback mCallback;

    @BindView(R.id.shot_image) public ImageView image;
    @BindView(R.id.shot_gif_label) public TextView gifLabel;

    public ShotViewHolder(View itemView, ShotListCallback callback) {
        super(itemView);
        this.mCallback =callback;
        image.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.shot_image:
                mCallback.onShotClicked(getAdapterPosition());
                break;
        }
    }
}
