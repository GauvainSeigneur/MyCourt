package seigneur.gauvain.mycourt.data.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import seigneur.gauvain.mycourt.data.api.DribbbleService;
import seigneur.gauvain.mycourt.data.model.Shot;

public class ShotRepository {

    @Inject
    DribbbleService mDribbbleService;


    @Inject
    public ShotRepository(){}

    //get list of Shot from Dribbble
    public Flowable<List<Shot>> getShotsFromAPI(int applyResponseCache,  int page, int perPage) {
        return mDribbbleService.getShots(applyResponseCache, page, perPage);
    }

    //get list of Shot from Dribbble
    public Flowable<List<Shot>> getShotsFromAPItest(int applyResponseCache,  long page, int perPage) {
        return mDribbbleService.getShotAPI(applyResponseCache, page, perPage);
    }

    //send an update to Dribbble
    public Single<Shot> updateShot(String id,
                                   String title,
                                   String description,
                                   ArrayList<String> tags,
                                   boolean isLowProfile
    ) {
        return mDribbbleService.updateShot(id,title, description, tags, isLowProfile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Response<Void>> publishANewShot(
            HashMap<String, RequestBody> map,
            MultipartBody.Part file,
            String title,
            String description,
            ArrayList<String> tags) {
        return mDribbbleService.publishANewShot(map, file,title,description,tags)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
