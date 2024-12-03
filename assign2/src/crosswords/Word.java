package crosswords;

enum CWType {
    ACROSS,
    DOWN,
}

public class Word {
    private CWType type;
    private String word;
    private String hint;
    private int num;
    private Pos start;

    public Word(CWType type, String word, String hint, int num) {
        this.type = type;
        this.word = word;
        this.hint = hint;
        this.num = num;
        this.start = new Pos(0, 0);
    }

    public CWType getType() {
        return type;
    }

    public String getWord() {
        return word;
    }

    public String getHint() {
        return hint;
    }

    public int getNum() {
        return num;
    }

    public void setStart(int x, int y) {
        start.setX(x);
        start.setY(y);
    }

    public void setStart(Pos pos) {
        start = pos;
    }

    public Pos getStart() {
        return start;
    }
}
