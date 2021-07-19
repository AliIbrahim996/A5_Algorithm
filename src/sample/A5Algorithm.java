package sample;

import java.util.Arrays;

/**
 * @author Ali Ibrahim
 * @version 1.1
 * @date 14/07/2021
 */
public class A5Algorithm {


    private int[] reg_LFSR1;
    private int[] reg_LFSR2;
    private int[] reg_LFSR3;
    private int[] session_key;
    private final int[] frame_counter = new int[]{1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1,};
    private int[] key_stream;

    private final int lfsr1_clock_bit = 8;
    private final int lfsr2_clock_bit = 10;
    private final int lfsr3_clock_bit = 10;


    Controller c;


    public String get_registers_value() {
        StringBuilder result = new StringBuilder();
        result.append("Register LFSR1 bits are :\n");
        for (int j : reg_LFSR1) result.append(j).append(" ");
        result.append("\n");
        result.append("Register LFSR2 bits are :\n");
        for (int j : reg_LFSR2) result.append(j).append(" ");
        result.append("\n");
        result.append("Register LFSR3 bits are :\n");
        for (int j : reg_LFSR3) result.append(j).append(" ");
        return result.toString();
    }

    public int[] getReg_LFSR1() {
        return reg_LFSR1;
    }

    public int[] getReg_LFSR2() {
        return reg_LFSR2;
    }

    public int[] getReg_LFSR3() {
        return reg_LFSR3;
    }

    /**
     * @param c an Instance of Controller Class
     * @breif constructor
     */
    public A5Algorithm(Controller c) {
        this.c = c;
    }


    public void set_session_key(String session_key) {
        this.session_key = new int[64];
        for (int i = 0; i < 64; i++)
            this.session_key[i] = Integer.parseInt(String.valueOf(session_key.charAt(i)));
        clocking_lfsrs_with_session_key();
    }

    public void setFrame_counter() {
        StringBuilder frame_string_text = new StringBuilder("\n** frame counter after 22 cycle  **\n ");
        for (int j : frame_counter) {
            frame_string_text.append(j);
        }
        c.write_to_text_area(frame_string_text.toString());
        clocking_lFSRs_with_frame_counter();
    }

    /**
     * The registers are initialized with zero-values, and are in
     * this step clocked 64 times with session key
     */
    public void clocking_lfsrs_with_session_key() {
        reg_LFSR1 = new int[19];
        reg_LFSR2 = new int[22];
        reg_LFSR3 = new int[23];
        for (int keyIdx = 0; keyIdx < 64; keyIdx++) {
            int temp_1 = this.session_key[keyIdx] ^ feed_back(1);
            int temp_2 = this.session_key[keyIdx] ^ feed_back(2);
            int temp_3 = this.session_key[keyIdx] ^ feed_back(3);
            shift_left();
            this.reg_LFSR1[0] = temp_1;
            this.reg_LFSR2[0] = temp_2;
            this.reg_LFSR3[0] = temp_3;
        }
    }


    public void step1() {
        clocking_lfsrs_with_session_key();
    }

    public void step2() {
        clocking_lfsrs_with_session_key();
        clocking_lFSRs_with_frame_counter();
    }

    void step3() {
        clocking_lfsrs_with_session_key();
        clocking_lFSRs_with_frame_counter();
        for (int i = 0; i < 100; i++)
            clocking_lFSRs_with_majority_vote();
    }

    void step4() {
        clocking_lfsrs_with_session_key();
        clocking_lFSRs_with_frame_counter();
        for (int i = 0; i < 100; i++)
            clocking_lFSRs_with_majority_vote();
        production_of_key_stream();
    }

    /**
     * a method to shift registers one-bit to left
     */
    private void shift_left() {
        int[] temp_register = reg_LFSR1.clone();
        if (this.reg_LFSR1.length - 1 >= 0)
            System.arraycopy(temp_register, 0, this.reg_LFSR1, 1, this.reg_LFSR1.length - 1);
        temp_register = reg_LFSR2.clone();
        if (this.reg_LFSR2.length - 1 >= 0)
            System.arraycopy(temp_register, 0, this.reg_LFSR2, 1, this.reg_LFSR2.length - 1);
        temp_register = reg_LFSR3.clone();
        if (this.reg_LFSR3.length - 1 >= 0)
            System.arraycopy(temp_register, 0, this.reg_LFSR3, 1, this.reg_LFSR3.length - 1);
    }

    /**
     * a method to get feedback value after applying bitwise on each register
     *
     * @param reg_index represents the index of the register
     */
    public int feed_back(int reg_index) {
        int val;
        switch (reg_index) {
            case 1:
                val = (this.reg_LFSR1[13] ^ this.reg_LFSR1[16] ^ this.reg_LFSR1[17]
                        ^ this.reg_LFSR1[18]);
                break;
            case 2:
                val = (this.reg_LFSR2[20] ^ this.reg_LFSR3[21]);
                break;
            case 3:
                val = (this.reg_LFSR3[7] ^ this.reg_LFSR3[20] ^ this.reg_LFSR3[21]
                        ^ this.reg_LFSR3[22]);
                break;
            default:
                return 0;
        }
        return val;
    }


    /**
     * After the registers have been clocked with the session
     * key, they no longer hold only zero-values.the registers are clocked against a 22 bit
     * frame counter, where the bits of the frame counter are
     * XORed with the feedback of each register and fed into
     * the LSB of its respective register
     */
    public void clocking_lFSRs_with_frame_counter() {
        for (int counterIdx = 0; counterIdx < 22; counterIdx++) {
            int temp_1 = this.frame_counter[counterIdx] ^ feed_back(1);
            int temp_2 = this.frame_counter[counterIdx] ^ feed_back(2);
            int temp_3 = this.frame_counter[counterIdx] ^ feed_back(3);
            shift_left();
            this.reg_LFSR1[0] = temp_1;
            this.reg_LFSR2[0] = temp_2;
            this.reg_LFSR3[0] = temp_3;
        }
    }

