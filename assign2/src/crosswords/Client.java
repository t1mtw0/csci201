package crosswords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread {
    BufferedReader br;

    public Client() {
        Socket s = null;
        PrintWriter pw = null;
        Scanner in = null;

        try {
            in = new Scanner(System.in);
            System.out.println("Welcome to 201 Crossword!");
            System.out.println("Enter the server hostname: ");
            String hostname = in.nextLine();
            System.out.println("Enter the server port: ");
            int port = Integer.parseInt(in.nextLine());

            s = new Socket(hostname, port);
            this.br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            pw = new PrintWriter(s.getOutputStream());

            this.start();
            while (true) {
                String line = in.nextLine();
                pw.println(line);
                pw.flush();
            }
        } catch (IOException ioe) {
            System.out.println("ioe: " + ioe.getMessage());
        } finally {
            try {
                if (pw != null)
                    pw.close();
                if (br != null)
                    br.close();
                if (s != null)
                    s.close();
                if (in != null)
                    in.close();
            } catch (IOException ioe) {
                System.out.println("ioe: " + ioe.getMessage());
            }
        }
    }

    public void run() {
        try {
            String line = null;
            while (true) {
                line = br.readLine();
                if (line.equals("TERMINATE"))
                    System.exit(0);
                System.out.println(line);
            }
        } catch (IOException ioe) {
            System.out.println("ioe in Client run: " + ioe.getMessage());
        }
    }

    public static void main(String[] args) {
        new Client();
    }
}
