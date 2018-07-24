package seigneur.gauvain.mycourt.data.repository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Part;
import seigneur.gauvain.mycourt.data.api.DribbbleService;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;

public class ShotRepository {

    @Inject
    DribbbleService mDribbbleService;


    @Inject
    public ShotRepository(){}

    //get list of Shot from Dribbble
    public Flowable<List<Shot>> getShotsFromAPI(int applyResponseCache,  int page, int perPage) {
        return mDribbbleService.getShots(applyResponseCache, page, perPage);
    }

    //send an update to Dribbble
    public Single<Shot> updateShot(String id,
                                   String description,
                                   boolean isLowProfile,
                                   ArrayList<String> tags,
                                   String title) {
        return mDribbbleService.updateShot(id, description, isLowProfile,tags, title)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Shot> postShot(MultipartBody.Part body,
                                         RequestBody title) {
        return mDribbbleService.postShotTwo(body, title)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
