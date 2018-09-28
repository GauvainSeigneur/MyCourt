package seigneur.gauvain.mycourt.ui.shotDetail;

import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder;
import seigneur.gauvain.mycourt.ui.widget.FourThreeImageView;
import seigneur.gauvain.mycourt.ui.widget.TagView;

public class TagViewHolder extends BaseViewHolder {
    @BindView(R.id.tag_item) public TagView tag;
    public TagViewHolder(View itemView) {super(itemView);}
}
