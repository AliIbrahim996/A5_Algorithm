package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {
    @FXML
    public Button read_key = new Button();
    @FXML
    public RadioButton enc = new RadioButton();
    @FXML
    public Button run = new Button();
    @FXML
    public Button read_file = new Button();
    @FXML
    public ComboBox cycle = new ComboBox();
    @FXML
    public Button save = new Button();
    @FXML
    public Label cycle_label = new Label();
    Process p = new Process(this);
    boolean save_enc = false;
    @FXML
    public TextArea algo_text = new TextArea();


    public void write_to_text_area(String text) {
        algo_text.appendText(text);
    }

    @FXML
    private void on_enc_selected(ActionEvent event) {
        if (enc.selectedProperty().get()) {
            run.visibleProperty().setValue(true);
        }
    }


    @FXML
    private void on_read_key_action(ActionEvent event) {
        final File file;
        String path = System.getProperty("user.dir");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(path);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text files", "*.txt")
        );
        fileChooser.setTitle("Open key file");
        file = fileChooser.showOpenDialog(null);

        Thread th1 = new Thread(() -> {
            try {
                p.process();
            } catch (InterruptedException ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());

            }
        });

        Thread th2 = new Thread(() -> {
            try {
                write_to_text_area("** Start reading file **\n");
                p.read_session_key_from_file(file);
                read_file.visibleProperty().setValue(true);
                cycle.visibleProperty().setValue(true);
                cycle_label.visibleProperty().setValue(true);
            } catch (IOException | InterruptedException ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        });

        th1.start();
        th2.start();
    }

    @FXML
    private void on_run_action(ActionEvent event) {

        Thread th_process = new Thread(() -> {
            try {
                p.process();
            } catch (InterruptedException ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());

            }
        });

        if (enc.selectedProperty().get()) {
            Thread th_encrypt_thread = new Thread(() -> {
                try {
                    p.encrypt();
                    enc.selectedProperty().set(false);
                    save_enc = true;
                    save.visibleProperty().setValue(true);
                } catch (InterruptedException | FileNotFoundException interrupted_exception) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, interrupted_exception);
                }
            });
            th_process.start();
            th_encrypt_thread.start();
        }
    }

    @FXML
    private void on_save_action(ActionEvent event) {

        Thread th_process = new Thread(() -> {
            try {
                p.process();
            } catch (InterruptedException ex) {
                JOptionPane.showMessageDialog(null, "Error: " + Arrays.toString(ex.getStackTrace()));

            }
        });

        if (enc.selectedProperty().get()) {
            Thread th_encrypt_thread = new Thread(() -> {
                try {
                    p.encrypt();
                    enc.selectedProperty().set(false);
                    save_enc = true;
                    save.visibleProperty().setValue(true);
                } catch (InterruptedException | FileNotFoundException interrupted_exception) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, interrupted_exception);
                }
            });
            th_process.start();
            th_encrypt_thread.start();
        }
    }

    @FXML
    private void on_read_file_action(ActionEvent actionEvent) {
        File file;
        String path = System.getProperty("user.dir");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(path);
        FileChooser.ExtensionFilter text_filter = new FileChooser.ExtensionFilter(
                "Text", "*.txt"
        );

        FileChooser.ExtensionFilter audio_filter = new FileChooser.ExtensionFilter(
                "Audio", "*.mp3"
        );
        FileChooser.ExtensionFilter audio_filter2 = new FileChooser.ExtensionFilter(
                "Audio", "*.m4a"
        );
        fileChooser.getExtensionFilters().addAll(audio_filter, text_filter, audio_filter2);
        fileChooser.setTitle("Open file");
        try {
            file = fileChooser.showOpenDialog(null);
            Thread th_read;
            if (file.getAbsoluteFile().getName().endsWith(".mp3") || file.getAbsoluteFile().getName().endsWith(".m4a")) {
                th_read = new Thread(() -> {
                    try {
                        p.read_audio_file(file);
                        enc.visibleProperty().setValue(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                    }
                });
            } else {
                th_read = new Thread(() -> {
                    try {
                        p.read_file(file);
                        enc.visibleProperty().setValue(true);
                    } catch (InterruptedException | IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());

                    }
                });
            }
            Thread th_process = new Thread(() -> {
                try {
                    p.process();
                } catch (InterruptedException ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());

                }
            });
            th_process.start();
            th_read.start();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }

    }


    class Process {

        boolean flag = false, audio_flag = false;
        public A5Algorithm algorithm;
        String lines;

        public Process(Controller c) {
            algorithm = new A5Algorithm(c);
        }

        public void read_session_key_from_file(File file) throws IOException, InterruptedException {
            Thread.sleep(2000);
            synchronized (this) {
                if (file != null) {
                    Scanner read = new Scanner(file);
                    write_to_text_area("** Reading session key \n");
                    String session_key = read.nextLine();
                    Thread set_key = new Thread(() -> {
                        algorithm.set_session_key(session_key);
                    });
                    Thread set_frame = new Thread(() -> {
                        algorithm.setFrame_counter();
                    });
                    Thread set_majority = new Thread(() -> {
                        algorithm.clocking_lFSRs_with_majority_vote();
                    });
                    Thread set_key_stream = new Thread(() -> {
                        algorithm.production_of_key_stream();
                    });
                    set_key.start();
                    set_frame.start();
                    set_key.join();
                    set_frame.join();
                    set_majority.start();
                    set_majority.join();
                    set_key_stream.start();
                    set_key_stream.join();
                    write_to_text_area("\n** finished! **\n");
                    notify();
                } else {
                    JOptionPane.showMessageDialog(null, "No file selected!");
                }
            }
        }

        public void process() throws InterruptedException {
            synchronized (this) {
                wait();
                write_to_text_area("\nProgram Resumed!..\n");
            }
        }


        public void read_file(File file) throws InterruptedException, IOException {
            Thread.sleep(2000);
            synchronized (this) {
                if (file != null) {
                    write_to_text_area("** Reading file \n");
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    lines = Arrays.toString(bytes);
                    System.out.println(lines);
                    write_to_text_area("finished! **\n");
                    flag = true;
                    audio_flag = false;
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

        private void solver1() throws InterruptedException {
            String cipher_text = "";
            if (audio_flag)
                cipher_text = algorithm.encrypt(lines);
            else
                cipher_text = algorithm.encrypt(algorithm.to_binary(lines));
            write_to_text_area("\n** Cipher text is: **\n" + cipher_text);
        }

        public void read_audio_file(File file) throws InterruptedException, IOException {
            Thread.sleep(2000);
            synchronized (this) {
                if (file != null) {
                    write_to_text_area("** Reading  file \n");
                    System.out.println("Audio file");
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    lines = algorithm.convert_byte_arrays_to_binary(bytes);
                    System.out.println("audio file length : " + lines.length());
                    write_to_text_area("finished! **\n");
                    audio_flag = true;
                    flag = false;
                    notify();
                } else {
                    JOptionPane.showMessageDialog(null, "No file selected!");
                }
            }
        }
    }
}

