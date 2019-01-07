package seigneur.gauvain.mycourt.ui.shotEdition

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import android.widget.Toast

import java.util.ArrayList
import java.util.Date

import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import seigneur.gauvain.mycourt.data.model.Attachment
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

    /*
    *********************************************************************************************
    * EVENT WHICH VIEW WILL SUBSCRIBE
    *********************************************************************************************/
    val pickShotCommand = SingleLiveEvent<Void>()
    val pickAttachmentCommand = SingleLiveEvent<Void>()
    val cropImageCmd = SingleLiveEvent<Void>()
    val requestPermCmd = SingleLiveEvent<Void>()
    val checkPerm = SingleLiveEvent<Void>()
    val onPublishSucceed = SingleLiveEvent<Void>()
    var imagePickedUriSource: Uri? = null //NOT LIVEDATA - NOT RELATED TO UI
    var imagePickedFileName: String? = null //NOT LIVEDATA - NOT RELATED TO UI
    var imagePickedFormat: String? = null //NOT LIVEDATA - NOT RELATED TO UI
    var imageSize: IntArray? = null //NOT LIVEDATA - NOT RELATED TO UI
    private val croppedImageUri = MutableLiveData<Uri>()
    val pickCropImgErrorCmd = SingleLiveEvent<Int>()
    val isReadyToPubish     = MutableLiveData<Boolean>()
    //Listen change in editText
    private val mTitle = MutableLiveData<String>()
    private val mDescription = MutableLiveData<String>()
    private val mTags = MutableLiveData<ArrayList<String>>()
    //List of attachment
    private val mTempAttachmentList = ArrayList<Attachment>() //UI list
    private val mAttachmentsTobeUploaded= MutableLiveData<ArrayList<Attachment>>()

    private val mTempAttachmentsToDelete = ArrayList<Attachment>()//Non UI list to keep reference of attachment that they needs to be deleted

    val title: LiveData<String>
        get() = mTitle

    val description: LiveData<String>
        get() = mDescription

    val tags: LiveData<ArrayList<String>>
        get() = mTags

    val readytoPublish: LiveData<Boolean>
        get() = isReadyToPubish

    val attachmentsTobeUploaded : LiveData<ArrayList<Attachment>>
        get() = mAttachmentsTobeUploaded

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
        if (mTempDraft == null)
            mGetSourceTask.getOriginOfEditRequest()
    }

    fun onImagePreviewClicked() {
        if (mTempDraft!!.typeOfDraft == Constants.EDIT_MODE_UPDATE_SHOT)
            Timber.d("not allowed to change image already published")
        else
            pickShotCommand.call()
    }

    fun onAddAttachmentClicked() {
        pickAttachmentCommand.call()
    }

    fun onImagePicked() {
        cropImageCmd.call()
    }

    fun onImageCropped(uri: Uri?) {
        croppedImageUri.value = uri
        checkIfIsReadyToPublish(mTitle.value, croppedImageUri.value)
    }

    fun onPickCropError(errorCode: Int) {
        pickCropImgErrorCmd.value = errorCode
    }

    fun onTitleChanged(title: String?) {
        mTitle.value = title
        checkIfIsReadyToPublish(mTitle.value, croppedImageUri.value)
    }

    fun onDescriptionChanged(desc: String) {
        mDescription.value = desc
    }

    fun onTagChanged(tag: String) {
        mTags.value = EditUtils.tagListWithoutQuote(tag)
    }

    fun onAttachmentAdded(attachment: Attachment) {
        mTempAttachmentList.add(attachment)
        mAttachmentsTobeUploaded.value = mTempAttachmentList
    }

    fun onRemoveAttachment(pos: Int) {
        if (mTempAttachmentList[pos].id!=-1L && mTempAttachmentList[pos].shotId.isNotEmpty()) {
            mTempAttachmentsToDelete.add(mTempAttachmentList[pos])
            //todo schedule a delete post on Dribbble
        }
        //ui and list to be uploaded
        mTempAttachmentList.removeAt(pos)
        mAttachmentsTobeUploaded.value = mTempAttachmentList

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
        changeTempDraftInfo() //first change info for temporary draft
        if (mTempDraft!!.typeOfDraft == Constants.EDIT_MODE_UPDATE_SHOT) {
            mPublishTask.updateShot(
                    mTempDraft!!,
                    false)
        } else
            mPublishTask.postShot(
                    mTempDraft!!,
                    mApplication,
                    getCroppedImageUri().value!!, //when image is changed
                    imagePickedFormat!!,null,null)//todo- define value
    }

    /*
    *********************************************************************************************
    * PRIVATE METHODS
    *********************************************************************************************/
    private fun registerOrUpdateDraft(context: Context?, isRegisteringImage: Boolean) {
        if (isRegisteringImage) {
            mStoreDrafTask.storeDraftImage(context!!,
                    imagePickedFormat!!, getCroppedImageUri().value!!)
        } else {
            changeTempDraftInfo()
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

    private fun changeTempDraftInfo() {
        mTempDraft!!.changeInfoFromEdit(
                mTempDraft!!.imageUri, //Image doesn't change
                mTempDraft!!.imageFormat, //Image doesn't change
                title.value,
                description.value,
                tags.value)
    }

    /**
     * The draft needs, at least, to have an image and a title to published and only a title
     * to be updated
     * Must be called in method which change title and image uri values changes
     * @param title - title of the shot to be published/updated
     * @param imageUri - link of the image to be published
     */
    private fun checkIfIsReadyToPublish(title:String?, imageUri:Uri?) :Boolean {
        var isReady: Boolean?=false
        if (title!=null  && title.isNotEmpty() && imageUri!=null && imageUri.toString().isNotEmpty()) {
            isReady = true
        }
        onReadyToPublishOrNot(isReady)
        Timber.tag("babar").d("Uri: "+imageUri)
        Timber.tag("babar").d("title: "+title)
        return isReady!!
    }

    /**
     * Change value of "ready to publish"
     * @param readyOrNot - by default is false
     */
    private fun onReadyToPublishOrNot(readyOrNot: Boolean?=false) {
        isReadyToPubish.value=readyOrNot
    }

    /*
    *********************************************************************************************
    * GETTER AND SETTER - CAN BE CALLED BY VIEW AND VIEWMODEL
    *********************************************************************************************/
    fun getTempDraft(): Draft? {
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

    override fun onStoreDraftSucceed() {}

    override fun onFailed() {}

    /*
     *********************************************************************************************
     * PublishTaskCallBack
     *********************************************************************************************/
    override fun onPublishSuccess() {
        onPublishSucceed.call()
    }

}
