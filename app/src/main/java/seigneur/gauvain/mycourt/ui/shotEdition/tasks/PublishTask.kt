package seigneur.gauvain.mycourt.ui.shotEdition.tasks

import android.content.Context
import io.reactivex.disposables.CompositeDisposable
import seigneur.gauvain.mycourt.data.model.Attachment
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository
import seigneur.gauvain.mycourt.data.repository.ShotRepository
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler
import timber.log.Timber


class PublishTask(
        private val mCompositeDisposable: CompositeDisposable,
        private val mShotRepository: ShotRepository,
        private val mShotDraftRepository: ShotDraftRepository,
        private val mNetworkErrorHandler: NetworkErrorHandler?,
        private val mConnectivityReceiver: ConnectivityReceiver,
        private val mPublishCallBack: PublishCallBack) {

    /*
    *************************************************************************
    * POST SHOT ON DRIBBBLE
    *************************************************************************/
    fun postShot(draft: Draft, context: Context) {
        mPublishCallBack.showWaiter(true)
        if (draft.hasAttachmentToPublish()) {
            //has attachment concat request
            mCompositeDisposable.add(
                    mShotRepository.postShotAndAttachment(draft, context)
                            .subscribe(
                                    { response ->
                                        when (response.code()) {
                                        Constants.ACCEPTED -> { onPublishSucceed(draft) }
                                        else ->
                                            onPublishFailed("Post failed: " + response.code() +": "+ response.message(), null)
                                        }},
                                    {t -> onPublishFailed(t.toString(), t)}
                            )
            )
        } else {
            mCompositeDisposable.add(
                    //doesn't have attachment
                    mShotRepository.publishANewShot(draft, context)
                            .subscribe(
                                    { response ->
                                        when (response.code()) {
                                            Constants.ACCEPTED -> { onPublishSucceed(draft) }
                                            else ->
                                                onPublishFailed("Post failed: " + response.code() +": "+ response.message(), null)
                                        }
                                    },
                                    {t -> onPublishFailed(t.toString(), t)}
                            )
            )
        }

    }


    /*
    *************************************************************************
    * UPDATE SHOT ON DRIBBBLE
    * 4 state possible :
    * A - only update Shot info
    * b - update shot info and post related attachment
    * c - update shot info and delete related attachment
    * d - update shot info, post and delete related attachment
    *************************************************************************/
    fun updateShot(
            draft: Draft,
            attachmentTodelete:List<Attachment>,
            context: Context,
            profile: Boolean) {
        mPublishCallBack.showWaiter(true)
        if (attachmentTodelete.isNotEmpty() && draft.hasAttachmentToPublish()) {
            //has new attachment to publish and old attachment to delete
            mCompositeDisposable.add(
                    mShotRepository.updateShotAndUploadAndDeleteAttachment(draft, profile, context, attachmentTodelete)
                            .subscribe(
                                    { _ -> Timber.d("update success updateShotAndUploadAndDeleteAttachment") },
                                    {  onPublishFailed("Update failed: $it",it)}
                            )
            )
        } else if (attachmentTodelete.isNotEmpty() && !draft.hasAttachmentToPublish()) {
            //has attachment to delete
            mCompositeDisposable.add(
                    mShotRepository.updateShotAndDeleteAttachment(draft, profile, attachmentTodelete)
                            .subscribe(
                                    { shot -> Timber.d("update success updateShotAndDeleteAttachment ")
                                        //onUpdateShotSuccess(shot, draft, context, attachmentTodelete)
                                    },
                                    { onPublishFailed("Update failed: $it", it)}
                            )
            )
        } else if (attachmentTodelete.isEmpty() && draft.hasAttachmentToPublish()) {
            //has new attachment to publish
            mCompositeDisposable.add(
                    mShotRepository.updateShotAndPostAttachment(draft, profile, context)
                            .subscribe(
                                    { shot -> Timber.d("update success updateShotAndPostAttachment")
                                        //onUpdateShotSuccess(shot, draft, context, attachmentTodelete)
                                    },
                                    {  onPublishFailed("Update failed: $it",it)}
                            )
            )
        } else {
            //no new attachment or delete attachment
            mCompositeDisposable.add(
                    mShotRepository.updateShot(draft, profile)
                            .subscribe(
                                    { shot -> Timber.d("updateShot success ")
                                        //onUpdateShotSuccess(shot, draft, context, attachmentTodelete)
                                    },
                                    {   onPublishFailed("Update failed: $it",it)
                                    }
                            )
            )
        }

    }

    /*
    *************************************************************************
    * DB OPERATION - DELETE DRAFT AFTER PUBLISH OR UPDATE
    *************************************************************************/
    private fun deleteDraftAfterPublish(draft:Draft) {
        mCompositeDisposable.add(mShotDraftRepository.deleteDraft(draft.draftID)
                .subscribe(
                        this::onDeleteSucceed,
                        this::onDeleteError
                )
        )

    }

    private fun onDeleteSucceed() {
        Timber.d("delete succeed")
    }

    private fun onDeleteError(throwable: Throwable) {
        Timber.e(throwable)
    }

    /*
    *************************************************************************
    * GLOBAL METHOD FOR EVERY REQUEST AND POST
    *************************************************************************/
    private fun onPublishSucceed(draft:Draft) {
        mPublishCallBack.showWaiter(false)
        deleteDraftAfterPublish(draft) //todo
        mPublishCallBack.onPublishSucceed()
    }

    private fun onPublishFailed(error:String, t:Throwable?) {
        t?.let{
            handleNetworkOperationError(t,100)
        }
        mPublishCallBack.showWaiter(false)
        mPublishCallBack.onPublishFailed(error)
    }

    /*
    *************************************************************************
    * MANAGE NETWORK EXCEPTION
    *
    * Note : we could use RxJavaPlugins.setErrorHandler but as we already
    * specified an error handler using subscribe, RxJavaPlugins.setErrorHandler will not be called.
    *************************************************************************/
    private fun handleNetworkOperationError(error: Throwable, eventID: Int) {
        Timber.d("error handled by rx")
        if (mNetworkErrorHandler == null) {
            Timber.tag("rxHandler").d("mNetworkErrorHandler is null")
        } else {
            mNetworkErrorHandler.handleNetworkErrors(error, eventID,
                    object : NetworkErrorHandler.onErrorListener {
                override fun onNetworkException(throwable: Throwable) {
                    if (mConnectivityReceiver.isOnline()) {
                        Timber.d("weird, you are connected, may be an interruption")
                    } else {
                        Timber.d("Not connected to internet")
                    }
                }
                override fun onHttpException(throwable: Throwable) {
                    Timber.d(throwable)
                }
            })
        }

    }

    /**
     * CALLBACK FOR VIEWMODEL
     */
    interface PublishCallBack {

        fun showWaiter(showIt:Boolean)

        fun onPublishSucceed()

        fun onPublishFailed(error:String)

    }

}
