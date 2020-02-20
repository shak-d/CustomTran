package lft5_1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Lexer {

    public static int line = 1;
    private char peek = ' ';

    private void readch(BufferedReader br) {
        try {
            peek = (char) br.read();
        } catch (IOException exc) {
            peek = (char) -1; // ERROR
        }
    }

    public Token lexical_scan(BufferedReader br) {
        while (peek == ' ' || peek == '\t' || peek == '\n'  || peek == '\r') {
            if (peek == '\n') line++;
            readch(br);
        }

        switch (peek) {
            case '!':
                peek = ' ';
                return Token.not;
            case '(':
                peek = ' ';
                return Token.lpt;
            case ')':
                peek = ' ';
                return Token.rpt;
            case '{':
                peek = ' ';
                return Token.lpg;
            case '}':
                peek = ' ';
                return Token.rpg;
            case '+':
                peek = ' ';
                return Token.plus;
            case '-':
                peek = ' ';
                return Token.minus;
            case '*':
                peek = ' ';
                return Token.mult;
            case '/':
                readch(br);
                if(peek == '/'){
                    while(peek != '\n' && peek != (char)-1)
                        readch(br);
                    return lexical_scan(br);
                }
                else if(peek == '*'){
                    peek = ' ';
                    while(peek == ' '){
                        readch(br);
                        if(peek == '*'){
                            readch(br);
                            if(peek == '/') {
                                readch(br);
                                return lexical_scan(br);
                            }
                            else
                                peek = ' ';
                        }
                        else if(peek == (char)-1){
                            System.err.println("Unable to find end of comment" );
                            return null;
                        }
                        else
                            peek = ' ';
                    }
                }
                return Token.div;
            case ';':
                peek = ' ';
                return Token.semicolon;

            case '&':
                readch(br);
                if (peek == '&') {
                    peek = ' ';
                    return Word.and;
                } else {
                    System.err.println("Erroneous character"
                            + " after & : "  + peek );
                    return null;
                }
            case '|':
                readch(br);
                if (peek == '|') {
                    peek = ' ';
                    return Word.or;
                } else {
                    System.err.println("Erroneous character"
                            + " after | : "  + peek );
                    return null;
                }
            case '>':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.ge;
                }
                else {
                    return Word.gt;
                }
            case '<':
                readch(br);
                if (peek == '>') {
                    peek = ' ';
                    return Word.ne;
                }
                else if(peek == '=') {
                    peek = ' ';
                    return Word.le;
                }
                else
                    return Word.lt;
            case '=':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.eq;
                } else {
                    return Word.assign;
                }
            case (char)-1:
                return new Token(Tag.EOF);

            default:
                if (Character.isLetter(peek)) {
                    StringBuilder s = new StringBuilder();
                    while(Character.isLetter(peek) || Character.isDigit(peek)){

                        s.append(peek);
                        readch(br);
                    }
                    if(s.toString().equals(Word.cond.lexeme))
                        return Word.cond;
                    if(s.toString().equals(Word.dotok.lexeme))
                        return Word.dotok;
                    if(s.toString().equals(Word.elsetok.lexeme))
                        return Word.elsetok;
                    if(s.toString().equals(Word.print.lexeme))
                        return Word.print;
                    if(s.toString().equals(Word.read.lexeme))
                        return Word.read;
                    if(s.toString().equals(Word.when.lexeme))
                        return Word.when;
                    if(s.toString().equals(Word.whiletok.lexeme))
                        return Word.whiletok;
                    if(s.toString().equals(Word.seq.lexeme))
                        return Word.seq;
                    if(s.toString().equals(Word.then.lexeme))
                        return Word.then;
                    return new Word(Tag.ID, s.toString());


                } else if (Character.isDigit(peek)) {

                    StringBuilder s = new StringBuilder();
                    while (Character.isDigit(peek)){

                        s.append(peek);
                        readch(br);
                    }
                    return new NumberTok(s.toString());
                } else {
                    System.err.println("Erroneous character: "
                            + peek );
                    return null;
                }
        }
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "C:\\test.txt"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Token tok;
            do {
                tok = lex.lexical_scan(br);
                System.out.println("Scan: " + tok);
            } while (tok.tag != Tag.EOF);
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }

}
