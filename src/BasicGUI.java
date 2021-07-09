import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ali Ibrahim
 * @version 1.0
 * @date  08/07/2021
 */
public class BasicGUI extends JFrame {

    JPanel jp = new JPanel();

    final static Process p = new Process();
    public static JTextArea jtx = new JTextArea(20, 40);
    JButton read_key = new JButton("Read key");
    JButton read_file = new JButton("Read file");
    JButton run = new JButton("Run algorithm");
    static JRadioButton encrypt = new JRadioButton("Encrypt");
    static JRadioButton decrypt = new JRadioButton("Decrypt");
    JButton clear = new JButton("clear");
    JScrollPane sPane = new JScrollPane();

    public BasicGUI() {
        this.setTitle("A5 algorithm");
        this.setVisible(true);
        this.setSize(500, 500);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                String[] ObjButtons = {"Yes", "No"};
                int PromptResult = JOptionPane.showOptionDialog(null, "Are you sure you want to exit?",
                        "A5 algorithm", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null, ObjButtons, ObjButtons[1]);
                if (PromptResult == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        read_key.addActionListener(new on_read_key_action_listener());
        read_file.addActionListener(new on_read_file_action_listener());
        clear.addActionListener(new on_clear_action_listener());
        run.addActionListener(new on_run_action_listener());
        sPane.add(jtx);
        sPane.setViewportView(jtx);
        encrypt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (encrypt.isSelected())
                    decrypt.setSelected(false);
            }
        });

        decrypt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (decrypt.isSelected()) {
                    encrypt.setSelected(false);
                }
            }
        });
        jp.add(sPane);
        jp.add(read_key);
        jp.add(read_file);
        jp.add(encrypt);
        jp.add(decrypt);
        jp.add(run);
        jp.add(clear);
        add(jp);
    }

    public static void main(String[] args) {
        BasicGUI gui = new BasicGUI();
    }

    static class on_read_key_action_listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            final File file;
            jtx.setText("");
            String path = System.getProperty("user.dir");
            JFileChooser jFileChooser1 = new JFileChooser(path);
            if (jFileChooser1.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                file = jFileChooser1.getSelectedFile();
                jtx.append("Start Reading file....\n");
                Thread th1 = new Thread(() -> {
                    try {
                        p.process();
                    } catch (InterruptedException ex) {
                        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());

                    }
                });

                Thread th2 = new Thread(() -> {
                    try {
                        p.read_session_key_from_file(file);
                    } catch (IOException | InterruptedException ex) {
                        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                    }
                });

                th1.start();
                th2.start();
            }

        }
    }

    static class on_read_file_action_listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Thread th_process = new Thread(() -> {
                try {
                    p.process();
                } catch (InterruptedException ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());

                }
            });

            Thread th_read = new Thread(() -> {
                try {
                    p.read_file();
                } catch (InterruptedException | FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());

                }
            });

            th_process.start();
            th_read.start();
        }
    }

    private static class on_clear_action_listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            jtx.setText("");
        }
    }

    private static class on_run_action_listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Thread th_process = new Thread(() -> {
                try {
                    p.process();
                } catch (InterruptedException ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());

                }
            });

            if (encrypt.isSelected()) {
                Thread th_encrypt_thread = new Thread(() -> {
                    try {
                        p.encrypt();
                        encrypt.setSelected(false);
                    } catch (InterruptedException | FileNotFoundException interrupted_exception) {
                        Logger.getLogger(BasicGUI.class.getName()).log(Level.SEVERE, null, interrupted_exception);
                    }
                });
                th_process.start();
                th_encrypt_thread.start();
            } else if (decrypt.isSelected()) {
                Thread th_decrypt_thread = new Thread(() -> {
                    try {
                        p.decrypt();
                        decrypt.setSelected(false);
                    } catch (InterruptedException | FileNotFoundException ex) {
                        Logger.getLogger(BasicGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                th_process.start();
                th_decrypt_thread.start();
            }

        }
    }
}

