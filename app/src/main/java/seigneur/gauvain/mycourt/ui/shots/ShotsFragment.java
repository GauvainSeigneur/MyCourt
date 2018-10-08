package seigneur.gauvain.mycourt.ui.shots;

import android.app.Activity;
import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.ui.base.BaseFragment;
import seigneur.gauvain.mycourt.ui.shotDetail.ShotDetailActivity;
import seigneur.gauvain.mycourt.ui.shots.list.adapter.ShotItemCallback;
import seigneur.gauvain.mycourt.ui.shots.list.adapter.ShotListAdapter;
import seigneur.gauvain.mycourt.ui.shots.list.data.NetworkState;
import seigneur.gauvain.mycourt.ui.shots.list.data.Status;
import timber.log.Timber;


/**
 * Created by gse on 22/11/2017.
 */
public class ShotsFragment extends BaseFragment implements ShotItemCallback {

    @BindView(R.id.usersSwipeRefreshLayout)
    SwipeRefreshLayout usersSwipeRefreshLayout;

    @BindView(R.id.rvShots)
    RecyclerView mRvShots;

    @BindView(R.id.globalNetworkState)
    LinearLayout globalNetworkState;

    @BindView(R.id.errorMessageTextView)
    TextView errorMessageTextView;

    @BindView(R.id.retryLoadingButton)
    Button retryLoadingButton;

    @BindView(R.id.loadingProgressBar)
    ProgressBar loadingProgressBar;

    private ShotListAdapter shotListAdapter;

    private LinearLayoutManager mLinearLayoutManager;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private ShotsViewModel shotsViewModel;

    /*
     ************************************************************************************
     *  Fragment lifecycle
     ************************************************************************************/
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this);
        }
        super.onAttach(activity);
    }

    @Override
    public void onAttach(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this);
        }
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate");
        shotsViewModel = ViewModelProviders.of(this, viewModelFactory).get(ShotsViewModel.class);
        shotsViewModel.init();
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_shots;
    }

    @Override
    public void onCreateView(View inRootView, Bundle inSavedInstanceState){
        Timber.d("onCreateView");
        initAdapter();
        initSwipeToRefresh();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //listen livedata
        subscribeToSingleEvent(shotsViewModel);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void initAdapter() {
        mLinearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        shotListAdapter = new ShotListAdapter(this);
        mRvShots.setLayoutManager(mLinearLayoutManager);
        mRvShots.setAdapter(shotListAdapter);
        shotsViewModel.shotList.observe(this, shotListAdapter::submitList);
        shotsViewModel.getNetworkState().observe(this, shotListAdapter::setNetworkState);
    }

    /**
     * Init swipe to refresh and enable pull to refresh only when there are items in the adapter
     */
    private void initSwipeToRefresh() {
        shotsViewModel.getRefreshState().observe(this, networkState -> {
            if (networkState != null) {
                if (shotListAdapter.getCurrentList() != null) {
                    if (shotListAdapter.getCurrentList().size() > 0) {
                        usersSwipeRefreshLayout.setRefreshing(
                                networkState.getStatus() == NetworkState.LOADING.getStatus());
                    } else {
                        setInitialLoadingState(networkState);
                    }
                } else {
                    setInitialLoadingState(networkState);
                }
            }
        });
        usersSwipeRefreshLayout.setOnRefreshListener(() -> shotsViewModel.refresh());
    }

    private void subscribeToSingleEvent(ShotsViewModel shotsViewModel) {
        shotsViewModel.getShotClickEvent().observe(
                this,
                position -> {

                    ActivityOptions options = null;
                    Intent i = new Intent(getActivity(), ShotDetailActivity.class);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        options = ActivityOptions.makeSceneTransitionAnimation((Activity) getActivity(),
                                mLinearLayoutManager.findViewByPosition(position),
                                getActivity().getString(R.string.shot_transition_name));
                        getContext().startActivity(i, options.toBundle());
                    }
                }
        );
    }

    /**
     * Show the current network state for the first load when the user list
     * in the adapter is empty and disable swipe to scroll at the first loading
     *
     * @param networkState the new network state
     */
    private void setInitialLoadingState(NetworkState networkState) {
        //error message
        errorMessageTextView.setVisibility(networkState.getMessage() != null ? View.VISIBLE : View.GONE);
        if (networkState.getMessage() != null) {
            errorMessageTextView.setText(networkState.getMessage());
        }

        //loading and retry
        /*if (shotListAdapter!=null) {
            retryLoadingButton.setVisibility(shotListAdapter.getItemCount()>0 ? View.GONE : View.VISIBLE);
            loadingProgressBar.setVisibility(shotListAdapter.getItemCount()>0 ? View.GONE : View.VISIBLE);
        }*/
        retryLoadingButton.setVisibility(networkState.getStatus() == Status.FAILED ? View.VISIBLE : View.GONE);
        loadingProgressBar.setVisibility(networkState.getStatus() == Status.RUNNING ? View.VISIBLE : View.GONE);

        usersSwipeRefreshLayout.setEnabled(networkState.getStatus() == Status.SUCCESS);
    }

    @OnClick(R.id.retryLoadingButton)
    void retryInitialLoading() {
        shotsViewModel.retry();
    }

    @Override
    public void retry() {
        shotsViewModel.retry();
    }

    @Override
    public void onShotClicked(int position) {
        //usersViewModel.retry()
        Shot shotItem = shotListAdapter.getShotClicked(position);
        shotsViewModel.onShotClicked(shotItem, position);
        //Toast.makeText(getContext(), ""+shotItem.title, Toast.LENGTH_SHORT).show();
        //mShotsPresenter.onShotClicked(shotItem, position);
    }
    

}
