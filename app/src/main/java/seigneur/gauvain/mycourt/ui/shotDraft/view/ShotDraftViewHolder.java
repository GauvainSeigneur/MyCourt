package seigneur.gauvain.mycourt.ui.shotDraft.view;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder;
import seigneur.gauvain.mycourt.ui.widget.FourThreeImageView;

public class ShotDraftViewHolder extends BaseViewHolder {
    @BindView(R.id.shot_draft_layout) public CardView shotDraftLayout;
    @BindView(R.id.shot_draft_title) public TextView shotDraftTitle;
    @BindView(R.id.draft_type) public TextView shotDraftType;
    @BindView(R.id.shot_draft_image) public FourThreeImageView shotDraftImage;

    public ShotDraftViewHolder(View itemView) {super(itemView);}
}
