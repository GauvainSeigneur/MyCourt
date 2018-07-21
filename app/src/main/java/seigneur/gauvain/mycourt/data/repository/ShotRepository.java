package seigneur.gauvain.mycourt.data.repository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.api.DribbbleService;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;

public class ShotRepository {

    @Inject
    DribbbleService mDribbbleService;


    @Inject
    public ShotRepository(){}

    public Shot shotForPublish() {
        return new Shot();
    }

    //get list of Shot from Dribbble
    public Flowable<List<Shot>> getShotsFromAPI(int applyResponseCache,  int page) {
        return mDribbbleService.getShots(applyResponseCache, page);
    }

    //send an update to Dribbble
    public Single<Shot> updateShot(String id,
                                   String description,
                                   boolean isLowProfile,
                                   String[] tags,
                                   String title) {
        return mDribbbleService.updateShot(id, description, isLowProfile,tags, title)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
