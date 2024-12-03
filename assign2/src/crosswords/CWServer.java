package crosswords;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.net.Socket;

public class CWServer extends Thread {
    private Vector<PlayerThread> playerThreads;
    private Vector<ReentrantLock> playerLocks;
    private Vector<Condition> playerConds;
    private Vector<Integer> playerScores;
    private int numPlayers = -1;
    private String[][] board;
    private ArrayList<Word> acrossWords;
    private ArrayList<Word> downWords;
    private ArrayList<Word> placedWords;
    private ArrayList<Word> unplacedWords;
    private int curPlayer;
    private Boolean isGameOver = false;
    private Boolean isGameStart = false;

    public CWServer(int port) {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
            playerThreads = new Vector<PlayerThread>();
            playerLocks = new Vector<ReentrantLock>();
            playerConds = new Vector<Condition>();
            playerScores = new Vector<Integer>();
            int pi = 0;

            System.out.println("Listening on port " + port);
            System.out.println("Waiting for players...");

            this.start();

            while (true) {
                if (playerThreads.size() == 0) {
                    Socket s = ss.accept();
                    System.out.println("Connection from " + s.getInetAddress());
                    ReentrantLock lock = new ReentrantLock();
                    playerLocks.add(lock);
                    Condition cond = lock.newCondition();
                    playerConds.add(cond);
                    PlayerThread pt = new PlayerThread(s, this, lock, cond, ++pi, true);
                    playerThreads.add(pt);
                    playerScores.add(0);
                } else if (playerThreads.size() < numPlayers) {
                    Socket s = ss.accept();
                    System.out.println("Connection from " + s.getInetAddress());
                    ReentrantLock lock = new ReentrantLock();
                    playerLocks.add(lock);
                    Condition cond = lock.newCondition();
                    playerConds.add(cond);
                    PlayerThread pt = new PlayerThread(s, this, lock, cond, ++pi, false);
                    playerThreads.add(pt);
                    playerScores.add(0);
                } else {
                    if (playerThreads.size() != numPlayers || numPlayers == -1)
                        continue;
                    isGameStart = true;
                    this.join();
                    curPlayer = 1;
                    System.out.println("Game can now begin.");
                    broadcast("The game is now beginning.", null);
                    if (!isGameOver) {
                        playerLocks.get(curPlayer - 1).lock();
                        playerConds.get(curPlayer - 1).signal();
                        playerLocks.get(curPlayer - 1).unlock();
                    }
                    for (PlayerThread pt : playerThreads)
                        pt.join();
                    while (!isGameOver)
                        if (acrossWords.isEmpty() && downWords.isEmpty())
                            isGameOver = true;
                    System.out.println("The game has concluded.");
                    System.out.println("Sending scores.");
                    pi = 0;
                    isGameStart = false;
                    isGameOver = false;
                    numPlayers = -1;
                    playerThreads.clear();
                    playerLocks.clear();
                    playerConds.clear();
                    playerScores.clear();
                    placedWords.clear();
                    unplacedWords.clear();
                    this.run();
                }
            }
        } catch (IOException ioe) {
            System.out.println("ioe in Server: " + ioe.getMessage());
        } catch (InterruptedException ie) {
            System.out.println("ie in Server: " + ie.getMessage());
        } finally {
            try {
                if (ss != null)
                    ss.close();
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }
    }

    public String[][] getBoard() {
        return board;
    }

    public ArrayList<Word> getAcrossWords() {
        return acrossWords;
    }

