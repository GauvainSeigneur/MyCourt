package seigneur.gauvain.mycourt.ui.shotEdition

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import android.widget.Toast

import java.util.ArrayList
import java.util.Date

import javax.inject.Inject

import gnu.trove.TIntArrayList
import io.reactivex.disposables.CompositeDisposable
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository
import seigneur.gauvain.mycourt.data.repository.ShotRepository
import seigneur.gauvain.mycourt.data.repository.TempDataRepository
import seigneur.gauvain.mycourt.ui.shotEdition.tasks.GetSourceTask
import seigneur.gauvain.mycourt.ui.shotEdition.tasks.PublishTask
import seigneur.gauvain.mycourt.ui.shotEdition.tasks.StoreDraftTask
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.ListUtils
import seigneur.gauvain.mycourt.utils.SingleLiveEvent
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler
import timber.log.Timber

class ShotEditionViewModel @Inject
constructor() : ViewModel(),
        StoreDraftTask.StoreRequestListener,
        GetSourceTask.SourceCallback,
        PublishTask.PublishCallBack {

    @Inject
    lateinit var mShotDraftRepository: ShotDraftRepository

    @Inject
    lateinit var mNetworkErrorHandler: NetworkErrorHandler

    @Inject
    lateinit var mConnectivityReceiver: ConnectivityReceiver

    @Inject
    lateinit var mShotRepository: ShotRepository

    @Inject
    lateinit var mTempDataRepository: TempDataRepository

    @Inject
    lateinit var mApplication: Application

    //RX disposable
    private val compositeDisposable = CompositeDisposable()
    //Manage source and Edition mode (new or update)
    private var mTempDraft: Draft? = null
    val setUpUiCmd = SingleLiveEvent<Void>()

    //task files
    private val mStoreDrafTask: StoreDraftTask by lazy {
        StoreDraftTask(compositeDisposable, mShotDraftRepository, this) }
    private val mGetSourceTask: GetSourceTask by lazy {
        GetSourceTask(mTempDataRepository, compositeDisposable, this) }
    private val mPublishTask: PublishTask by lazy {
        PublishTask(compositeDisposable, mShotRepository, mShotDraftRepository,
                mNetworkErrorHandler, mConnectivityReceiver,this)
    }

    //Pick and Crop Image
    /*
    *********************************************************************************************
    * EVENT WHICH VIEW WILL SUBSCRIBE
    *********************************************************************************************/
    val pickShotCommand = SingleLiveEvent<Void>()
    val cropImageCmd = SingleLiveEvent<Void>()
    val requestPermCmd = SingleLiveEvent<Void>()
    val checkPerm = SingleLiveEvent<Void>()
    var imagePickedUriSource: Uri? = null //NOT LIVEDATA - NOT RELATED TO UI
    var imagePickedFileName: String? = null //NOT LIVEDATA - NOT RELATED TO UI
    var imagePickedFormat: String? = null //NOT LIVEDATA - NOT RELATED TO UI
    var imageSize: IntArray? = null //NOT LIVEDATA - NOT RELATED TO UI
    private val croppedImageUri = MutableLiveData<Uri>()
    val pickCropImgErrorCmd = SingleLiveEvent<Int>()
    //Listen change in editText
    private val mTitle = MutableLiveData<String>()
    private val mDescription = MutableLiveData<String>()
    private val mTags = MutableLiveData<ArrayList<String>>()

    val title: LiveData<String>
        get() = mTitle

    val description: LiveData<String>
        get() = mDescription

    val tags: LiveData<ArrayList<String>>
        get() = mTags

    public override fun onCleared() {
        super.onCleared()
        Timber.d("viewmodel cleared")
        compositeDisposable.clear()
    }

    /*
     *********************************************************************************************
     * PUBLIC METHODS CALLED IN VIEW
     *********************************************************************************************/
    fun init() {
        initTasks()
        if (mTempDraft == null)
            mGetSourceTask!!.getOriginOfEditRequest()
    }

    fun onImagePreviewClicked() {
        if (mTempDraft!!.typeOfDraft == Constants.EDIT_MODE_UPDATE_SHOT)
            Timber.d("not allowed to change image already published")
        else
            pickShotCommand.call()
    }

    fun onImagePicked() {
        cropImageCmd.call()
    }

    fun onImageCropped(uri: Uri) {
        croppedImageUri.value = uri
    }

    fun onPickcropError(erroCode: Int) {
        pickCropImgErrorCmd.value = erroCode
    }

    fun onTitleChanged(title: String) {
        mTitle.value = title
    }

    fun onDescriptionChanged(desc: String) {
        mDescription.value = desc
    }

    fun onTagChanged(tag: String) {
        mTags.value = EditUtils.tagListWithoutQuote(tag)
    }

    fun requestPerm() {
        requestPermCmd.call()
    }

    fun onPermGranted() {
        registerOrUpdateDraft(mApplication, true)
    }

    fun onStoreDraftClicked() {
        if (title.value == null || title.value!!.isEmpty()) {
            //TODO - single live event
            //mEditShotView.showMessageEmptyTitle();
        } else {
            if (getCroppedImageUri().value != null && imagePickedFormat != null) {
                checkPerm.call()
            } else {
                registerOrUpdateDraft(mApplication, false)
            }
        }
    }

    fun onPublishClicked() {
        if (mTempDraft!!.typeOfDraft == Constants.EDIT_MODE_UPDATE_SHOT) {
            val shotId: String? = null
            /*/if (mObjectSource instanceof Shot) {
                Shot shot = (Shot) mObjectSource;
                shotId = shot.getId();
            } else if (mObjectSource instanceof Draft) {
                Draft shotDraft = (Draft) mObjectSource;
                shotId = shotDraft.getShot().getId();
            }*/
            //todo - finish and test !!!
            mPublishTask!!.updateShot(
                    mTempDraft!!.shot.id!!,
                    title.value!!,
                    description.value!!,
                    tags.value!!, false)
        } else
            mPublishTask!!.postShot(
                    mApplication,
                    getCroppedImageUri().value!!, //when image is changed
                    imagePickedFormat!!,
                    title.value!!,
                    description.value!!,
                    tags.value!!)
    }

    /*
    *********************************************************************************************
    * PRIVATE METHODS
    *********************************************************************************************/
    private fun initTasks() {
        if (mStoreDrafTask != null && mPublishTask != null && mGetSourceTask != null) {
            Timber.d("tasks initialized")
        } else {

            Timber.d("tasks null")
        }

    }

    private fun registerOrUpdateDraft(context: Context?, isRegisteringImage: Boolean) {
        if (isRegisteringImage) {
            mStoreDrafTask.storeDraftImage(context!!,
                    imagePickedFormat!!, getCroppedImageUri().value!!)
        } else {
            mTempDraft!!.changeInfoFromEdit(
                    mTempDraft!!.imageUri, //Image doesn't change
                    mTempDraft!!.imageFormat, //Image doesn't change
                    title.value,
                    description.value,
                    tags.value)
            if (mTempDraft!!.draftID == 0L) {
                //new draft, so save it in db
                mStoreDrafTask.save(mTempDraft!!)
            } else {
                //it is draft fetch from db, update it
                mStoreDrafTask.update(mTempDraft!!)
            }
        }
    }

    fun getCroppedImageUri(): LiveData<Uri> {
        return croppedImageUri
    }

    /*
    *********************************************************************************************
    * GETTER AND SETTER - CAN BE CALLED BY VIEW AND VIEWMODEL
    *********************************************************************************************/
    fun getmTempDraft(): Draft? {
        return mTempDraft
    }

    /*
    *********************************************************************************************
    * GetSourceTaskCallback
    *********************************************************************************************/
    override fun dataForUIReady() {
        setUpUiCmd.call()
    }

    override fun setUpTempDraft(draft: Draft) {
        mTempDraft = draft
        Timber.d("tempDraft created : " + mTempDraft!!.draftID)
    }

    /*
    *********************************************************************************************
    * StoreTaskCallback
    *********************************************************************************************/
    override fun onSaveImageSuccess(uri: String) {
        mTempDraft!!.changeInfoFromEdit(
                uri,
                imagePickedFormat,
                title.value,
                description.value,
                tags.value)

        if (mTempDraft!!.draftID == 0L) {
            //new draft, so save it in db
            mStoreDrafTask.save(mTempDraft!!)
        } else {
            //it is draft fetch from db, update it
            mStoreDrafTask.update(mTempDraft!!)
        }
    }

    override fun onStoreDraftSucceed() {

    }

    override fun onFailed() {

    }

    /*
     *********************************************************************************************
     * PublishTaskCallBack
     *********************************************************************************************/
    override fun onPublishSuccess() {
        Toast.makeText(mApplication, "publish succeed", Toast.LENGTH_SHORT).show()
    }

}
