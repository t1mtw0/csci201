package crosswords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.io.PrintWriter;

public class PlayerThread extends Thread {
    private CWServer server;
    private Socket socket;
    private ReentrantLock lock;
    private Condition cond;
    private PrintWriter pw;
    private BufferedReader br;
    private int playerNum;
    private Boolean isFirst;
    private Boolean wasFirst;

    public PlayerThread(Socket s, CWServer server, ReentrantLock lock, Condition cond, int pn, Boolean isFirst) {
        this.server = server;
        this.socket = s;
        this.lock = lock;
        this.cond = cond;
        this.playerNum = pn;
        this.isFirst = isFirst;
        this.wasFirst = isFirst;
        try {
            pw = new PrintWriter(this.socket.getOutputStream());
            br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.start();
        } catch (IOException ioe) {
            System.out.println("ioe PlayerThread: " + ioe.getMessage());
        }
    }

    public void run() {
        try {
            String line = null;
            while (true) {
                lock.lock();
                if (!this.server.isGameStart()) {
                    if (isFirst) {
                        sendMessage("How many players will there be? ");
                        while ((line = br.readLine()) == null)
                            ;
                        this.server.setNumPlayers(Integer.parseInt(line));
                        System.out.println("Number of players: " + this.server.getNumPlayers());
                        isFirst = false;
                    } else {
                        if (!wasFirst) {
                            String mes = "Player(s) ";
                            mes += ("" + 1);
                            for (int i = 2; i <= this.server.getPlayerThreads().size() - 1; i++)
                                mes += "," + i;
                            mes += " have already joined.";
                            sendMessage(mes);
                        }
                        cond.await();
                    }
                    if (this.server.getPlayerThreads().size() != this.server.getNumPlayers()) {
                        String mes = "Waiting for player(s): ";
                        for (int i = this.server.getPlayerThreads().size() + 1; i <= this.server.getNumPlayers(); i++)
                            mes += i + " ";
                        sendMessage(mes);
                    }
                } else {
                    if (this.playerNum != this.server.getCurPlayer())
                        cond.await();
                    if (this.server.getAcrossWords().isEmpty() && this.server.getDownWords().isEmpty()) {
                        return;
                    }
                    System.out.println("Sending game board.");
                    this.server.bcBoard();
                    this.server.bcHints();
                    this.server.broadcast("Player " + this.server.getCurPlayer() + "'s turn.", this);
                    String dir = getDir();
                    int num = getNum(dir);
                    String guess = getGuess(dir, num);
                    String d = dir.equals("a") ? "across" : "down";
                    this.server.broadcast(
                            "Player " + this.playerNum + " guessed '" + guess + "' for " + num + " " + d + ".", this);
                    updateGuess(dir, num, guess);
                    lock.unlock();
                    if (this.server.getAcrossWords().isEmpty() && this.server.getDownWords().isEmpty()) {
                        for (int i = 0; i < this.server.getPlayerThreads().size(); i++) {
                            if (this.server.getPlayerThreads().get(i) == this)
                                continue;
                            this.server.getPlayerLocks().get(i).lock();
                            this.server.getPlayerConds().get(i).signal();
                            this.server.getPlayerLocks().get(i).unlock();
                            this.server.getPlayerThreads().get(i).join();
                        }
                        this.server.broadcastScores();
                        this.server.broadcast("TERMINATE", null);
                        return;
                    }
                    this.server.getPlayerLocks().get(playerNum % this.server.getNumPlayers()).lock();
                    this.server.setCurPlayer(playerNum % this.server.getNumPlayers() + 1);
                    this.server.getPlayerConds().get(playerNum % this.server.getNumPlayers()).signal();
                    this.server.getPlayerLocks().get(playerNum % this.server.getNumPlayers()).unlock();
                }
            }
        } catch (IOException ioe) {
            System.out.println("ioe PlayerThread run: " + ioe.getMessage());
        } catch (InterruptedException ie) {
            System.out.println("ie PlayerThread run: " + ie.getMessage());
        }
    }

    private void updateGuess(String dir, int num, String guess) {
        Boolean correct = false;
        if (dir.equals("a")) {
            for (Word w : this.server.getAcrossWords()) {
                int wnum = w.getNum();
                if (wnum == num && w.getWord().equals(guess)) {
                    synchronized (this.server.getAcrossWords()) {
                        this.server.getAcrossWords().remove(w);
                    }
                    synchronized (this.server.getBoard()) {
                        Pos wstart = w.getStart();
                        int y = wstart.getY();
                        for (int x = 0; x < w.getWord().length(); x++) {
                            this.server.getBoard()[y][x + wstart.getX()] = Character
                                    .toString(w.getWord().charAt(x));
                        }
                    }
                    this.server.broadcast("That is correct.", null);
                    int prevScore = this.server.getPlayerScores().get(playerNum - 1);
                    this.server.getPlayerScores().set(playerNum - 1, prevScore + 1);
                    correct = true;
                    break;
                }
            }
        } else {
            for (Word w : this.server.getDownWords()) {
                int wnum = w.getNum();
                if (wnum == num && w.getWord().equals(guess)) {
                    synchronized (this.server.getDownWords()) {
                        this.server.getDownWords().remove(w);
                    }
                    synchronized (this.server.getBoard()) {
                        Pos wstart = w.getStart();
                        int x = wstart.getX();
                        for (int y = 0; y < w.getWord().length(); y++)
                            this.server.getBoard()[y + wstart.getY()][x] = Character
                                    .toString(w.getWord().charAt(y));

                    }
                    this.server.broadcast("That is correct.", null);
                    int prevScore = this.server.getPlayerScores().get(playerNum - 1);
                    this.server.getPlayerScores().set(playerNum - 1, prevScore + 1);
                    correct = true;
                    break;
                }
            }
        }
        if (!correct)
            this.server.broadcast("That is not correct.", null);
    }

    private String getDir() throws IOException {
        String dir = null;
        while (true) {
            sendMessage("Would you like to answer a question across (a) or down (d)? ");
            while ((dir = br.readLine()) == null)
                ;
            if (!dir.equals("a") && !dir.equals("d"))
                sendMessage("That is not a valid response.");
            else if (dir.equals("a") && this.server.getAcrossWords().size() == 0)
                sendMessage("That is not a valid response.");
            else if (dir.equals("d") && this.server.getDownWords().size() == 0)
                sendMessage("That is not a valid response.");
            else
                break;
        }
        return dir;
    }

    private int getNum(String dir) throws IOException {
        int num = -1;
        Boolean found = false;
        while (!found) {
            sendMessage("Which number? ");
            String numS = null;
            while ((numS = br.readLine()) == null)
                ;
            try {
                num = Integer.parseInt(numS);
            } catch (NumberFormatException nfe) {
                sendMessage("That is not a valid response.");
                continue;
            }
            if (dir.equals("a")) {
                for (Word w : this.server.getAcrossWords()) {
                    if (w.getNum() == num)
                        found = true;
                }
            } else {
                for (Word w : this.server.getDownWords()) {
                    if (w.getNum() == num)
                        found = true;
                }
            }
            if (!found)
                sendMessage("That is not a valid response.");
        }
        return num;
    }

    private String getGuess(String dir, int num) throws IOException {
        String d = dir.equals("a") ? "across" : "down";
        sendMessage("What is your guess for " + num + " " + d + "?");
        String guess = null;
        while ((guess = br.readLine()) == null)
            ;
        guess = guess.toLowerCase();
        return guess;
    }

    public void sendMessage(String mes) {
        pw.println(mes);
        pw.flush();
    }
}
