package seigneur.gauvain.mycourt.data.repository;

import android.content.Context;
import android.net.Uri;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.local.dao.PostDao;
import seigneur.gauvain.mycourt.data.model.Draft;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.utils.image.ImageUtils;
import seigneur.gauvain.mycourt.utils.SingleLiveEvent;

@Singleton
public class ShotDraftRepository {

    @Inject
    PostDao postDao;

    @Inject
    public ShotDraftRepository(){}

    public SingleLiveEvent<Void> onDraftDBChanged =new SingleLiveEvent<>();

    public Maybe<List<Draft>> getShotDraft() {
        return postDao.getAllPost()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Maybe<Draft> getShotDraftByShotId(String ShotID) {
        return postDao.getShotDraftByShotId(ShotID);
    }


    public Completable updateShotDraft(Draft shotDraft) {
        return Completable.fromRunnable(() -> postDao.updateDraft(shotDraft))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                            //notifiy With Single event
                            onDraftDBChanged.call();
                        }

                );
    }

    public Completable storeShotDraft(Draft shotDraft) {
        return Completable.fromRunnable(
                () -> postDao.insertPost(shotDraft)
        )
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                            //notifiy With Single event
                            onDraftDBChanged.call();
                        }

                );
    }

    public Completable deleteDraft(int id) {
        return Completable.fromRunnable(() -> postDao.deletDraftByID(id))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                            //notifiy With Single event
                            onDraftDBChanged.call();
                        }

                );
    }

    public Single<String> storeImageAndReturnItsUri(String imageCroppedFormat, Uri croppedFileUri, Context context) {
        return Single.fromCallable(new Callable<String>() {
            @Override public String call() throws Exception {
                return ImageUtils.saveImageAndGetItsFinalUri(imageCroppedFormat,croppedFileUri,context);
            }
        });
    }
}
