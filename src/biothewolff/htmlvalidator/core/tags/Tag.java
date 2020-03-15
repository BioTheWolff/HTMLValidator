package biothewolff.htmlvalidator.core.tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The basic Tag in html document.
 */
public class Tag
{

    public final String name;
    public ArrayList<Tag> list;
    public Map<String, String> attributes = new HashMap<>();

    // Constructor
    public Tag(String name)
    {
        this.name = name;
        this.list = new ArrayList<>();
    }

    // Checkers
    public boolean hasChildren() { return !list.isEmpty(); }

    // Modifiers
    public void addChild(Tag toAdd)
    {
        this.list.add(toAdd);
    }

    public boolean addAttribute(String name, String value)
    {
        boolean flag = false;
        if (this.attributes.containsKey(name)) flag = true;

        this.attributes.putIfAbsent(name, value);

        return flag;
    }
}
