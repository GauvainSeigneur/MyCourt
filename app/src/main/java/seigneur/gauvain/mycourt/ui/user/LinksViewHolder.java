package seigneur.gauvain.mycourt.ui.user;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder;

public class LinksViewHolder extends BaseViewHolder {
    @BindView(R.id.user_link_layout) public FrameLayout linklayout;
    @BindView(R.id.link_icon) public ImageView linkIcon;
    @BindView(R.id.link_title) public TextView linkTitle;

    public LinksViewHolder(View itemView) {super(itemView);}
}
