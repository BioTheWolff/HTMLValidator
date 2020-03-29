package biothewolff.htmlvalidator.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

public class MainLayout implements ActionListener {

    public final JFrame f;
    public JButton b;
    public String text;
    public final int w = 400;
    public final int h = 500;

    public MainLayout() {

        f = new JFrame();

        // makes sure the program exits on window close
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // setup all kind of things
        f.setTitle("HTML Validator");
        f.setLayout(null);
        f.setSize(w, h);

        // draw assets
        drawTitle();
        drawFileChooserButton();

        // make it running in foreground
        f.setVisible(true);

    }

    private void drawTitle() {
        JLabel t = new JLabel("HTML Validator - by Fabien \"BioTheWolff\" Z.");
        t.setBounds(w/6, 0, w, 50);
        t.setVerticalAlignment(SwingConstants.CENTER);

        f.add(t);
    }

    private void drawFileChooserButton() {
        b = new JButton("Open file");
        b.setBounds(w/3, h/2, w/3, 100);

        b.addActionListener(this);

        f.add(b);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == b) {
            try {
                String result = Utils.handleFileChooser(f);
                if (result != null) text = result;
            } catch (FileNotFoundException ignored) {}
        }
    }
}
