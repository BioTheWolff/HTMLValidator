package biothewolff.htmlvalidator;

import biothewolff.htmlvalidator.core.Constants;
import biothewolff.htmlvalidator.core.read.HTMLReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static final Map<String, Boolean> arguments_map = new HashMap<String, Boolean>() {
        {
            put("crash-test", false);
            put("tree", false);
        }
    };
    private static final Map<String, String> arguments_display = new HashMap<String, String>() {
        {
            put("help", "Displays this help message and exits.");
            put("crash-test", "Throw an exception if the file contains errors and could not be validated.");
            put("tree", "display the document tree. May not be displayed if crash-test is provided.");
        }
    };

    public static void main(String[] args) throws Exception {
        // compute and store arguments
        computeArguments(new ArrayList<>(Arrays.asList(args)));

        if (args.length > 0 && args[0] != null) {
            // Grab file from arguments
            if (!args[0].endsWith(".html") && !args[0].endsWith(".htm"))
                throw new Exception("Wrong file extension (accepted: .htm, .html)");
            FileReader file = new FileReader(args[0]);

            // Try and read the file
            try (BufferedReader br = new BufferedReader(file)) {
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

                if (arguments_map.get("crash-test") && reader.hasPreValidationErrors()) {
                    throw new Exception("Found pre-validation errors.\n");
                }

                if (arguments_map.get("tree")) reader.displayDocument(2);
            }

        } else {
            System.out.println("No file path provided. Aborting.");
        }
    }

    public static void computeArguments(ArrayList<String> args) {
        if (args.isEmpty()) return;

        if (args.contains("-h") || args.contains("--help")) {
            displayHelp();
            System.exit(0);
        }

        for (String a : args) {
            // we take out the prefix
            String name = a.replace("--", "");
            if (arguments_map.containsKey(name)) arguments_map.put(name, true);
        }
    }

    public static void displayHelp() {

        // Build the optional args strings
        StringBuilder compact_opt_args = new StringBuilder();
        StringBuilder smooth_opt_args = new StringBuilder();

        for (String arg : arguments_display.keySet()) {
            String val = arguments_display.get(arg);

            compact_opt_args.append(String.format("[--%s] ", arg));
            smooth_opt_args.append(String.format("    %s - %s\n", arg, val));
        }

        // Build the general display message
        String sb = String.format("%s v%s - Licensed under %s\n", Constants.name, Constants.version, Constants.license) +
                String.format("  Author: %s\n", Constants.author) +
                String.format("  %s\n\n", Constants.description) +
                String.format("java -jar HTMLValidator.jar <filename> %s\n\n", compact_opt_args.toString()) +
                "Positional argument:" +
                "\n" +
                "    filename - the name of the file to validate" +
                "\n" +
                "Optional arguments:\n" +
                smooth_opt_args.toString();
        System.out.println(sb);

    }
}
