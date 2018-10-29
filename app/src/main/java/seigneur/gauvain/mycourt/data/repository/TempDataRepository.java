package seigneur.gauvain.mycourt.data.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import seigneur.gauvain.mycourt.data.model.Draft;
import seigneur.gauvain.mycourt.data.model.Shot;

/**
 * Class used to get reference of data which are not
 * saved in DB or SharedPrefs
 * eg. data send between presenters
 */
@Singleton
public class TempDataRepository {

    //Used to temporarily store Shot in order to send it between presenter...
    public Shot shot;
    //Used to temporarily store ShotDraft in order to send it between presenter...
    public Draft mShotDraft;

    //Set to define the source of the edition to register the Draft in the right way
    public int mDraftCallingSource;

    //Used to notify that change has been made on Draft DB
    //public boolean mDraftsChanged=false;

    @Inject
    public TempDataRepository() {}

    public void setShot(Shot inShot){
        shot=inShot;
    }

    public Shot getShot() {return shot;}

    public void setShotDraft(Draft shotDraft){
        mShotDraft=shotDraft;
    }

    public Draft getShotDraft() {return mShotDraft;}

    public void setDraftCallingSource(int draftCallingSource) {
        mDraftCallingSource= draftCallingSource;
    }

    public int getDraftCallingSource() {
        return mDraftCallingSource;
    }

}
