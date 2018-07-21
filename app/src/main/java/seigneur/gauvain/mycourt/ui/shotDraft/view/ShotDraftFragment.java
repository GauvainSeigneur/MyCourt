package seigneur.gauvain.mycourt.ui.shotDraft.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.ui.base.BaseFragment;
import seigneur.gauvain.mycourt.ui.shotDraft.presenter.ShotDraftPresenter;
import seigneur.gauvain.mycourt.ui.shotEdition.view.EditShotActivity;
import timber.log.Timber;


/**
 * Created by gse on 22/11/2017.
 */

public class ShotDraftFragment extends BaseFragment implements ShotDraftView, Toolbar.OnMenuItemClickListener {

    @Inject
    ShotDraftPresenter mShotDraftPresenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.swipe_refresh_drafts)
    SwipeRefreshLayout mRefreshDraftLayout;

    @BindView(R.id.rv_shot_draft)
    RecyclerView shotDraftRV;

    @BindView(R.id.fab_add)
    FloatingActionButton fabAdd;

    private ShotDraftListCallback mcallabck;
    private ShotDraftsListAdapter mShotDraftsListAdapter;

    List<ShotDraft> shotDraftsSaved = new ArrayList<ShotDraft>();

    /**
     * FRAGMENT LIFE CYCLE
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //new instance
        Timber.d("onCreate new instance");
        mcallabck = new ShotDraftListCallback() {
            @Override
            public void onShotDraftClicked(ShotDraft shotDraft,int position) {
                mShotDraftPresenter.onShotDraftClicked(shotDraft, position);
            }
        };
        mShotDraftsListAdapter= new ShotDraftsListAdapter(getContext(), shotDraftsSaved, mcallabck);
    }

     // Overridden from BaseFragment
    @Override
    public void onCreateView(View rootView, Bundle savedInstanceState) {
        //bindView here
        ButterKnife.bind(this, rootView);
        toolbar.inflateMenu(R.menu.menu_shot_detail);
        toolbar.setOnMenuItemClickListener(this);
        shotDraftRV.setLayoutManager(new GridLayoutManager(getContext(),2));
        shotDraftRV.setAdapter(mShotDraftsListAdapter);
        mRefreshDraftLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mShotDraftPresenter.onRefresh();
            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditShotActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this);
            mShotDraftPresenter.onAttach();
        }
        super.onAttach(activity);
    }

    @Override
    public void onAttach(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this);
            mShotDraftPresenter.onAttach();
        }
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume called");
        //mShotDraftPresenter.refreshListOfDraft();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mShotDraftPresenter.onDetach();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(activity, "yeah!", Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    /**
     * BASE FRAGMENT METHODS
     */
    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_shot_draft;
    }


    @Override
    public void stopRefresh() {
       // if (mRefreshDraftLayout.isRefreshing())
            mRefreshDraftLayout.setRefreshing(false);
    }

    @Override
    public void showDraftList(List<ShotDraft> shotDraft, boolean isRefreshing) {
        if (isRefreshing)
            mShotDraftsListAdapter.clear(); //todo - use diffutils instead of this, is not perform
        shotDraftsSaved.addAll(shotDraft);
        mShotDraftsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void showEmptyView(boolean isVisible) {
        if (isVisible)
            Toast.makeText(activity, "empty view", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void goToShotEdition() {
        Intent intent = new Intent(getActivity(), EditShotActivity.class);
        startActivity(intent);
    }


}
