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
import seigneur.gauvain.mycourt.data.model.Draft;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.MyTextUtils;

/**
 *
 */
public class EditUtils {

    public EditUtils(){}

    /**
     * Manage description String
     * @param draft
     * @return
     */
    public static String getDescription(Draft draft) {
        if (draft.getShot()!=null && draft.getShot().getDescription() != null)  {
            //If user click on edit from a shot detail and this shot is not already in draft
            //- by check if draft id is equal to 0 - we must manage description text as html
            // to avoid <p> or <br> elements for example
            if (draft.getDraftID()==0 && draft.getShot().getId()!=null &&
                    !draft.getShot().getId().isEmpty()) {
                return Html.fromHtml(draft.getShot().getDescription()).toString();
            }
            //If the source of a the temporary draft is a draft itself do not treat text as html
            else {
                return draft.getShot().getDescription();
            }
        } else {
            return null;
        }
    }

    //get image uri from data sent by presenter
    public static Uri getImageUrl(Context context, Draft draft) {
        if (draft.getImageUri()!=null) {
            if (draft.getTypeOfDraft()==Constants.EDIT_MODE_NEW_SHOT) {
                Uri imageuri =FileProvider.getUriForFile(
                        context,
                        context.getString(R.string.file_provider_authorities),
                        new File(draft.getImageUri()));
                return imageuri;
            }
            else {
                return Uri.parse(draft.getImageUri());
            }
        } else {
            return null;
        }

    }

    /**
     * get TagList and convert it in String  with Dribble pattern to send it in the right format
     * @return
     */
    public static StringBuilder getTagList (@Nullable Draft draft){
        StringBuilder stringBuilder = new StringBuilder();
            ArrayList<String> tagList =draft.shot.getTagList();
            if (tagList!=null)
                stringBuilder =adaptTagListToEditText(tagList);
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
