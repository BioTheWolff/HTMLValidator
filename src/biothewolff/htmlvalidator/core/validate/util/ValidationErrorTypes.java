package biothewolff.htmlvalidator.core.validate.util;

public enum ValidationErrorTypes {

    // errors
    DOCUMENT_LEFT_OPEN(0, "A tag has not been closed"),
    VOID_ELEMENT_CLOSED(0, "A void element (self-closing tag) has been closed"),

    // warnings
    CLOSING_UNOPENED_TAG(1, "A never opened tag was closed"),
    CLOSING_PARENT_BEFORE_CHILDREN(1, "The parent tag was closed before children were");

    private final int level;
    private final String display;

    ValidationErrorTypes(int level, String display) {
        this.level = level;
        this.display = display;
    }

    // DISPLAY
    public String getDisplay() {
        return this.display;
    }

    // ERROR LEVEL
    public int getLevel() {
        return this.level;
    }

    public boolean isError() {
        return this.level == 0;
    }

    public boolean isWarning() {
        return this.level == 1;
    }

}
