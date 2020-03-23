package biothewolff.htmlvalidator.core.read;

import biothewolff.htmlvalidator.core.validate.util.ValidationError;
import biothewolff.htmlvalidator.core.validate.util.ValidationErrorTypes;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class is the main class which will read the code
 */
public class HTMLReader
{

    public final String[] text;
    public Tag document;

    private boolean hasBeenRead = false;

    // the current opened tags lists, in FILO mode
    private ArrayList<String> openedTagsNames = new ArrayList<>();
    private ArrayList<Tag> openedTagsClasses = new ArrayList<>();

    private ArrayList<ValidationError> validationErrors = new ArrayList<>();

    // self-closing tags list
    private ArrayList<String> self_closing_tags = new ArrayList<>(Arrays.asList(
            "area", "base", "br", "col", "embed", "hr",
            "img", "input", "link", "meta", "param", "source",
            "track", "wbr"
    ));

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

        if (!openedTagsNames.isEmpty())
            validationErrors.add(new ValidationError(ValidationErrorTypes.DOCUMENT_LEFT_OPEN, null));

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

                if (state.equals(ReaderStates.IN_OPEN_TAG)) evaluateRegisterOpenTag(beingRead.toString());

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

                evaluateRegisterCloseTag(beingRead.toString());

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

    // PURGES
    private void purgeBuffer()
    {
        buffer.delete(0, buffer.toString().length());
    }

    private void purgeContent()
    {
        beingRead.delete(0, beingRead.toString().length());
    }

    // LOGGING EVALUATORS
    private void evaluateRegisterOpenTag(String content)
    {
        String name, attrs;

        if (content.contains(" ")) {
            name = content.split(" ")[0];

            ArrayList<String> attrs_list = new ArrayList<>(Arrays.asList(content.split(" ")));
            attrs_list.remove(name);
            attrs = String.join(" ", attrs_list);
        }
        else
        {
            name = content;
            attrs = null;
        }

        registerOpenTag(name);
        putAttributesInTag(attrs);

        if (self_closing_tags.contains(name))
        {
            closeTagAndMergeWithParent();
        }
    }

    private void evaluateRegisterCloseTag(String content)
    {
        String name;

        if (beingRead.toString().contains(" ")) name = beingRead.toString().split(" ")[0];
        else name = content;

        if (self_closing_tags.contains(name))
        {
            validationErrors.add(new ValidationError(ValidationErrorTypes.VOID_ELEMENT_CLOSED, name));
            return;
        }

        int index = openedTagsNames.lastIndexOf(name);

        // if tag doesn't exist (isn't opened) or is not in the last index (tag closure skip)
        if (index == -1)
        {
            validationErrors.add(new ValidationError(ValidationErrorTypes.CLOSING_UNOPENED_TAG, name));
        }
        else if (index < openedTagsNames.size() - 1)
        {
            validationErrors.add(new ValidationError(ValidationErrorTypes.CLOSING_PARENT_BEFORE_CHILDREN, name));
        }
        else closeTagAndMergeWithParent();
    }

    // LOGGING TAGS
    private void registerOpenTag(String name)
    {
        openedTagsNames.add(name);
        openedTagsClasses.add(new Tag(name));
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

    // LOGGING TAG CONTENTS
    private void putAttributesInTag(@Nullable String attributes)
    {
        if (attributes == null) return;
        //TODO: SPLIT ATTRIBUTES
        openedTagsClasses.get(openedTagsNames.size()-1).addAttribute("attrs", attributes);
    }

    // DISPLAY
    public void displayDocument(int offset)
    {
        if (hasBeenRead && document != null) document.displayTag(0, offset);
    }

    public void displayPreValidationErrors()
    {
        if (validationErrors.isEmpty())
        {
            System.out.println("No pre-validation error found.");
            return;
        }

        System.out.println("--- Pre-validation errors ---");
        for (ValidationError e : validationErrors)
        {
            System.out.println(e);
        }
        System.out.println("--- END ---");
    }

    public boolean hasPreValidationErrors()
    {
        return hasBeenRead && !validationErrors.isEmpty();
    }
}
