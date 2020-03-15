package biothewolff.htmlvalidator.core;

import java.io.File;
import java.util.ArrayList;

/**
 * This class is the main class which will read the code
 */
public class HTMLReader
{

    public final String[] text;
    private boolean hasBeenRead = false;

    public HTMLReader(String toRead) { text = toRead.split(""); }

    public void readAndParse()
    {
        // the current opened tags list, in FILO mode
        ArrayList<String> currentOpenedTags = new ArrayList<>();

        // defines if the text has to be read as being in <> or not
        boolean inTag = false;
        boolean inCloseTag = false;

        /*
        the two string builders.
        The first one is used to buffer the chars to take action (<!--, --!>, </, etc)
        The second one is used to buffer the current text. Will be saved elsewhere and emptied with each action took
         */
        StringBuilder buffer = new StringBuilder();
        StringBuilder beingRead = new StringBuilder();

        for (String s : text) {

        }
    }
}
