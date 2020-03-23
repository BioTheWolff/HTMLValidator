package biothewolff.htmlvalidator;

import biothewolff.htmlvalidator.core.read.HTMLReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Main
{
    public static void main(String[] args) throws Exception
    {

        ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));

        if (args.length > 0 && args[0] != null)
        {
            // Grab file from arguments
            if (!args[0].endsWith(".html") && !args[0].endsWith(".htm")) throw new Exception("Wrong file extension (accepted: .htm, .html)");
            FileReader file = new FileReader(args[0]);

            // Try and read the file
            try(BufferedReader br = new BufferedReader(file)) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String file_text = sb.toString();

                HTMLReader reader = new HTMLReader(file_text);
                reader.readAndParse();

                reader.displayPreValidationErrors();

                if (arguments.contains("--crash-test") && reader.hasPreValidationErrors())
                {
                    throw new Exception("Found pre-validation errors.\n");
                }

                reader.displayDocument(2);
            }

        }
        else
        {
            // Launch UI
            System.out.println("test");
        }
    }
}
