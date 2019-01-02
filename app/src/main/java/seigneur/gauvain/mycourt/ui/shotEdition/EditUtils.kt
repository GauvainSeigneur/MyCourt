package seigneur.gauvain.mycourt.ui.shotEdition

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import android.text.Html

import java.io.File
import java.util.ArrayList
import java.util.Arrays
import java.util.regex.Matcher
import java.util.regex.Pattern

import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.MyTextUtils

/**
 *
 */
class EditUtils {
    companion object {

        /**
         * Manage description String
         * @param draft
         * @return
         */
        fun getDescription(draft: Draft): String? {
            return if (draft.shot.description != null) {
                //If user click on edit from a shot detail and this shot is not already in draft
                //- by check if draft id is equal to 0 - we must manage description text as html
                // to avoid <p> or <br> elements for example
                if (draft.draftID == 0L && draft.shot.id != null &&
                        !draft.shot.id!!.isEmpty()) {
                    Html.fromHtml(draft.shot.description).toString()
                } else {
                    draft.shot.description
                }//If the source of a the temporary draft is a draft itself do not treat text as html
            } else {
                null
            }
        }

        //get image uri from data sent by presenter
        fun getImageUrl(context: Context, draft: Draft): Uri? {
            return if (draft.imageUri != null) {
                if (draft.typeOfDraft == Constants.EDIT_MODE_NEW_SHOT) {
                    FileProvider.getUriForFile(
                            context,
                            context.getString(R.string.file_provider_authorities),
                            File(draft.imageUri!!))
                } else {
                    Uri.parse(draft.imageUri)
                }
            } else {
                null
            }

        }

        /**
         * get TagList and convert it in String  with Dribble pattern to send it in the right format
         * @return
         */
        fun getTagList(draft: Draft?): StringBuilder {
            var stringBuilder = StringBuilder()
            val tagList = draft!!.shot.tagList
            if (tagList != null)
                stringBuilder = adaptTagListToEditText(tagList)
            return stringBuilder
        }

        /**
         * Check if a tag contains more than one word, if true, add double quote to it,
         * @param tagList - list from Shot or ShotDraft
         * @return string from list with each item separated by a comma
         */
        private fun adaptTagListToEditText(tagList: ArrayList<String>?): StringBuilder {
            val listString = StringBuilder()
            val multipleWordTagPattern = Pattern.compile(MyTextUtils.multipleWordtagRegex)
            for (s in tagList!!) {
                var inS =s
                val wordMatcher = multipleWordTagPattern.matcher(s)
                if (!wordMatcher.matches()) {
                    inS = "\"" + s + "\""
                }
                listString.append("$inS, ")
            }
            return listString
        }

        /**
         *
         * @param tagString
         * @return
         */
        fun tagListWithoutQuote(tagString: String): ArrayList<String> {
            val listWithQuote = tempTagList(tagString)
            val output = arrayOfNulls<String>(listWithQuote.size)
            var builder: StringBuilder
            for (i in listWithQuote.indices) {
                builder = StringBuilder()
                output[i] = builder.toString()
                output[i] = listWithQuote[i].replace("\"".toRegex(), "")
            }

            return ArrayList(Arrays.asList<String>(*output))
        }

        //Create taglist according to Dribbble pattern
        private fun tempTagList(tagString: String?): ArrayList<String> {
            val tempList = ArrayList<String>()
            //create the list just one time, not any time the tags changed
            if (tagString != null && !tagString.isEmpty()) {
                val p = Pattern.compile(MyTextUtils.tagRegex)
                val m = p.matcher(tagString.toLowerCase())
                if (MyTextUtils.isDoubleQuoteCountEven(tagString)) {
                    // number is even or 0
                    while (m.find()) {
                        tempList.add(m.group(0))
                    }
                } else {
                    //todo-  number is odd: warn user and stop
                }
            }
            return tempList
        }
    }

}
