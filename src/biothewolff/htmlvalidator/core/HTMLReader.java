package biothewolff.htmlvalidator.core;

import biothewolff.htmlvalidator.core.tags.Tag;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is the main class which will read the code
 */
public class HTMLReader
{

    public final String[] text;
    public Tag document;

    private boolean hasBeenRead = false;

    // the current opened tags list, in FILO mode
    private ArrayList<String> openedTagsNames = new ArrayList<>();
    private ArrayList<Tag> openedTagsClasses = new ArrayList<>();

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
        for (String s : text)
        {
            evaluateChar(s);
        }

        hasBeenRead = true;
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
                    state = ReaderStates.IN_OPEN_TAG;
                    beingRead.append(s);
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
                //System.out.println("open/doctype tag content: " + beingRead.toString());

                if (state.equals(ReaderStates.IN_OPEN_TAG))
                {
                    String name, attrs;

                    if (beingRead.toString().contains(" ")) {
                        name = beingRead.toString().split(" ")[0];

                        ArrayList<String> attrs_list = new ArrayList<>(Arrays.asList(beingRead.toString().split(" ")));
                        attrs_list.remove(name);
                        attrs = String.join(" ", attrs_list);
                    }
                    else
                    {
                        name = beingRead.toString();
                        attrs = null;
                    }

                    logOpenTag(name);
                    putAttributesInTag(attrs);
                }

                purgeContent();
                state = ReaderStates.OUT_OF_TAG;
            }
            else {
                beingRead.append(s);
            }
        }
        // in close tag
        else if (state.equals(ReaderStates.IN_CLOSE_TAG))
        {
            if (">".equals(s)) {
                state = ReaderStates.OUT_OF_TAG;
                //System.out.println("close tag content: " + beingRead.toString());

                String name;

                if (beingRead.toString().contains(" "))
                {
                    name = beingRead.toString().split(" ")[0];
                }
                else
                {
                    name = beingRead.toString();
                }

                evaluateTagClosure(name, null);

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

    // LOGGING
    private void logOpenTag(String name)
    {
        openedTagsNames.add(name);
        openedTagsClasses.add(new Tag(name));
    }

    private void putAttributesInTag(@Nullable String attributes)
    {
        if (attributes == null) return;
        //TODO: SPLIT ATTRIBUTES
        openedTagsClasses.get(openedTagsNames.size()-1).addAttribute("attrs", attributes);
    }

    private void putContentInTag(@Nullable String content)
    {

    }

    /**
     * This function evaluates if the tag can be closed and merged into parent or is an error because wasn't opened
     * @param name the name of the tag
     * @param content the attributes of the tag, non parsed
     */
    private void evaluateTagClosure(String name, @Nullable String content)
    {
        int index = openedTagsNames.lastIndexOf(name);

        // if tag doesn't exist (isn't opened) or is not in the last index (tag closure skip)
        if (index == -1 || index < openedTagsNames.size() - 1)
        {
            System.out.println("wrongly placed closure " + name);
        }
        else
        {
            putContentInTag(content);
            closeTagAndMergeWithParent();
        }
    }

    private void closeTagAndMergeWithParent()
    {
        if (openedTagsNames.size() == 1)
        {
            // only one tag in the list, should be HTML tag
            document = openedTagsClasses.get(0);

            openedTagsNames.remove(0);
            openedTagsClasses.remove(0);
        }
        else
        {
            int parent_index = openedTagsNames.size()-2;
            int child_index = openedTagsNames.size()-1;

            Tag parent = openedTagsClasses.get(parent_index);
            Tag child = openedTagsClasses.get(child_index);

            parent.addChild(child);

            openedTagsNames.remove(child_index);
            openedTagsClasses.remove(child_index);
        }

    }

    public void displayDocument(int offset)
    {
        if (hasBeenRead && document != null) document.displayTag(0, offset);
    }
}
