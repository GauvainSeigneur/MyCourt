package seigneur.gauvain.mycourt.ui.shotEdition;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.Html;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.MyTextUtils;

/**
 *
 */
public class EditUtils {

    public EditUtils(){}

    public static int getDraftType(@Nullable Object object) {
        if (object instanceof ShotDraft) {
            ShotDraft shotDraft = (ShotDraft) object;
            return shotDraft.getDraftType();
        } else {
            return -1;
        }
    }

    /**
     * get title send by object
     * @param object
     * @return
     */
    public static String getTitle(@Nullable Object object){
        if (object instanceof Shot) {
            Shot shot = (Shot) object;
            return shot.getTitle();
        } else if(object instanceof ShotDraft) {
            ShotDraft shotDraft = (ShotDraft) object;
            return shotDraft.getTitle();
        } else {
            return null;
        }
    }

    //get image uri from data sent by presenter
    public static Uri getImageUrl(Context context, @Nullable Object object) {
        if (object instanceof Shot){
            Shot shot = (Shot) object;
            return Uri.parse(shot.getImageUrl());
        }
        else if (object instanceof ShotDraft) {
            ShotDraft shotDraft = (ShotDraft) object;
            if (shotDraft.getImageUrl()!=null) {
                if (shotDraft.getDraftType()==Constants.EDIT_MODE_NEW_SHOT) {
                    Uri imageuri =FileProvider.getUriForFile(
                            context,
                            context.getString(R.string.file_provider_authorities),
                            new File(shotDraft.getImageUrl()));
                    return imageuri;
                }
                else {
                    return Uri.parse(shotDraft.getImageUrl());
                }
            }
            else
                return null;
        }
        else
            return null;
    }

    public static String getImageFormat (@Nullable Object object) {
        if (object instanceof ShotDraft) {
            ShotDraft shotDraft = (ShotDraft) object;
            return shotDraft.getImageFormat();
        }
        else
            return null;
    }


    /**
     *
     * @param object
     * @return
     */
    public static String getDescription(@Nullable Object object){
        String desc = null;
        if (object instanceof Shot) {
            Shot shot = (Shot) object;
            desc= Html.fromHtml(shot.getDescription()).toString();
        } else if(object instanceof ShotDraft) {
            ShotDraft shotDraft = (ShotDraft) object;
            desc =  shotDraft.getDescription();
        }
        return desc;
    }

    /**
     * get TagList and convert it in String  with Dribble pattern to send it in the right format
     * @param object
     * @return
     */
    public static StringBuilder getTagList (@Nullable Object object){
        StringBuilder stringBuilder = new StringBuilder();
        if (object instanceof Shot) {
            Shot shot = (Shot) object;
            stringBuilder = adaptTagListToEditText(shot.getTagList());
        } else if(object instanceof ShotDraft) {
            ShotDraft shotDraft = (ShotDraft) object;
            ArrayList<String> tagList =shotDraft.getTagList();
            if (tagList!=null)
                stringBuilder =adaptTagListToEditText(tagList);
        }
        return stringBuilder;
    }

    /**
     * Check if a tag contains more than one word, if true, add double quote to it,
     * @param tagList - list from Shot or ShotDraft
     * @return string from list with each item separated by a comma
     */
    private static StringBuilder adaptTagListToEditText (@Nullable ArrayList<String> tagList) {
        StringBuilder listString = new StringBuilder();
        Pattern multipleWordTagPattern = Pattern.compile(MyTextUtils.multipleWordtagRegex);
        for (String s : tagList) {
            Matcher wordMatcher = multipleWordTagPattern.matcher(s);
            if (!wordMatcher.matches()) {
                s = "\""+ s +"\"";
            }
            listString.append(s+", ");
        }
        return listString;
    }

    /**
     *
     * @param tagString
     * @return
     */
    public static ArrayList<String> tagListWithoutQuote(String tagString) {
        ArrayList<String> listWithQuote = tempTagList(tagString);
        String[] output = new String[listWithQuote.size()];
        StringBuilder builder;
        for (int i = 0; i < listWithQuote.size(); i++) {
            builder = new StringBuilder();
            output[i] = builder.toString();
            output[i] = listWithQuote.get(i).replaceAll("\"", "");
        }

        return new ArrayList<>(Arrays.asList(output));
    }

    //Create taglist according to Dribbble pattern
    private static ArrayList<String> tempTagList(String tagString) {
        ArrayList<String> tempList = new ArrayList<>();
        //create the list just one time, not any time the tags changed
        if (tagString != null && !tagString.isEmpty()) {
            Pattern p = Pattern.compile(MyTextUtils.tagRegex);
            Matcher m = p.matcher(tagString.toLowerCase());
            if (MyTextUtils.isDoubleQuoteCountEven(tagString)) {
                // number is even or 0
                while (m.find()) {
                    tempList.add(m.group(0));
                }
            } else {
                //todo-  number is odd: warn user and stop
            }
        }
        return tempList;
    }

}
