package biothewolff.htmlvalidator.core.read;

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
    public void addChild(Tag toAdd) { this.list.add(toAdd); }

    public boolean addAttribute(String name, String value)
    {
        boolean flag = false;
        if (this.attributes.containsKey(name)) flag = true;

        this.attributes.putIfAbsent(name, value);

        return flag;
    }

    @Override
    public String toString()
    {
        return "Tag{" +
                "name='" + name + '\'' +
                '}';
    }

    public void displayTag(int indent, int offset)
    {
        // build new
        String display_offset = "";
        if (indent > 0) display_offset = new String(new char[indent]).replace("\0", " ");

        // display name (and attributes if they exist)
        if (attributes.containsKey("attrs"))
        {
            System.out.println(display_offset + "└─ " + name + " (" + attributes.get("attrs") + ")");
        }
        else
        {
            System.out.println(display_offset + "└─ " + name);
        }

        if (hasChildren()) {
            // has children, so we loop through the children and call the same function
            for (Tag child : list) {
                child.displayTag(indent + offset, offset);
            }
        }

    }
}
