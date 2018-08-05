package seigneur.gauvain.mycourt.data.repository;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import seigneur.gauvain.mycourt.data.local.dao.PostDao;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.utils.ImageUtils;

@Singleton
public class ShotDraftRepository {

    @Inject
    PostDao postDao;

    @Inject
    public ShotDraftRepository(){}

    public Maybe<List<ShotDraft>> getShotDraft() {
        return postDao.getAllPost();
    }

    public Maybe<ShotDraft> getShotDraftByShotId(String ShotID) {
        return postDao.getShotDraftByShotId(ShotID);
    }

    public Completable updateShotDraft(ShotDraft shotDraft) {
        return Completable.fromRunnable(() -> postDao.updateDraft(shotDraft));
    }
    public Completable deleteDraft(int id) {
        return Completable.fromRunnable(() -> postDao.deletDraftByID(id));
    }

    public Completable storeShotDraft(ShotDraft shotDraft) {
        return Completable.fromRunnable(() -> postDao.insertPost(shotDraft));
    }

    public Single<String> storeImageAndReturnItsUri(String imageCroppedFormat, Uri croppedFileUri, Context context) {
        return Single.fromCallable(new Callable<String>() {
            @Override public String call() throws Exception {
                return ImageUtils.saveImageAndGetItsFinalUri(imageCroppedFormat,croppedFileUri,context);
            }
        });
    }
}