    /**
     * the registers are
     * clocked 100 times with irregular clocking. This is where
     * the clock bits come into play. Irregular clocking follows
     * the majority rule, a decision rule that selects alternatives
     * that have a majority. The majority bit is determined by
     * the clocking bits of the registers (LFSR 1 clocking bit:
     * 8, LFSR 2 clocking bit: 10, LFSR 3 clocking bit: 10).
     * If a clocking bit of a register is equal to the majority
     * bit, the register is clocked. Otherwise, the register is
     * left unchanged
     */
    public void clocking_lFSRs_with_majority_vote() {
        int majorityBit = get_majority();
        if (this.reg_LFSR1[lfsr1_clock_bit] == majorityBit) {
            int temp_1 = feed_back(1);
            shift_reg_clock(1);
            this.reg_LFSR1[0] = temp_1;
        }
        if (this.reg_LFSR2[lfsr2_clock_bit] == majorityBit) {
            int temp_2 = feed_back(2);
            shift_reg_clock(2);
            this.reg_LFSR2[0] = temp_2;
        }
        if (this.reg_LFSR3[lfsr3_clock_bit] == majorityBit) {
            int temp_3 = feed_back(3);
            shift_reg_clock(3);
            this.reg_LFSR3[0] = temp_3;
        }
    }

    /**
     * @breif a method to shift registers to left "irregular clocking"
     */
    private void shift_reg_clock(int registerIdx) {
        int[] temp_register;
        switch (registerIdx) {
            case 1: {
                temp_register = this.reg_LFSR1.clone();
                if (this.reg_LFSR1.length - 1 >= 0)
                    System.arraycopy(temp_register, 0, this.reg_LFSR1, 1, this.reg_LFSR1.length - 1);
            }
            break;
            case 2: {
                temp_register = this.reg_LFSR2.clone();
                if (this.reg_LFSR2.length - 1 >= 0)
                    System.arraycopy(temp_register, 0, this.reg_LFSR2, 1, this.reg_LFSR2.length - 1);
            }
            break;
            case 3: {
                temp_register = this.reg_LFSR3.clone();
                if (this.reg_LFSR3.length - 1 >= 0)
                    System.arraycopy(temp_register, 0, this.reg_LFSR3, 1, this.reg_LFSR3.length - 1);
            }
            break;
        }
    }

    public String to_binary(String plain_text) {
        System.out.println("Start converting to binary");
        System.out.println("plain_text length " + plain_text.length());
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < plain_text.length(); i++) {
            result.append(String.format("%8s", Integer.toBinaryString(plain_text.charAt(i)))   // char -> int, auto-cast
                    .replaceAll(" ", "0"));                      // zero pads;
        }
        return result.toString();
    }

    public String convert_binary_to_string(String binary_string) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(binary_string.split("(?<=\\G.{8})")).forEach(s -> sb.append((char) Integer.parseInt(s, 2)));
        return sb.toString();
    }


    private byte get_majority() {
        if ((this.reg_LFSR1[lfsr1_clock_bit] + this.reg_LFSR2[lfsr2_clock_bit] + this.reg_LFSR3[lfsr3_clock_bit]) > 1)
            return 1;
        else
            return 0;
    }

    /**
     * Pre-processing of the registers is now complete, and they
     * are ready to produce the pseudo random bit stream that
     * will encrypt the plain text. To produce the PRAND bit
     * stream, called ”key stream”, the registers are clocked
     * 228 times with irregular clocking. For each cycle, the
     * most significant bit (MSB) of each register is used for a
     * final computation. Each register’s MSB is XORed with
     * one-another, and the result is added to the key stream.
     * Since each cycle generates a key stream bit, and we cycle our circuit 228 times,
     * the output key stream will consist of 228 bit
     */
    public void production_of_key_stream() {

        key_stream = new int[228];
        for (int i = 0; i < 228; i++) {
            clocking_lFSRs_with_majority_vote();
            this.key_stream[i] = this.reg_LFSR1[18] ^ this.reg_LFSR2[21] ^ this.reg_LFSR3[22];
        }

        StringBuilder key_string_text = new StringBuilder("\n** Key Stream after  228 cycle  **\n ");
        for (int j : key_stream) {
            key_string_text.append(j);
        }
        c.write_to_text_area(key_string_text.toString());
    }

    /**
     * @param plain_text refers to text that we want to encrypt
     * @return a String represents the cipher text
     * @breif method to encrypt plain text
     */
    public String encrypt(String plain_text) {
        System.out.println("Running..");
        StringBuilder encrypted_text = new StringBuilder();
        for (int i = 0; i < plain_text.length(); i++) {
            encrypted_text.append((this.key_stream[i % 228] ^ plain_text.charAt(i)) == 48 ? "0" : "1");
            System.out.println("Iteration " + i);
        }
        return encrypted_text.toString();
    }

    public String decrypt(String cipher_text) {
        System.out.println("Running..");
        StringBuilder plain_text = new StringBuilder();
        for (int i = 0; i < cipher_text.length(); i++) {
            plain_text.append((this.key_stream[i % 228] ^ cipher_text.charAt(i)) == 48 ? "0" : "1");
        }
        return convert_binary_to_string(plain_text.toString());
    }

}
