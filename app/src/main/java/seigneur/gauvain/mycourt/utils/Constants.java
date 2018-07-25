package seigneur.gauvain.mycourt.utils;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Constants {
    /**
     * API
     */
    public static final String HEADER_CACHE = "android-cache";
    public static final String RESPONSE_CACHE_DELAY = "ResponseCacheDelay";
    public static final String HEADER_SHOT_ID = "ShotID";

    /**
     * INTERNAL CONSTANTS
     */
    public static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    public static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;

    public static final int NO_TEAM_ID = -1;

    //public static final int PICK_IMAGE_ID = 1001; // the number doesn't matter
    public static final int PICK_IMAGE_REQUEST = 1001; // the number doesn't matter

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EDIT_MODE_NEW_SHOT, EDIT_MODE_UPDATE_SHOT})
    public @interface EditionMode {}
    public static final int EDIT_MODE_NEW_SHOT = 501;
    public static final int EDIT_MODE_UPDATE_SHOT = 502;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EDIT_PUBLISHED,DRAFT_PUBLISHED, EDIT_DRAFTED, EDIT_ABORTED})
    public @interface EditResult {}
    public static final int EDIT_PUBLISHED = 601;
    public static final int DRAFT_PUBLISHED = 602;
    public static final int EDIT_DRAFTED = 603;
    public static final int EDIT_ABORTED = 604;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SOURCE_DRAFT, SOURCE_SHOT, SOURCE_FAB})
    public @interface DraftCallingSource {}
    public static final int SOURCE_DRAFT            = 701; //from an already saved draft
    public static final int SOURCE_SHOT             = 702; //from a shot
    public static final int SOURCE_FAB              = 703; //from a fab (new draft)

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EDIT_MODE_NEW_SHOT, EDIT_MODE_UPDATE_SHOT})
    public @interface ImageCroppingMode {}
    public static final int CROP_MODE_HD = 801; //800*600
    public static final int CROP_MODE_NORMAL = 802; //400*300
    public static final int CROP_MODE_GIF_INCORRET_FORMAT = 803; //gif can't be cropped, so it must directly correct format

    public static final String INSTAGRAM = "instagram";
    public static final String FACEBOOK = "facebook";
    public static final String GITHUB = "github";
    public static final String TWITTER = "twitter";
    public static final String CREATIVEMARKET = "creative market";
    public static final String MEDIUM = "medium";
    public static final String BEHANCE = "behance";
    public static final String LINKEDIN = "linkedin";

    //pagination
    public static final int PAGE_START = 1;
    public static int CURRENT_PAGE = PAGE_START;
    public static final int PER_PAGE = 30;

    //use it like this in activity
    /*@EditionMode
    public abstract int getEditionMode();
    // Attach the annotation
    public abstract void setEditionMode(@EditionMode int mode);*/

}
