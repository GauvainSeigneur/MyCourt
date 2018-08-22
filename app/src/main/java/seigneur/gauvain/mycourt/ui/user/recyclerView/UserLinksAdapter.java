package seigneur.gauvain.mycourt.ui.user.recyclerView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Map;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.utils.ListUtils;

import static seigneur.gauvain.mycourt.utils.Constants.BEHANCE;
import static seigneur.gauvain.mycourt.utils.Constants.CREATIVEMARKET;
import static seigneur.gauvain.mycourt.utils.Constants.FACEBOOK;
import static seigneur.gauvain.mycourt.utils.Constants.GITHUB;
import static seigneur.gauvain.mycourt.utils.Constants.INSTAGRAM;
import static seigneur.gauvain.mycourt.utils.Constants.LINKEDIN;
import static seigneur.gauvain.mycourt.utils.Constants.MEDIUM;
import static seigneur.gauvain.mycourt.utils.Constants.TWITTER;

public class UserLinksAdapter extends RecyclerView.Adapter<LinksViewHolder> {

    private Context mContext;
    Map<String, String> links;

    @Override
    public int getItemCount() {
        return links.size();
    }

    public UserLinksAdapter(Context context, @NonNull  Map<String, String> links) {
        this.mContext = context;
        this.links = links;
    }

    @Override
    public LinksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_user_links, parent, false);
        return new LinksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LinksViewHolder holder, int position) {
        holder.linkTitle.setText(ListUtils.mapToListKey(links).get(position));
        bindIcon(holder, position);
        //todo manage url on cllick
    }

    private void bindIcon(LinksViewHolder holder, int position) {
        switch(ListUtils.mapToListKey(links).get(position)) {
            case INSTAGRAM:
                holder.linkIcon.setImageResource(R.drawable.ic_instagram);
                holder.linklayout.setBackgroundResource(R.drawable.social_item_background_instagram);
                break;
            case FACEBOOK:
                holder.linkIcon.setImageResource(R.drawable.ic_facebook);
                holder.linklayout.setBackgroundTintList(ColorStateList.valueOf(
                        mContext.getResources().getColor(R.color.colorFacebook)));
                break;
            case TWITTER:
                holder.linkIcon.setImageResource(R.drawable.ic_twitter);
                break;
            case BEHANCE:
                holder.linkIcon.setImageResource(R.drawable.ic_behance);
                break;
            case MEDIUM:
                holder.linkIcon.setImageResource(R.drawable.ic_medium);
                break;
             case LINKEDIN:
                holder.linkIcon.setImageResource(R.drawable.ic_linkedin);
                break;
            case GITHUB:
                holder.linkIcon.setImageResource(R.drawable.ic_github_circle);
                holder.linklayout.setBackgroundTintList(ColorStateList.valueOf(
                        mContext.getResources().getColor(R.color.colorGithub)));
                break;
            case CREATIVEMARKET:
                holder.linkIcon.setImageResource(R.drawable.ic_creative_market);
                break;
            default:
                holder.linkIcon.setImageResource(R.drawable.ic_web);
                holder.linklayout.setBackgroundTintList(ColorStateList.valueOf(
                        mContext.getResources().getColor(R.color.colorAccent)));
        }
    }

}