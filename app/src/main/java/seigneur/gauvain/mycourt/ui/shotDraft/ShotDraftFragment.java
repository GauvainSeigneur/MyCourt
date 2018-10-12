package seigneur.gauvain.mycourt.ui.shotDraft;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Draft;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.ui.base.BaseFragment;
import seigneur.gauvain.mycourt.ui.main.MainViewModel;
import seigneur.gauvain.mycourt.ui.shotEdition.EditShotActivity;
import timber.log.Timber;


/**
 * Created by gse on 22/11/2017.
 */
public class ShotDraftFragment extends BaseFragment implements  Toolbar.OnMenuItemClickListener {

   /* @Inject
    ShotDraftPresenter mShotDraftPresenter;*/

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    Application mApplication;

    ShotDraftViewModel mShotDraftViewModel;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.swipe_refresh_drafts)
    SwipeRefreshLayout mRefreshDraftLayout;

    @BindView(R.id.rv_shot_draft)
    RecyclerView shotDraftRV;

    private ShotDraftListCallback mcallabck;
    private ShotDraftsListAdapter mShotDraftsListAdapter;

    List<Draft> shotDraftsSaved = new ArrayList<Draft>();

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
        //provide ViewModel
        mShotDraftViewModel = ViewModelProviders.of(this, viewModelFactory).get(ShotDraftViewModel.class);
        mShotDraftViewModel.fetchShotDrafts();
        mcallabck = new ShotDraftListCallback() {
            @Override
            public void onShotDraftClicked(Draft shotDraft,int position) {
                mShotDraftViewModel.onShotDraftClicked(shotDraft, position);
                // mShotDraftPresenter.onShotDraftClicked(shotDraft, position); //TODO SINGLE EVENT

            }
        };
        mShotDraftsListAdapter= new ShotDraftsListAdapter(getContext(), shotDraftsSaved, mcallabck);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_shot_draft;
    }

    @Override
    public void onCreateView(View inRootView, Bundle inSavedInstanceState){
        Timber.d("onCreateView");
        toolbar.inflateMenu(R.menu.menu_shot_detail);
        toolbar.setOnMenuItemClickListener(this);
        shotDraftRV.setLayoutManager(new GridLayoutManager(getContext(),2));
        shotDraftRV.setAdapter(mShotDraftsListAdapter);
        mRefreshDraftLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mShotDraftViewModel.onRefresh(true);
            }
        });
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //listen livedata
        subscribeToSingleEvents(mShotDraftViewModel);
        listenLiveData(mShotDraftViewModel);
    }

    private void subscribeToSingleEvents(ShotDraftViewModel viewModel) {
        viewModel.dbChanged().observe(
                this,
                dbChangedEVent -> {
                    Timber.d("db has changed");
                    Toast.makeText(mApplication, "db has changed", Toast.LENGTH_SHORT).show();
                }
        );

        viewModel.getItemClickedEvent().observe(
                this,
                item -> {
                    Intent intent = new Intent(getActivity(), EditShotActivity.class);
                    startActivity(intent);
                }
        );



    }

    private void listenLiveData(ShotDraftViewModel shotDraftViewModel) {
        mShotDraftViewModel.getDrafts()
                .observe(
                        this,
                        new Observer <List<Draft>>() {
                            @Override
                            public void onChanged(@Nullable List<Draft> drafts) {
                                if (drafts!=null) {
                                    showEmptyView(false);
                                    showDraftList(drafts, false);//todo - fix this
                                } else {
                                    showEmptyView(true);
                                }
                            }
                        }
                );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(mApplication, "yeah!", Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    public void stopRefresh() {
       // if (mRefreshDraftLayout.isRefreshing())
            mRefreshDraftLayout.setRefreshing(false);
    }

    public void showDraftList(List<Draft> shotDraft, boolean isRefreshing) {
        if (isRefreshing)
            mShotDraftsListAdapter.clear(); //todo - use diffutils instead of this, is not perform

        mShotDraftsListAdapter.clear(); //TODO - MANAGE THIS EVENT MORE PROPERLY

        shotDraftsSaved.addAll(shotDraft);
        mShotDraftsListAdapter.notifyDataSetChanged();
    }

    public void showEmptyView(boolean isVisible) {
        if (isVisible)
            Toast.makeText(mApplication, "empty view", Toast.LENGTH_SHORT).show();
    }

    public void goToShotEdition() {
        Intent intent = new Intent(getActivity(), EditShotActivity.class);
        startActivity(intent);
    }


}
