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

    public Single<List<ShotDraft>> getShotDraft() {
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

    public Single<Uri> storeImageAndReturnItsUri(String imageCroppedFormat, Uri croppedFileUri, Context context) {
        return Single.fromCallable(new Callable<Uri>() {
            @Override public Uri call() throws Exception {
                return ImageUtils.saveImageAndGetItsFinalUri(imageCroppedFormat,croppedFileUri,context);
            }
        });
    }

    /**
     * Must be deleted - just for test
     */
    public Completable storeCroppedImage(String ImageCroppedFormat, Uri croppedFileUri, Context context) {
        // Returns a Completable which when subscribed, executes the callable function,
        // ignores its normal result and emits onError or onComplete only.
        return Completable.fromCallable(new Callable<Void>() {
            @Override public Void call() throws Exception {
                ImageUtils.copyFileToGallery(ImageCroppedFormat,croppedFileUri,context);
                return null;
            }

        });
    }
}
