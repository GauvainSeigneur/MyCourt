package seigneur.gauvain.mycourt.ui.shotDetail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import seigneur.gauvain.mycourt.R;

/**
 * RecyclerView Adapter for Shot's tags
 */
public class TagListAdapter extends RecyclerView.Adapter<TagViewHolder> {

    private List<String> tags;
    private Context context;
    public TagViewHolder tagViewHolder;

    public TagListAdapter(Context context, @NonNull List<String> tags) {
        this.context = context;
        this.tags = tags;
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_tag, parent, false);
            return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TagViewHolder holder, final int position) {
        holder.tag.setText(tags.get(position));
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }
}
