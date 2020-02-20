package lft5_1;

public class NumberTok extends Token {
    public int lexeme;
    public NumberTok(String s) { super(Tag.NUM); lexeme= Integer.parseInt(s); }
    public String toString() { return "<" + tag + ", " + lexeme + ">"; }
}