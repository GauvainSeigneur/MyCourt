package seigneur.gauvain.mycourt.ui.shots.list.adapter;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;


import java.util.Objects;

import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.ui.shots.list.data.NetworkState;

public class ShotListAdapter extends PagedListAdapter<Shot, RecyclerView.ViewHolder> {

    private NetworkState networkState;

    private ShotItemCallback shotItemCallback;

    private int pos;

    public ShotListAdapter(ShotItemCallback shotItemCallback) {
        super(UserDiffCallback);
        this.shotItemCallback = shotItemCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case R.layout.list_item_shot:
                return ShotViewHolder.create(parent,shotItemCallback);
            case R.layout.list_item_network_state:
                return NetworkStateViewHolder.create(parent, shotItemCallback);
            default:
                throw new IllegalArgumentException("unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case R.layout.list_item_shot:
                ((ShotViewHolder) holder).bindTo(getItem(position));
                break;
            case R.layout.list_item_network_state:
                ((NetworkStateViewHolder) holder).bindTo(networkState);
                break;
        }
    }

    private boolean hasExtraRow() {
        return networkState != null && networkState != NetworkState.LOADED;
    }

    @Override
    public int getItemViewType(int position) {
        if (hasExtraRow() && position == getItemCount() - 1) {
            return R.layout.list_item_network_state;
        } else {
            return R.layout.list_item_shot;
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + (hasExtraRow() ? 1 : 0);
    }

    public Shot getShotClicked(int pos) {
        return getItem(pos);
    }

    /**
     * Set the current network state to the adapter
     * but this work only after the initial load
     * and the adapter already have list to add new loading raw to it
     * so the initial loading state the activity responsible for handle it
     *
     * @param newNetworkState the new network state
     */
    public void setNetworkState(NetworkState newNetworkState) {
        if (getCurrentList() != null) {
            if (getCurrentList().size() != 0) {
                NetworkState previousState = this.networkState;
                boolean hadExtraRow = hasExtraRow();
                this.networkState = newNetworkState;
                boolean hasExtraRow = hasExtraRow();
                if (hadExtraRow != hasExtraRow) {
                    if (hadExtraRow) {
                        notifyItemRemoved(super.getItemCount());
                    } else {
                        notifyItemInserted(super.getItemCount());
                    }
                } else if (hasExtraRow && previousState != newNetworkState) {
                    notifyItemChanged(getItemCount() - 1);
                }
            }
        }
    }

    private static DiffUtil.ItemCallback<Shot> UserDiffCallback = new DiffUtil.ItemCallback<Shot>() {
        @Override
        public boolean areItemsTheSame(@NonNull Shot oldItem, @NonNull Shot newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Shot oldItem, @NonNull Shot newItem) {
            return Objects.equals(oldItem, newItem);
        }
    };


}