    public ArrayList<Word> getDownWords() {
        return downWords;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(int np) {
        this.numPlayers = np;
    }

    public Boolean isGameStart() {
        return isGameStart;
    }

    public Boolean isGameOver() {
        return isGameOver;
    }

    public void setIsGameOver(Boolean isGameOver) {
        this.isGameOver = isGameOver;
    }

    public Vector<PlayerThread> getPlayerThreads() {
        return playerThreads;
    }

    public Vector<ReentrantLock> getPlayerLocks() {
        return playerLocks;
    }

    public Vector<Condition> getPlayerConds() {
        return playerConds;
    }

    public Vector<Integer> getPlayerScores() {
        return playerScores;
    }

    public int getCurPlayer() {
        return curPlayer;
    }

    public void setCurPlayer(int curPlayer) {
        this.curPlayer = curPlayer;
    }

    public void broadcast(String message, PlayerThread exclude) {
        for (PlayerThread pt : playerThreads) {
            if (pt == exclude)
                continue;
            pt.sendMessage(message);
        }
    }

    public void broadcastScores() {
        broadcast("Final Score", null);
        int i = 1;
        Boolean tie = true;
        int winner = -1;
        int maxScore = -1;
        for (int s : playerScores) {
            String mes = "Player " + i + " - " + s + " correct answers.";
            broadcast(mes, null);
            if (s != maxScore && winner != -1)
                tie = false;
            if (s > maxScore) {
                winner = i;
                maxScore = s;
            }
            i++;
        }
        if (!tie)
            broadcast("Player " + winner + " is the winner.", null);
        else
            broadcast("There has been a tie with an equal score of " + maxScore, null);
    }

    public void bcBoard() {
        for (String[] cl : board) {
            String mes = "";
            for (String c : cl) {
                String spaces = "";
                for (int i = 0; i < 6 - c.length(); i++)
                    spaces += " ";
                mes += c + spaces;
            }
            broadcast(mes, null);
            broadcast("\n", null);
        }
    }

    public void bcHints() {
        broadcast("Across", null);
        for (Word w : acrossWords) {
            String mes = "";
            mes += w.getNum() + " " + w.getHint();
            broadcast(mes, null);
        }
        broadcast("Down", null);
        for (Word w : downWords) {
            String mes = "";
            mes += w.getNum() + " " + w.getHint();
            broadcast(mes, null);
        }
    }

    public void run() {
        System.out.println("Reading random game file.");
        readRandGameF();
        System.out.println("File read successfully.");
        Boolean found = findSpots();
        if (!found)
            System.exit(1);
        createBoard();
    }

    private void readRandGameF() {
        File fol = new File("./gamedata");
        String[] csvFiles = fol.list(new FilenameFilter() {
            public boolean accept(File dir, String fn) {
                return fn.endsWith(".csv");
            }
        });
        if (csvFiles.length == 0)
            System.exit(0);
        Random rg = new Random();
        acrossWords = new ArrayList<>();
        downWords = new ArrayList<>();
        while (true) {
            String file = csvFiles[rg.nextInt(csvFiles.length)];
            String fn = "gamedata/" + file;
            acrossWords.clear();
            downWords.clear();
            CWType curType = null;
            Boolean next = false;
            try {
                BufferedReader br = new BufferedReader(new FileReader(fn));
                String line;
                while (br.ready()) {
                    line = br.readLine();
                    String[] v = line.split(",");
                    if (v[0].equals("ACROSS")) {
                        curType = CWType.ACROSS;
                        continue;
                    } else if (v[0].equals("DOWN")) {
                        curType = CWType.DOWN;
                        continue;
                    }
                    if (v.length != 3) {
                        next = true;
                        continue;
                    }
                    int num;
                    String wordStr;
                    String hint;
                    try {
                        num = Integer.parseInt(v[0]);
                    } catch (NumberFormatException nfe) {
                        next = true;
                        continue;
                    }
                    wordStr = v[1];
                    hint = v[2];
                    Word word = new Word(curType, wordStr, hint, num);
                    if (curType == CWType.ACROSS)
                        acrossWords.add(word);
                    else if (curType == CWType.DOWN)
                        downWords.add(word);
                    else {
                        next = true;
                    }
                }
                br.close();
            } catch (IOException ioe) {
                System.out.println("ioe in Server run: " + ioe.getMessage());
            }
            if (!next)
                break;
        }
    }

    private Boolean findSpots() {
        placedWords = new ArrayList<>();
        unplacedWords = new ArrayList<>();
        for (Word w : downWords)
            unplacedWords.add(w);
        for (Word w : acrossWords)
            unplacedWords.add(w);
        unplacedWords.sort((a, b) -> Integer.compare(a.getWord().length(), b.getWord().length()));
        findSpotOne();
        return findSpotsAll();
    }

    private void findSpotOne() {
        Word word = unplacedWords.get(unplacedWords.size() - 1);
        word.setStart(0, 0);
        unplacedWords.remove(unplacedWords.size() - 1);
        placedWords.add(word);
    }

    private Boolean findSpotsAll() {
        if (unplacedWords.size() == 0)
            return true;

        for (int wi = unplacedWords.size() - 1; wi >= 0; wi--) {
            Word w = unplacedWords.get(wi);
            for (int pwi = placedWords.size() - 1; pwi >= 0; pwi--) {
                Word pw = placedWords.get(pwi);
                if (pw.getNum() == w.getNum()) {
                    Pos wstart = new Pos(pw.getStart().getX(), pw.getStart().getY());
                    w.setStart(wstart);
                    if (checkInterference(w))
                        return false;
                    else {
                        placedWords.add(w);
                        unplacedWords.remove(w);
                        if (findSpotsAll())
                            return true;
                        else {
                            placedWords.remove(w);
                            unplacedWords.add(w);
                        }
                    }
                    return false;
                }
            }
            for (int pwi = placedWords.size() - 1; pwi >= 0; pwi--) {
                Word pw = placedWords.get(pwi);
                if (pw.getType() == w.getType())
                    continue;
                ArrayList<Pos> intersects = findIntersect(w, pw);
                for (Pos is : intersects) {
                    placeWord(w, pw, is);
                    if (!checkInterference(w)) {
                        placedWords.add(w);
                        unplacedWords.remove(w);
                        if (findSpotsAll())
                            return true;
                        else {
                            placedWords.remove(w);
                            unplacedWords.add(w);
                        }
                    }
                }
            }
        }

        return false;
    }

    private ArrayList<Pos> findIntersect(Word w, Word pw) {
        ArrayList<Pos> intersects = new ArrayList<>();
        String ww = w.getWord();
        String pww = pw.getWord();
        for (int i = 0; i < ww.length(); ++i) {
            char wc = ww.charAt(i);
            for (int j = 0; j < pww.length(); ++j) {
                char pwc = pww.charAt(j);
                if (wc == pwc)
                    intersects.add(new Pos(i, j));
            }
        }
        return intersects;
    }

    private void placeWord(Word w, Word pw, Pos is) {
        Pos intSpot = new Pos(0, 0);
        Pos startSpot = new Pos(0, 0);
        if (pw.getType() == CWType.ACROSS) {
            intSpot.setX(pw.getStart().getX() + is.getY());
            intSpot.setY(pw.getStart().getY());
            startSpot.setX(intSpot.getX());
            startSpot.setY(intSpot.getY() - is.getX());
        } else {
            intSpot.setX(pw.getStart().getX());
            intSpot.setY(pw.getStart().getY() + is.getY());
            startSpot.setX(intSpot.getX() - is.getX());
            startSpot.setY(intSpot.getY());
        }
        w.setStart(startSpot);
    }

    private Boolean checkInterference(Word w) {
        Pos wstart = w.getStart();
        String ww = w.getWord();
        int wl = ww.length();
        if (w.getType() == CWType.ACROSS) {
            for (int i = 0; i < wl; i++) {
                Pos spot = new Pos(wstart.getX() + i, wstart.getY());
                for (Word pw : placedWords) {
                    char cAtSpot = ww.charAt(i);
                    if (checkInterfOne(cAtSpot, pw, spot))
                        return true;
                }
            }
        } else {
            for (int i = 0; i < wl; i++) {
                Pos spot = new Pos(wstart.getX(), wstart.getY() + i);
                for (Word pw : placedWords) {
                    char cAtSpot = ww.charAt(i);
                    if (checkInterfOne(cAtSpot, pw, spot))
                        return true;
                }
            }
        }
        return false;
    }

    private Boolean checkInterfOne(char charAtSpot, Word pw, Pos spot) {
        Pos pwstart = pw.getStart();
        String pww = pw.getWord();
        int pwl = pww.length();
        if (pw.getType() == CWType.ACROSS) {
            for (int i = 0; i < pwl; i++) {
                Pos pspot = new Pos(pwstart.getX() + i, pwstart.getY());
                if (pspot.getX() == spot.getX() && pspot.getY() == spot.getY() && charAtSpot != pww.charAt(i))
                    return true;
            }
        } else {
            for (int i = 0; i < pwl; i++) {
                Pos pspot = new Pos(pwstart.getX(), pwstart.getY() + i);
                if (pspot.getX() == spot.getX() && pspot.getY() == spot.getY() && charAtSpot != pww.charAt(i))
                    return true;
            }
        }
        return false;
    }

    private void createBoard() {
        Pos min = new Pos(Integer.MAX_VALUE, Integer.MAX_VALUE);
        Pos max = new Pos(Integer.MIN_VALUE, Integer.MIN_VALUE);
        for (Word pw : placedWords) {
            Pos pwstart = pw.getStart();
            Pos pwend;
            if (pw.getType() == CWType.ACROSS)
                pwend = new Pos(pwstart.getX() + pw.getWord().length() - 1, pwstart.getY());
            else
                pwend = new Pos(pwstart.getX(), pwstart.getY() + pw.getWord().length() - 1);
            if (pwstart.getX() < min.getX())
                min.setX(pwstart.getX());
            if (pwstart.getY() < min.getY())
                min.setY(pwstart.getY());
            if (pwend.getX() > max.getX())
                max.setX(pwend.getX());
            if (pwend.getY() > max.getY())
                max.setY(pwend.getY());
        }
        int boardH = max.getY() - min.getY() + 1;
        int boardW = max.getX() - min.getX() + 1;
        board = new String[boardH][boardW];
        for (int i = 0; i < boardH; i++)
            for (int j = 0; j < boardW; j++)
                board[i][j] = "_";
        for (Word pw : placedWords) {
            Pos wp = pw.getStart();
            if (pw.getType() == CWType.ACROSS)
                board[wp.getY() - min.getY()][wp.getX() - min.getX()] = ("" + pw.getNum());
            else if (pw.getType() == CWType.DOWN)
                board[wp.getY() - min.getY()][wp.getX() - min.getX()] = ("" + pw.getNum());
            pw.setStart(wp.getX() - min.getX(), wp.getY() - min.getY());
        }
    }

    public static void main(String[] args) {
        new CWServer(3456);
    }
}
