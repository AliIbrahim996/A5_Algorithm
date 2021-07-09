import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * @author Ali Ibrahim
 */
public class Process {

    boolean flag = false;
    A5 algo;
    List<String> text;

    public void read_session_key_from_file(File file) throws IOException, InterruptedException {
        Thread.sleep(2000);
        synchronized (this) {
            if (file != null) {
                Scanner read = new Scanner(file, StandardCharsets.UTF_8.name());
                BasicGUI.jtx.append("Reading session key...\n");
                String session_key = read.nextLine();
                algo = new A5();
                algo.setSession_key(session_key);
                BasicGUI.jtx.append("finished!..\n");
                notify();
            } else {
                JOptionPane.showMessageDialog(null, "No file selected!");
            }
        }
    }

    private File fileChooser() {
        File file;
        String path = System.getProperty("user.dir");
        JFileChooser jFileChooser1 = new JFileChooser(path);
        if (jFileChooser1.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = jFileChooser1.getSelectedFile();
            BasicGUI.jtx.append("Start Reading file....\n");
            flag = true;
            return file;
        }
        return null;
    }

    public void process() throws InterruptedException {
        synchronized (this) {
            wait();
            BasicGUI.jtx.append("Program Resumed!..\n");
        }
    }

    public void decrypt() throws InterruptedException, FileNotFoundException {
        Thread.sleep(2000);
        synchronized (this) {
            solver2();
            notify();

        }
    }

    public void read_file() throws InterruptedException, FileNotFoundException {
        Thread.sleep(2000);
        synchronized (this) {
            File file = fileChooser();
            text = new ArrayList();
            if (file != null) {
                Scanner read = new Scanner(file, StandardCharsets.UTF_8.name());
                BasicGUI.jtx.append("Reading file...\n");
                while (read.hasNextLine()) {
                    text.add(read.nextLine());
                }
                BasicGUI.jtx.append("finished!..\n");
                notify();
            } else {
                JOptionPane.showMessageDialog(null, "No file selected!");
            }
        }
    }

    public void encrypt() throws InterruptedException, FileNotFoundException {
        Thread.sleep(2000);
        synchronized (this) {
            solver1();
            notify();

        }
    }

    private void solver1() {
        String encrypted_lines = "\n** Cipher text is: **\n";
        for (String text_line : text) {
            encrypted_lines += algo.encrypt(text_line) + "\n";
        }
        BasicGUI.jtx.append(encrypted_lines);
    }

    private void solver2() {
        String decrypted_lines = "\n** plain text is: **\n";
        for (String text_line : text) {
            decrypted_lines += algo.decrypt(text_line) + "\n";
        }
        BasicGUI.jtx.append(decrypted_lines);
    }
}
