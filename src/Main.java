import java.util.Scanner;
/**
 * @author Ali Ibrahim
 * @version 1.0
 * @date  08/07/2021
 */
public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String key;
        A5 algo = new A5();
        //and example key 0101001000011010110001110001100100101001000000110111111010110111
        do {
            System.out.println("Enter a 64-bit session key:");
            key = input.next();
        } while (!algo.set_key(key));
        System.out.println("Input your choice:\n[0]: Quit\n[1]: Encrypt\n[2]: Decrypt\nPress 0, 1, or 2:");
        String choice = input.next();
        switch (choice) {
            case "1":
                String plain_text = input.next();
                System.out.println("Plain text: " + plain_text);
                System.out.println("**************************************************");
                System.out.println("Cipher text: " + algo.encrypt(plain_text));
                break;
            case "2":
                String cipher_text;
                do {
                    cipher_text = input.next();
                } while (!algo.checkCipherText(cipher_text));

                System.out.println("Cipher text: " + cipher_text);
                System.out.println("**************************************************");
                System.out.println("Plain text: " + algo.decrypt(cipher_text));
                break;
            case "0":
                System.out.println("Have a nice day!");
                break;
        }
    }
}


