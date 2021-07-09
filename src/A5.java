import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * @author Ali Ibrahim
 * @version 1.0
 * @date  08/07/2021
 */
public class A5 {
    byte[] reg_LFSR1;
    byte[] reg_LFSR2;
    byte[] reg_LFSR3;

    private String session_key;

    public String getSession_key() {
        return session_key;
    }

    public void setSession_key(String session_key) {
        this.session_key = session_key;
    }

    /*!
     * @brief loads registers using a 64-bit key as a parameter
     * @param [in] a 64-bit key
     * */
    private void load_registers(String key) {
        int i = 0;
        byte key_array[] = key.getBytes();
        int LFSR1_ = 19;
        reg_LFSR1 = new byte[LFSR1_];
        int LFSR2_ = 22;
        reg_LFSR2 = new byte[LFSR2_];
        int LFSR3_ = 23;
        reg_LFSR3 = new byte[LFSR3_];
        while (i < LFSR1_) {
            reg_LFSR1[i] = key_array[i];
            i++;
        }
        int j = 0;
        int p = LFSR1_;
        while (j < LFSR2_) {
            reg_LFSR2[j] = key_array[p];
            p++;
            j++;
        }
        int k = LFSR2_ + LFSR1_;
        int r = 0;
        while (r < LFSR3_) {
            reg_LFSR3[r] = key_array[k];
            k++;
            r++;
        }
    }

    /*!
     * @brief sets the key and loads the registers if it contains 0's and 1's and if it's exactly 64 bits
     * @param [in] a 64-bit key
     * @return boolean value to indicate that key has been loaded or not
     * */
    public boolean set_key(String key) {
        String key_reg_expr = "^[01]+$";
        Pattern pat = Pattern.compile(key_reg_expr);
        if (key == null) {
            return false;
        }
        if (key.length() == 64 && pat.matcher(key).matches()) {
            load_registers(key);
            return true;
        }
        return false;
    }

    /*!
     * @brief converts plaintext to binary
     * @param [in] plain text
     * @return binary string
     * */
    private String to_binary(String plain_text) {

        StringBuilder result = new StringBuilder();
        char[] chars = plain_text.toCharArray();
        for (char aChar : chars) {
            result.append(
                    String.format("%8s", Integer.toBinaryString(aChar))   // char -> int, auto-cast
                            .replaceAll(" ", "0")                         // zero pads
            );
        }
        return result.toString();
    }

    /*!
     * @brief get the majority of clocking bit
     * */
    private int get_majority(int lfsr1_bit, int lfsr2_bit, int lfsr3_bit) {
        if (lfsr1_bit + lfsr2_bit + lfsr3_bit > 1)
            return 1;
        else
            return 0;
    }

    /*!
     * @brief method that return a key stream for a specified length
     * @param [i] length of the key stream
     * @return byte array which contains key bytes
     *
     * */
    private byte[] get_key_stream(int length) {
        byte[] reg_LFSR1_temp = reg_LFSR1.clone();
        byte[] reg_LFSR2_temp = reg_LFSR2.clone();
        byte[] reg_LFSR3_temp = reg_LFSR3.clone();
        byte[] key_stream = new byte[length];
        int i = 0;
        while (i < length) {
            int majority = get_majority(reg_LFSR1_temp[8], reg_LFSR2_temp[10], reg_LFSR3_temp[10]);
            if (majority == reg_LFSR1_temp[8]) {
                int new_ = reg_LFSR1_temp[13] ^ reg_LFSR1_temp[16] ^ reg_LFSR1_temp[17] ^ reg_LFSR1_temp[18];
                byte[] reg_x_temp_two = reg_LFSR1_temp.clone();
                int j = 1;
                while (j < reg_LFSR1_temp.length) {
                    reg_LFSR1_temp[j] = reg_x_temp_two[j - 1];
                    j++;
                }
                reg_LFSR1_temp[0] = (byte) new_;
            }

            if (majority == reg_LFSR2_temp[10]) {
                int new_one = reg_LFSR2_temp[20] ^ reg_LFSR2_temp[21];
                byte[] reg_y_temp_two = reg_LFSR2_temp.clone();
                int k = 1;
                while (k < reg_LFSR2_temp.length) {
                    reg_LFSR2_temp[k] = reg_y_temp_two[k - 1];
                    k++;
                }
                reg_LFSR2_temp[0] = (byte) new_one;
            }

            if (majority == reg_LFSR3_temp[10]) {
                int new_two = reg_LFSR3_temp[7] ^ reg_LFSR3_temp[20] ^ reg_LFSR3_temp[21] ^ reg_LFSR3_temp[22];
                byte[] reg_y_temp_two = reg_LFSR2_temp.clone();
                int k = 1;
                while (k < reg_LFSR2_temp.length) {
                    reg_LFSR2_temp[k] = reg_y_temp_two[k - 1];
                    k++;
                }
                reg_LFSR3_temp[0] = (byte) new_two;
            }
            key_stream[i] = (byte) (reg_LFSR1_temp[18] ^ reg_LFSR2_temp[21] ^ reg_LFSR3_temp[22]);
            i++;
        }

        return key_stream;
    }


    /*!
     * @brief method that converts binary String to plain String
     * @param [in] binary_string
     * @return A clear plain String
     * */
    String convert_binary_to_string(String binary_string) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(binary_string.split("(?<=\\G.{8})")).forEach(s -> sb.append((char) Integer.parseInt(s, 2)));
        return sb.toString();
    }

    /*!
     * @brief  method that if cipher text contains only zeros and ones
     * @param [in] cipher text
     * @return true if it is all bits(0 or 1) false otherwise
     *
     * */
    public boolean checkCipherText(String cipher_text) {
        String passRegex = "^[01]+$";
        Pattern pat = Pattern.compile(passRegex);
        if (cipher_text == null) {
            System.out.println("Please input cipher text");
            return false;
        }
        if (pat.matcher(cipher_text).matches()) {
            return true;
        }
        System.out.println("Please input only 0 and 1");
        return false;
    }

    /*!
     * @brief method to encrypt plain text
     * @param [in] plain_text
     * @return a String represents the cipher text
     * */
    String encrypt(String plain_text) {
        set_key(getSession_key());
        String s = "";
        String binary = to_binary(plain_text);
        byte[] key_stream = get_key_stream(binary.length());
        int i = 0;
        byte binary_bytes[] = binary.getBytes();
        while (i < binary.length()) {
            s += binary_bytes[i] ^ key_stream[i];
            i++;
        }
        return s;
    }

    /*!
     * @brief method to decrypt binary text
     * @param [in] cipher_text
     * @return a String represents the plain text
     * */
    String decrypt(String cipher_text) {
        String s = "";
        set_key(getSession_key());
        byte binary[] = cipher_text.getBytes();
        byte[] key_stream = get_key_stream(cipher_text.length());
        int i = 0;
        while (i < cipher_text.length()) {
            s += binary[i] ^ key_stream[i];
            i++;
        }
        return convert_binary_to_string(s);
    }
}