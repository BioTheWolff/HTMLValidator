package biothewolff.htmlvalidator.core;

public enum ReaderStates
{
    IN_CLOSE_TAG,
    IN_COMMENT,
    IN_DOCTYPE,
    IN_OPEN_TAG,
    IN_TAG_UNKNOWN,
    OUT_OF_TAG,
    IDLE,
    WAITING_FOR_COMPLETION;
}
