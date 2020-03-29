package biothewolff.htmlvalidator.ui;

import javax.swing.*;
import java.io.*;

public class Utils {

    public static String handleFileChooser(JFrame f) throws FileNotFoundException {
        JFileChooser fc = new JFileChooser();
        int i = fc.showOpenDialog(f);

        if (i == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            FileReader fr = new FileReader(file);

            try (BufferedReader br = new BufferedReader(fr)) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }

                return sb.toString();
            } catch (IOException e) {
                return null;
            }
        }

        return null;
    }

}
