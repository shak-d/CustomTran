package lft5_1;

import java.io.*;

public class Translator {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    SymbolTable st = new SymbolTable();
    CodeGenerator code = new CodeGenerator();
    int count=0;

    public Translator(Lexer l, BufferedReader br) {
        lex = l;
        pbr = br;
        move();
    }

    void move() {
        look = lex.lexical_scan(pbr);
        System.out.println("token = " + look);
    }

    void error(String s) {
        throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t) {
        if (look.tag == t) {
            if (look.tag != Tag.EOF) move();
        } else error("syntax error");
    }

    public void prog() {
        if(look.tag == '(') {
            int lnext_prog = code.newLabel();
            stat(lnext_prog);
            code.emitLabel(lnext_prog);
            match(Tag.EOF);
            try {
                code.toJasmin();
            } catch (java.io.IOException e) {
                System.out.println("IO error\n");
            }
        }
        else error("syntax error");
    }

    public void statlist(int next){

        if(look.tag == '('){
            int nextq = code.newLabel();
            stat(nextq);
            code.emitLabel(nextq);
            statlistp(next);

        }
        else error("syntax error");
    }

    public void statlistp(int next){

        if(look.tag == '('){
            int nextq = code.newLabel();
            stat(nextq);
            code.emitLabel(nextq);
            statlistp(next);

        }
        else if(look.tag != ')')
            error("syntax error");

    }

    public void stat(int lnext_prog){

        if(look.tag == '('){
            match('(');
            statp(lnext_prog);
            match(')');
        }
        else error("syntax error");

    }

    public void statp(int lnext) {
        switch(look.tag) {

            case '=':
                match('=');
                int addr = st.lookupAddress(((Word)look).lexeme);
                if (addr==-1) {
                    addr = count;
                    st.insert(((Word)look).lexeme,count++);
                }
                match(Tag.ID);
                expr();
                code.emit(OpCode.istore, addr);
                break;
            case Tag.WHILE:
                match(Tag.WHILE);
                int b_true, b_false, begin;
                begin = code.newLabel();
                b_true = code.newLabel();
                b_false = lnext;
                code.emitLabel(begin);
                bexpr(b_true, b_false);
                code.emitLabel(b_true);
                stat(begin);
                code.emit(OpCode.GOto, begin);
                break;
            case Tag.COND:
                match(Tag.COND);
                int be_true, be_false;
                be_true = code.newLabel();
                be_false = code.newLabel();
                bexpr(be_true, be_false);
                code.emitLabel(be_true);
                stat(lnext);
                code.emit(OpCode.GOto, lnext);
                code.emitLabel(be_false);
                elseopt(lnext);
                break;
            case Tag.DO:
                match(Tag.DO);
                statlist(lnext);
                break;

            case Tag.PRINT:
                match(Tag.PRINT);
                exprlist();
                code.emit(OpCode.invokestatic,1);
                break;
            case Tag.READ:
                match(Tag.READ);
                if (look.tag==Tag.ID) {
                    int read_id_addr = st.lookupAddress(((Word)look).lexeme);
                    if (read_id_addr==-1) {
                        read_id_addr = count;
                        st.insert(((Word)look).lexeme,count++);
                    }
                    match(Tag.ID);
                    code.emit(OpCode.invokestatic,0);
                    code.emit(OpCode.istore,read_id_addr);
                }
                else
                    error("Error in grammar (stat) after read with " + look);
                break;
            default:
                error("syntax error");
                break;
        }
    }

    public void elseopt(int next){

        if(look.tag == '('){

            match('(');
            match(Tag.ELSE);
            stat(next);
            match(')');
            code.emit(OpCode.GOto, next);
        }
        else if(look.tag != ')')
            error("syntax error");
    }

    public void bexpr(int btrue, int bfalse){

        if(look.tag == '('){

            match('(');
            bexprp(btrue, bfalse);
            match(')');
        }
        else error("syntax error");
    }

    public void bexprp(int btrue, int bfalse){

        if(look.tag == Tag.RELOP){
            String le = ((Word)look).lexeme;
            if(le.equals("==")){
                match(Tag.RELOP);
                expr();
                expr();
                code.emit(OpCode.if_icmpeq, btrue);
                code.emit(OpCode.GOto, bfalse);
            }
            else if(le.equals("<")){
                match(Tag.RELOP);
                expr();
                expr();
                code.emit(OpCode.if_icmplt, btrue);
                code.emit(OpCode.GOto, bfalse);
            }
            else if(le.equals("<=")){
                match(Tag.RELOP);
                expr();
                expr();
                code.emit(OpCode.if_icmple, btrue);
                code.emit(OpCode.GOto, bfalse);
            }
            else if(le.equals(">")){
                match(Tag.RELOP);
                expr();
                expr();
                code.emit(OpCode.if_icmpgt, btrue);
                code.emit(OpCode.GOto, bfalse);
            }
            else if(le.equals(">=")){
                match(Tag.RELOP);
                expr();
                expr();
                code.emit(OpCode.if_icmpge, btrue);
                code.emit(OpCode.GOto, bfalse);
            }
            else if(le.equals("<>")){
                match(Tag.RELOP);
                expr();
                expr();
                code.emit(OpCode.if_icmpne, btrue);
                code.emit(OpCode.GOto, bfalse);
            }

        }
        else error("syntax error");
    }

    public void expr(){

        if(look.tag == Tag.NUM){
            code.emit(OpCode.ldc, ((NumberTok)look).lexeme);
            match(Tag.NUM);
        }
        else if(look.tag == Tag.ID) {
            int address = st.lookupAddress(((Word)look).lexeme);
            if (address==-1) {
                address = count;
                st.insert(((Word)look).lexeme,count++);
            }
            match(Tag.ID);
            code.emit(OpCode.iload, address);
        }
        else if(look.tag == '('){

            match('(');
            exprp();
            match(')');
        }
        else error("syntax error");
    }

    private void exprp() {
        switch(look.tag) {
            case '+':
                match('+');
                exprlist();
                code.emit(OpCode.iadd);
                break;
            case '-':
                match('-');
                expr();
                expr();
                code.emit(OpCode.isub);
                break;
            case '*':
                match('*');
                exprlist();
                code.emit(OpCode.imul);
                break;
            case '/':
                match('/');
                expr();
                expr();
                code.emit(OpCode.idiv);
                break;
            default:
                error("syntax error");
                break;
        }
    }

    public void exprlist(){

        if(look.tag == Tag.NUM || look.tag == Tag.ID || look.tag == '('){

            expr();
            exprlstp();
        }
        else error("syntax error");
    }

    public void exprlstp(){

        if(look.tag == Tag.NUM || look.tag == Tag.ID || look.tag == '('){

            expr();
            exprlstp();
        }
        else if(look.tag != ')')
            error("syntax error");

    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "C://test.txt"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Translator translator = new Translator(lex, br);
            translator.prog();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}