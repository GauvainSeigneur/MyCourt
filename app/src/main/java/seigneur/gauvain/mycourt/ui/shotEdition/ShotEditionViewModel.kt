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
import seigneur.gauvain.mycourt.utils.*
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

    //Task class
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
    var imagePickedFormat: String? = "png" //NOT LIVEDATA - NOT RELATED TO UI
    var imageSize: IntArray? = null //NOT LIVEDATA - NOT RELATED TO UI
    private val croppedImageUri = MutableLiveData<Uri>()
    val pickCropImgErrorCmd = SingleLiveEvent<Int>()
    val isReadyToPubish     = MutableLiveData<Boolean>()
    //Listen change in editText
    private val mTitle = MutableLiveData<String>()
    private val mDescription = MutableLiveData<String>()
    private val mTags = MutableLiveData<ArrayList<String>>()
    //List of attachment
    private val mTempAttachmentList = ArrayList<Attachment>() //Non UI list
    private val mAttachmentUIList= MutableLiveData<ArrayList<Attachment>>() //UI list
    private val mTempAttachmentsToDelete = ArrayList<Attachment>()//Non UI list to keep reference of attachment that they needs to be deleted
    private var hasAttachment =false

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
        Timber.d("imagecroppedUri: $uri")
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
        //mPublishTask.postAttachments("5381467", mApplication, attachment!!)
        updateAttachmentList(attachment, null, Constants.ADD_ATTACHMENT_OPE)
    }

    fun onRemoveAttachment(pos: Int) {
        programDeletePOST(pos)
        updateAttachmentList(null, pos, Constants.REMOVE_ATTACHMENT_OPE)
    }

    fun requestPerm() {
        requestPermCmd.call()
    }

    fun onPermGranted() {
        //registerOrUpdateDraft(mApplication, true)
    }

    fun onStoreDraftClicked() {
        if (title.value == null || title.value!!.isEmpty()) {
            //TODO - single live event
            Timber.d("niquetamere")
        } else {
            registerOrUpdateDraft(mApplication, true)
           /* if (getCroppedImageUri().value != null && imagePickedFormat != null) {
                checkPerm.call()
            } else {
                registerOrUpdateDraft(mApplication, false)
            }*/
        }
    }

    fun onPublishClicked() {
        changeDraftInfo()
        //Timber.d("checkAttachment: "+mTempAttachmentList[0].uri)
        //Timber.d("checkPostShot: "+ getCroppedImageUri().value!!)
        //mPublishTask.postAttachments("5381467",mApplication , mTempAttachmentList)
        if (mTempDraft!!.typeOfDraft == Constants.EDIT_MODE_UPDATE_SHOT) {
            mPublishTask.updateShot(
                    mTempDraft!!,
                    false)
        } else
            mPublishTask.postShot(
                    mTempDraft!!,
                    mApplication)
    }

    /*
    *********************************************************************************************
    * LIVEDATA GETTER CALLED BY UI
    *********************************************************************************************/
    fun getTempDraft(): Draft? {
        return mTempDraft
    }

    val title: LiveData<String>
        get() = mTitle

    val description: LiveData<String>
        get() = mDescription

    val tags: LiveData<ArrayList<String>>
        get() = mTags

    val readytoPublish: LiveData<Boolean>
        get() = isReadyToPubish

    val attachmentsTobeUploaded : LiveData<ArrayList<Attachment>>
        get() = mAttachmentUIList

    fun getCroppedImageUri(): LiveData<Uri> {
        return croppedImageUri
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
        Timber.d("tempDraft created : " + mTempDraft!!.shot.attachment)
    }

    /*
    *********************************************************************************************
    * StoreTaskCallback
    *********************************************************************************************/
    override fun onSaveImageSuccess(uri: String) {
        changeDraftInfo(uri)
        storeDraft()
    }

    override fun onStoreDraftSucceed() {
        Timber.d("store draft succeed")
    }

    override fun onFailed() {}

    /*
    *********************************************************************************************
    * PublishTaskCallBack
    *********************************************************************************************/
    override fun onPublishSuccess() {
        onPublishSucceed.call()
    }

    /*
    *********************************************************************************************
    * PRIVATE METHODS
    *********************************************************************************************/
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
        return isReady!!
    }

    /**
     * Change value of "ready to publish"
     * @param readyOrNot - by default is false
     */
    private fun onReadyToPublishOrNot(readyOrNot: Boolean?=false) {
        isReadyToPubish.value=readyOrNot
    }

    private fun programDeletePOST(position:Int) {
        //if attachment id is different than -1L, is an published on, so we have to perform
        //DELETE POST if user delete it from UI
        if (mTempAttachmentList[position].id != -1L) {
            //create temporary attachment object
            val tempAttachmentToDelete = mTempAttachmentList[position]
            //inject to it the current shot id to perform Delete POST on Dribbble
            mTempDraft?.shot?.id?.let {
                tempAttachmentToDelete.shotId = mTempDraft!!.shot.id
            }
            //finally add to delete list
            mTempAttachmentsToDelete.add(tempAttachmentToDelete)
        }
    }

    private fun updateAttachmentList(newAttachment:Attachment?, position:Int?, operationType: Int) {
        //ui and list to be uploaded
        when (operationType) {
           Constants.ADD_ATTACHMENT_OPE ->  mTempAttachmentList.add(newAttachment!!)
            else   ->  mTempAttachmentList.removeAt(position!!)
        }
        //notify UI
        mAttachmentUIList.value = mTempAttachmentList
        //check if the list include new shots to program or not Pubish Attachment POST on DRIBBBLE
        hasAttachment = mTempAttachmentList.any { it -> it.id == -1L }

        Timber.d("has attachment: "+ hasAttachment)
    }


    private fun registerOrUpdateDraft(context: Context?, isRegisteringImage: Boolean) {
        if (isRegisteringImage) {
            mStoreDrafTask.storeDraftImage(
                    context!!,
                    imagePickedFormat!!,
                    getCroppedImageUri().value!!)
        } else {
            changeDraftInfo()
            storeDraft()
        }
    }

    private fun changeDraftInfo(croppedImageUri: String?= mTempDraft!!.imageUri) {
        //define image format according to cropping state
        fun imageFormat():String? {
            val format:String?
            if (croppedImageUri==mTempDraft?.imageUri) {
                format = mTempDraft?.imageFormat //Image doesn't change
            } else {
                format = imagePickedFormat //Image change
            }
            return format
        }

        mTempDraft!!.changeInfoFromEdit(
                croppedImageUri,
                imageFormat(),
                title.value,
                description.value,
                tags.value,
                mTempAttachmentList)

    }

    private fun storeDraft() {
        if (mTempDraft!!.draftID == 0L) {
            //new draft, so save it in db
            mStoreDrafTask.save(mTempDraft!!)
        } else {
            //it is draft fetch from db, update it
            mStoreDrafTask.update(mTempDraft!!)
        }
    }


}
