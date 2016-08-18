package com.qiscus.library.chat.util;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class WordUtil {
    public static String capsFirstLetter(String string) {
        return Character.toString(string.charAt(0)).toUpperCase() + string.substring(1);
    }

    public static String capsFirstLetterEachWord(String sentence) {
        String words[] = sentence.split(" ");
        String newSentence = "";
        for (String word : words) {
            newSentence += capsFirstLetter(word) + " ";
        }
        return newSentence.trim();
    }
}
