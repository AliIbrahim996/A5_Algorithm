package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

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
    @FXML
    public RadioButton dec = new RadioButton();

    Process p = new Process(this);
    boolean save_enc = false, save_dec = false;
    @FXML
    public TextArea algo_text = new TextArea();


    public void write_to_text_area(String text) {
        if (text.length() <= 1000000)
            Platform.runLater(() -> algo_text.appendText(text));
        else
            System.out.println(text);
    }

    @FXML
    private void on_enc_selected(ActionEvent event) {
        if (enc.selectedProperty().get()) {
            dec.selectedProperty().setValue(false);
            run.visibleProperty().setValue(true);
        }
    }

    @FXML
    private void on_de_selected(ActionEvent event) {
        if (dec.selectedProperty().get()) {
            enc.selectedProperty().setValue(false);
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
                algo_text.clear();
                Platform.runLater(() -> algo_text.setText("** Start reading file **\n"));
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
        Thread th_encrypt_thread;
        if (enc.selectedProperty().get()) {
            th_encrypt_thread = new Thread(() -> {
                try {
                    p.encrypt();
                    enc.selectedProperty().set(false);
                    save_enc = true;
                    save_dec = false;
                    save.visibleProperty().setValue(true);
                } catch (InterruptedException | FileNotFoundException interrupted_exception) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, interrupted_exception);
                }
            });

            th_process.start();
            th_encrypt_thread.start();
        } else if (dec.selectedProperty().get()) {
            th_encrypt_thread = new Thread(() -> {
                try {
                    p.decrypt();
                    dec.selectedProperty().set(false);
                    save_dec = true;
                    save_enc = false;
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

        FileChooser.ExtensionFilter txt_files =
                new FileChooser.ExtensionFilter("TXT files", "*.txt");
        FileChooser.ExtensionFilter audio_files =
                new FileChooser.ExtensionFilter("Audio files", "*.m4a");
        FileChooser save_as = new FileChooser();
        save_as.getExtensionFilters().addAll(txt_files, audio_files);
        File file = save_as.showSaveDialog(null);
        Thread th_process = new Thread(() -> {
            try {
                p.process();
            } catch (InterruptedException ex) {
                JOptionPane.showMessageDialog(null, "Error: " + Arrays.toString(ex.getStackTrace()));

            }
        });

        Thread th_save_thread = new Thread(() -> {
            try {
                int type;
                if (file.getAbsoluteFile().getName().endsWith(".m4a"))
                    type = 1;
                else
                    type = 2;
                p.save_file(file, type);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        th_process.start();
        th_save_thread.start();

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
                        dec.visibleProperty().setValue(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                    }
                });
            } else {
                th_read = new Thread(() -> {
                    try {
                        p.read_file(file);
                        enc.visibleProperty().setValue(true);
                        dec.visibleProperty().setValue(true);
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
        String lines = "";
        String text = "";

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
                    algorithm.set_session_key(session_key);
                    algorithm.setFrame_counter();
                    for (int i = 0; i < 100; i++)
                        algorithm.clocking_lFSRs_with_majority_vote();
                    algorithm.production_of_key_stream();
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
                    lines = "";
                    Scanner reader = new Scanner(file, String.valueOf(StandardCharsets.UTF_8));
                    while (reader.hasNextLine())
                        lines += reader.nextLine() + "\n";
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

        public void save_file(File file, int type) throws InterruptedException {
            Thread.sleep(2000);
            synchronized (this) {
                try {
                    if (type == 1) {
                        byte[] decoded_array = DatatypeConverter.parseBase64Binary(text);
                        FileOutputStream output_stream = new FileOutputStream(file);
                        output_stream.write(decoded_array);
                        output_stream.close();
                    } else {
                        PrintWriter writer;
                        writer = new PrintWriter(file);
                        writer.println(text);
                        writer.close();
                    }
                    write_to_text_area("\n** File " + file.getName() + " saved successfully!**\n");
                    notify();
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
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

        public void decrypt() throws InterruptedException, FileNotFoundException {
            Thread.sleep(2000);
            synchronized (this) {
                solver2();
                notify();

            }
        }

        private void solver2(){
            text = algorithm.decrypt(lines);
            write_to_text_area("\n** Plain text is: **\n" + text);
        }

        private void solver1() throws InterruptedException {
            if (audio_flag)
                text = algorithm.encrypt(lines);
            else
                text = algorithm.encrypt(algorithm.to_binary(lines));
            write_to_text_area("\n** Cipher text is: **\n" + text);
        }

        public void read_audio_file(File file) throws InterruptedException, IOException {
            Thread.sleep(2000);
            synchronized (this) {
                if (file != null) {
                    write_to_text_area("** Reading  file \n");
                    System.out.println("Audio file");
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    String encoded = DatatypeConverter.printBase64Binary(bytes);
                    lines = algorithm.to_binary(encoded);
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

