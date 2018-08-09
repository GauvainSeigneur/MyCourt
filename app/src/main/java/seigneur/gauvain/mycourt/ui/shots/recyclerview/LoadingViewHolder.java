package seigneur.gauvain.mycourt.ui.shots.recyclerview;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder;


public class LoadingViewHolder extends BaseViewHolder implements View.OnClickListener  {

    private ShotListCallback mCallback;

    @BindView(R.id.loadmore_progress) public ProgressBar mProgressBar;
    @BindView(R.id.loadmore_retry) public ImageButton mRetryBtn;
    @BindView(R.id.loadmore_errortxt) public TextView mErrorTxt;
    @BindView(R.id.loadmore_errorlayout) public LinearLayout mErrorLayout;
    @BindView(R.id.endlist_text) public TextView  mEndListText;
    @BindView(R.id.endlist_layout) public LinearLayout mEndListlayout;

    public LoadingViewHolder(View itemView, ShotListCallback callback) {
        super(itemView);
        this.mCallback=callback;
        mRetryBtn.setOnClickListener(this);
        mErrorLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loadmore_retry:
            case R.id.loadmore_errorlayout:
                mCallback.retryPageLoad();
                break;
        }
    }
}
