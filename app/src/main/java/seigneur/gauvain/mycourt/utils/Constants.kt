package seigneur.gauvain.mycourt.utils

import androidx.annotation.IntDef


object Constants {

        /**
         * API
         */
        const val HEADER_CACHE = "android-cache"
        const val RESPONSE_CACHE_DELAY = "ResponseCacheDelay"
        const val HEADER_SHOT_ID = "ShotID"

        /**
         * INTERNAL CONSTANTS
         */
        const val CUSTOM_ACTION_MODE_OFF = 40
        const val CUSTOM_ACTION_MODE_ON  = 41

        const val PIN_STEP_CHECK_STORED = 10
        const val PIN_STEP_NEW_PIN_ONE = 11
        const val PIN_STEP_NEW_PIN_TWO = 12

        const val REQUEST_STORAGE_READ_ACCESS_PERMISSION = 1001
        const val REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 1002
        const val PICK_IMAGE_REQUEST = 1003
        const val PICK_ATTACHMENT_REQUEST = 1004

        const val SECRET_PWD_ALIAS = "SecretPwdAlias"

        /**
         * Social
         */
        const val INSTAGRAM = "instagram"
        const val FACEBOOK = "facebook"
        const val GITHUB = "github"
        const  val TWITTER = "twitter"
        const  val CREATIVEMARKET = "creative market"
        const val MEDIUM = "medium"
        const val BEHANCE = "behance"
        const val LINKEDIN = "linkedin"

        @Retention(AnnotationRetention.RUNTIME)
        @IntDef(EDIT_MODE_NEW_SHOT, EDIT_MODE_UPDATE_SHOT)
        annotation class EditionMode
        const val EDIT_MODE_NEW_SHOT = 2001
        const val EDIT_MODE_UPDATE_SHOT = 2002

        @Retention(AnnotationRetention.RUNTIME)
        @IntDef(EDIT_PUBLISHED, DRAFT_PUBLISHED, EDIT_DRAFTED, EDIT_ABORTED)
        annotation class EditResult
        const val EDIT_PUBLISHED = 601
        const val DRAFT_PUBLISHED = 602
        const val EDIT_DRAFTED = 603
        const val EDIT_ABORTED = 604

        @Retention(AnnotationRetention.RUNTIME)
        @IntDef(SOURCE_DRAFT, SOURCE_SHOT, SOURCE_FAB)
        annotation class DraftCallingSource
        const val SOURCE_DRAFT = 701 //from an already saved draft
        const val SOURCE_SHOT = 702 //from a shot
        const val SOURCE_FAB = 703 //from a fab (new draft)

        @Retention(AnnotationRetention.RUNTIME)
        @IntDef(CROP_MODE_HD, CROP_MODE_NORMAL, CROP_MODE_GIF_INCORRET_FORMAT)
        annotation class ImageCroppingMode
        const val CROP_MODE_HD = 801 //800*600
        const val CROP_MODE_NORMAL = 802 //400*300
        const val CROP_MODE_GIF_INCORRET_FORMAT = 803 //gif can't be cropped, so it must directly correct format

        @Retention(AnnotationRetention.RUNTIME)
        @IntDef(BAD_REQUEST, UNAUTHENTICATED_USER, ACCESS_FORBIDDEN, PAGE_NOT_FOUND)
        annotation class HttpErrors
        //todo - error 500
        const val BAD_REQUEST = 400
        const val UNAUTHENTICATED_USER = 401
        const val ACCESS_FORBIDDEN = 403
        const val PAGE_NOT_FOUND = 404

        @Retention(AnnotationRetention.RUNTIME)
        @IntDef(REQUEST_OK, ACCEPTED)
        annotation class HttpSuccess
        const val REQUEST_OK = 200
        const val ACCEPTED = 202 //for post request


}
