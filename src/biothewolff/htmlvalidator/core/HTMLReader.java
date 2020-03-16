package biothewolff.htmlvalidator.core;

import biothewolff.htmlvalidator.core.tags.HTMLDocument;
import biothewolff.htmlvalidator.core.tags.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is the main class which will read the code
 */
public class HTMLReader
{

    public final String[] text;
    private boolean hasBeenRead = false;

    // the current opened tags list, in FILO mode
    private Map<String, Tag> currentOpenedTags = new HashMap<>();

    /*
    the two string builders.
    The first one is used to buffer the chars to take action (<!--, --!>, </, etc)
    The second one is used to buffer the current text. Will be saved elsewhere and emptied with each action took
     */
    private StringBuilder buffer = new StringBuilder();
    private StringBuilder beingRead = new StringBuilder();
    private ReaderStates state;

    public HTMLReader(String toRead)
    {
        text = toRead.split("");
        state = ReaderStates.IDLE;
    }

    public void readAndParse()
    {
        HTMLDocument document = new HTMLDocument();

        for (String s : text)
        {
            evaluateChar(s);
        }
    }

    private void evaluateChar(String s)
    {

        // initialise system
        if (state.equals(ReaderStates.IDLE)) state = ReaderStates.OUT_OF_TAG;

        // Out of tag
        if (state.equals(ReaderStates.OUT_OF_TAG))
        {
            switch (s)
            {
                case "<":
                    state = ReaderStates.IN_TAG_UNKNOWN;
                    break;
                case ">":
                    System.out.println("closing found out of tag");
                    break;
            }
        }
        // Saw a start of tag ( < ) and waiting to define its type
        else if (state.equals(ReaderStates.IN_TAG_UNKNOWN))
        {
            switch (s)
            {
                case "!":
                    state = ReaderStates.WAITING_FOR_COMPLETION;
                    buffer.append("<!");
                    break;
                case "/":
                    state = ReaderStates.IN_CLOSE_TAG;
                    break;
                default:
                    state = ReaderStates.IN_OPEN_TAG; beingRead.append(s);
                    break;
            }
        }
        // Waiting for completion of comment tag
        else if (state.equals(ReaderStates.WAITING_FOR_COMPLETION))
        {
            String bstr = buffer.toString();

            // HTML comment
            if ("-".equals(s))
            {
                if ("<!".equals(bstr) || "<!-".equals(bstr))
                {
                    buffer.append(s);
                }
                else if ("<!--".equals(bstr))
                {
                    state = ReaderStates.IN_COMMENT;
                }
                else System.out.println("Wrong comment type");
            }

            // buffer should be containing <!DOCTYPE by now
            if (bstr.length() == 9) {
                if (!bstr.equals("<!DOCTYPE")) System.out.println("Wrong doctype");
                else state = ReaderStates.IN_DOCTYPE; purgeBuffer();
            }
            else
            {
                // add to buffer
                buffer.append(s);
            }

        }
        // in opened tag
        else if (state.equals(ReaderStates.IN_OPEN_TAG) || state.equals(ReaderStates.IN_DOCTYPE))
        {
            if (">".equals(s)) {
                state = ReaderStates.OUT_OF_TAG;
                System.out.println("open/doctype tag content: " + beingRead.toString());
                purgeContent();
            } else {
                beingRead.append(s);
            }
        }
        // in close tag
        else if (state.equals(ReaderStates.IN_CLOSE_TAG))
        {
            if (">".equals(s)) {
                state = ReaderStates.OUT_OF_TAG;
                System.out.println("close tag content: " + beingRead.toString());
                purgeContent();
            } else {
                beingRead.append(s);
            }
        }
        // in comment
        else if (state.equals(ReaderStates.IN_COMMENT))
        {
            String bstr = buffer.toString();

            if ("-".equals(s) || ">".equals(s) || !"".equals(bstr))
            {
                // if we are in "" or "-" case and s equals "-"
                if (( "".equals(bstr) || "-".equals(bstr) ) && "-".equals(s)) buffer.append(s);
                // if we have "--" in buffer and s equals ">" this is a close comment
                else if ("--".equals(bstr) && ">".equals(s)) state = ReaderStates.OUT_OF_TAG;
                // this is a normal comment with two signs, do nothing and purge the buffer
                else purgeBuffer();
            }
        }

    }

    private void purgeBuffer()
    {
        buffer.delete(0, buffer.toString().length());
    }

    private void purgeContent()
    {
        beingRead.delete(0, beingRead.toString().length());
    }
}
