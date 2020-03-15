package biothewolff.htmlvalidator.core.tags;

/**
 * The handmade html document class decoded from raw html or file
 */
public class HTMLDocument
{
    public final Tag headTag;
    public final Tag bodyTag;

    public HTMLDocument()
    {
        headTag = new Tag("head");
        bodyTag = new Tag("body");
    }
}
