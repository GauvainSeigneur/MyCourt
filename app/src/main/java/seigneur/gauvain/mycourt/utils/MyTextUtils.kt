package seigneur.gauvain.mycourt.utils

class MyTextUtils {

    companion object {


        /**
         * remove <br> from html text
         * @param text
         * @return
         */
        fun noTrailingwhiteLines(text:CharSequence):CharSequence {
            var inText = text
            if (inText.isNotEmpty()) {
                while (inText[inText.lastIndex] == '\n') {
                    inText = text.subSequence(0, inText.length - 1)
                }
                return inText
            } else {
                return ""
            }
        }

        //Check if words are between two double quotes
        //if true, don't split them if they are separated by space or comma
        //if false, split them if they are separated by space or comma
        var tagRegex = "(\"([^\"]*)\"|[^, ]+)"
        //for checking single quote too
        //String tagRegex = "(\"([^\"]*)\"|'([^']*)|[^, ]+)";
        var multipleWordtagRegex = "\\w+"
        private val doublequote = '\"'

        /**
         * Check if the string contains even or odd double quote
         * @param text to be verified
         * @return boolean
         */
        fun isDoubleQuoteCountEven(text: String): Boolean {
            val doubleQuoteCount = text.replace("[^$doublequote]".toRegex(), "").length
            return doubleQuoteCount == 0 || doubleQuoteCount % 2 == 0
        }
    }
}
