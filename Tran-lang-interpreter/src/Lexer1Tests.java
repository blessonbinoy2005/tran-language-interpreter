import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.LinkedList;

public class Lexer1Tests {

    @Test
    public void SimpleLexerTest() {
        var l = new Lexer("ababab cdcd ef gh ijij kl mnop");
        try {
            var res = l.Lex();
            Assertions.assertEquals(7, res.size());
            Assertions.assertEquals("ababab", res.get(0).getValue());
            Assertions.assertEquals("cdcd", res.get(1).getValue());
            Assertions.assertEquals("ef", res.get(2).getValue());
            Assertions.assertEquals("gh", res.get(3).getValue());
            Assertions.assertEquals("ijij", res.get(4).getValue());
            Assertions.assertEquals("kl", res.get(5).getValue());
            Assertions.assertEquals("mnop", res.get(6).getValue());
            for (var result : res)
                Assertions.assertEquals(Token.TokenTypes.WORD, result.getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void MultilineLexerTest() {
        var l = new Lexer("ababab cdcd ef gh ijij kl mnop\nasdjkdsajkl\ndsajkdsa   asdjksald dsajhkl \n\n\n");
        try {
            var res = l.Lex();
            Assertions.assertEquals(16, res.size());
            Assertions.assertEquals("ababab", res.get(0).getValue());
            Assertions.assertEquals("cdcd", res.get(1).getValue());
            Assertions.assertEquals("ef", res.get(2).getValue());
            Assertions.assertEquals("gh", res.get(3).getValue());
            Assertions.assertEquals("ijij", res.get(4).getValue());
            Assertions.assertEquals("kl", res.get(5).getValue());
            Assertions.assertEquals("mnop", res.get(6).getValue());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(7).getType());
            Assertions.assertEquals("asdjkdsajkl", res.get(8).getValue());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(9).getType());
            Assertions.assertEquals("dsajkdsa", res.get(10).getValue());
            Assertions.assertEquals("asdjksald", res.get(11).getValue());
            Assertions.assertEquals("dsajhkl", res.get(12).getValue());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(13).getType());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(14).getType());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(15).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void IndentTest2() {
        var l = new Lexer(
                "loop keepGoing\n" +
                        "    if n >= 15\n" +
                        "        keepGoing = false\n" +
                        "loop keepGoing\n" +
                        "    if n >= 15\n" +
                        "        keepGoing = false\n"
        );
        try {
            var res = l.Lex();
            Assertions.assertEquals(32, res.size());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void NewlineTest() {
        var l = new Lexer("\n");
        try {
            var res = l.Lex();
            Assertions.assertEquals(1, res.size());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(0).getType());

        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void NotEqualsTest() {
        var l = new Lexer("!= !");
        try {
            var res = l.Lex();
            Assertions.assertEquals(2, res.size());
            Assertions.assertEquals(Token.TokenTypes.NOTEQUAL, res.get(0).getType());
            Assertions.assertEquals(Token.TokenTypes.NOT, res.get(1).getType());



        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void IndentTest() {
        var l = new Lexer(
                "loop keepGoing\n" +
                    "    if n >= 15\n" +
	                "        keepGoing = false\n"
                 );
        try {
            var res = l.Lex();
            Assertions.assertEquals(16, res.size());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void TwoCharacterTest() {
        var l = new Lexer(">= > <= < = == || != &&");
        try {
            var res = l.Lex();
            Assertions.assertEquals(9, res.size());
            Assertions.assertEquals(Token.TokenTypes.GREATERTHANEQUAL, res.get(0).getType());
            Assertions.assertEquals(Token.TokenTypes.GREATERTHAN, res.get(1).getType());
            Assertions.assertEquals(Token.TokenTypes.LESSTHANEQUAL, res.get(2).getType());
            Assertions.assertEquals(Token.TokenTypes.LESSTHAN, res.get(3).getType());
            Assertions.assertEquals(Token.TokenTypes.ASSIGN, res.get(4).getType());
            Assertions.assertEquals(Token.TokenTypes.EQUAL, res.get(5).getType());
            Assertions.assertEquals(Token.TokenTypes.OR, res.get(6).getType());
            Assertions.assertEquals(Token.TokenTypes.NOTEQUAL, res.get(7).getType());
            Assertions.assertEquals(Token.TokenTypes.AND, res.get(8).getType());

        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void OrAnd() {
        var l = new Lexer("||");
        try {
            var res = l.Lex();
            Assertions.assertEquals(1, res.size());
            Assertions.assertEquals(Token.TokenTypes.OR, res.get(0).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void MixedTest() {
        var l = new Lexer("word 1.2 : ( )");
        try {
            var res = l.Lex();
            Assertions.assertEquals(5, res.size());
            Assertions.assertEquals(Token.TokenTypes.WORD, res.get(0).getType());
            Assertions.assertEquals("word", res.get(0).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(1).getType());
            Assertions.assertEquals("1.2", res.get(1).getValue());
            Assertions.assertEquals(Token.TokenTypes.COLON, res.get(2).getType());
            Assertions.assertEquals(Token.TokenTypes.LPAREN, res.get(3).getType());
            Assertions.assertEquals(Token.TokenTypes.RPAREN, res.get(4).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }


    @Test
    public void EvilNumberTest() {
        var l = new Lexer("1.23 1.23 .45");
        try {
            var res = l.Lex();
            Assertions.assertEquals(3, res.size());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(0).getType());
            Assertions.assertEquals("1.23", res.get(0).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(1).getType());
            Assertions.assertEquals("1.23", res.get(1).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(2).getType());
            Assertions.assertEquals(".45", res.get(2).getValue());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void NumberTest() {
        var l = new Lexer("123 456 -789 0");
        try {
            var res = l.Lex();
            Assertions.assertEquals(5, res.size());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(0).getType());
            Assertions.assertEquals("123", res.get(0).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(1).getType());
            Assertions.assertEquals("456", res.get(1).getValue());
            Assertions.assertEquals(Token.TokenTypes.MINUS, res.get(2).getType());
            Assertions.assertEquals("", res.get(2).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(3).getType());
            Assertions.assertEquals("789", res.get(3).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(4).getType());
            Assertions.assertEquals("0", res.get(4).getValue());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void FractionNumberTest() {
        var l = new Lexer(".2 0.5 -1.6 -.8");
        try {
            var res = l.Lex();
            Assertions.assertEquals(6, res.size());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(0).getType());
            Assertions.assertEquals(".2", res.get(0).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(1).getType());
            Assertions.assertEquals("0.5", res.get(1).getValue());
            Assertions.assertEquals(Token.TokenTypes.MINUS, res.get(2).getType());
            Assertions.assertEquals("", res.get(2).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(3).getType());
            Assertions.assertEquals("1.6", res.get(3).getValue());
            Assertions.assertEquals(Token.TokenTypes.MINUS, res.get(4).getType());
            Assertions.assertEquals("", res.get(4).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(5).getType());
            Assertions.assertEquals(".8", res.get(5).getValue());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    //my test codes starts here

    @Test
    public void practice1() {
        var l = new Lexer("3.452");
        try {
            var res = l.Lex();
            Assertions.assertEquals(1, res.size());
            Assertions.assertEquals("3.452", res.get(0).getValue());
//            Assertions.assertEquals("cd", res.get(1).getValue());
//            Assertions.assertEquals("ef", res.get(2).getValue());
//            Assertions.assertEquals("gh", res.get(3).getValue());
            for (var result : res)
                Assertions.assertEquals(Token.TokenTypes.NUMBER, result.getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void WordsNumbersMixedTest() {
        var l = new Lexer("word 1.3 mixed 57");
        try {
            var res = l.Lex();
            Assertions.assertEquals(4, res.size());
            Assertions.assertEquals(Token.TokenTypes.WORD, res.get(0).getType());
            Assertions.assertEquals("word", res.get(0).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(1).getType());
            Assertions.assertEquals("1.3", res.get(1).getValue());
            Assertions.assertEquals(Token.TokenTypes.WORD, res.get(2).getType());
            Assertions.assertEquals("mixed", res.get(2).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(3).getType());
            Assertions.assertEquals("57", res.get(3).getValue());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void DOT() {
        var l = new Lexer(".");
        try {
            var res = l.Lex();
            Assertions.assertEquals(1, res.size());
            Assertions.assertEquals(Token.TokenTypes.DOT, res.get(0).getType());

//            for (var result : res)
//                Assertions.assertEquals(Token.TokenTypes.NUMBER, result.getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void MoreDot() {
        var l = new Lexer(".344 3.4 .");
        try {
            var res = l.Lex();
            Assertions.assertEquals(3, res.size());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(0).getType());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(1).getType());
            Assertions.assertEquals(Token.TokenTypes.DOT, res.get(2).getType());


//            for (var result : res)
//                Assertions.assertEquals(Token.TokenTypes.NUMBER, result.getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void DoubleDot() {
        var l = new Lexer("3.4.2");
        try {
            var res = l.Lex();
//            Assertions.assertEquals(2, res.size());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(0).getType());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(1).getType());

//            Assertions.assertEquals("3.4.2", res.get(0).getValue());


//            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(2).getType());


        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void practice1o1() {
        var l = new Lexer(".3452");
        try {
            var res = l.Lex();
            Assertions.assertEquals(1, res.size());
            Assertions.assertEquals(".3452", res.get(0).getValue());

            for (var result : res)
                Assertions.assertEquals(Token.TokenTypes.NUMBER, result.getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void practice2() {
        var l = new Lexer("3452 ab3 55");
        try {
            var res = l.Lex();
            Assertions.assertEquals(4, res.size());
            Assertions.assertEquals("3452", res.get(0).getValue());
            Assertions.assertEquals("ab", res.get(1).getValue());
            Assertions.assertEquals("3", res.get(2).getValue());
            Assertions.assertEquals("55", res.get(3).getValue());
//            Assertions.assertEquals("gh", res.get(3).getValue());
//            for (var result : res)
//                Assertions.assertEquals(Token.TokenTypes.NUMBER, result.getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void practice3() {
//        var l = new Lexer("3452" +
//                "  ab3 55\n 56 7a \n ");
        var l = new Lexer("34" +
                "  a\n6\n");
        try {
            var res = l.Lex();
            Assertions.assertEquals(5, res.size());


//            Assertions.assertEquals(9, res.size());
//            Assertions.assertEquals("3452", res.get(0).getValue());
//            Assertions.assertEquals("ab", res.get(1).getValue());
//            Assertions.assertEquals("3", res.get(2).getValue());
//            Assertions.assertEquals("55", res.get(3).getValue());
//            Assertions.assertEquals("gh", res.get(3).getValue());
//            Assertions.assertEquals(Token.TokenTypes.GREATERTHANEQUAL, res.get(0).getType());

//            for (var result : res)
//                Assertions.assertEquals(Token.TokenTypes.NUMBER, result.getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void practice4() {
//        var l = new Lexer("+ -- if");s
//        var l = new Lexer("+");
//        var l = new Lexer("if");
        var l = new Lexer(">=");
//        var l = new Lexer("!=");

//        var l = new Lexer("accessor \n mutator 345 * 99");

        try {
            var res = l.Lex();
//            Assertions.assertEquals(4, res.size());
//            Assertions.assertEquals(Token.TokenTypes.PLUS, res.get(0).getType());
//            Assertions.assertEquals(Token.TokenTypes.MINUS, res.get(1).getType());
//            Assertions.assertEquals(Token.TokenTypes.MINUS, res.get(2).getType());
            Assertions.assertEquals(Token.TokenTypes.GREATERTHANEQUAL, res.get(0).getType());
//            Assertions.assertEquals(Token.TokenTypes.NOTEQUAL, res.get(0).getType());

//              Assertions.assertEquals(Token.TokenTypes.IF, res.get(0).getType());

//            Assertions.assertEquals(Token.TokenTypes.ACCESSOR, res.get(0).getType());
//            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(1).getType());
//            Assertions.assertEquals(Token.TokenTypes.MUTATOR, res.get(2).getType());
//            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(3).getType());
//            Assertions.assertEquals(Token.TokenTypes.TIMES, res.get(4).getType());
//            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(5).getType());


        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void practice5() {
        var l = new Lexer(">= > <= < = == != if+123 word \n<> , +- == () 5.4 .333 . ifif .");
        try {
            var res = l.Lex();
            Assertions.assertEquals(26, res.size());
            Assertions.assertEquals(Token.TokenTypes.GREATERTHANEQUAL, res.get(0).getType());
            Assertions.assertEquals(Token.TokenTypes.GREATERTHAN, res.get(1).getType());
            Assertions.assertEquals(Token.TokenTypes.LESSTHANEQUAL, res.get(2).getType());
            Assertions.assertEquals(Token.TokenTypes.LESSTHAN, res.get(3).getType());
            Assertions.assertEquals(Token.TokenTypes.ASSIGN, res.get(4).getType());
            Assertions.assertEquals(Token.TokenTypes.EQUAL, res.get(5).getType());
            Assertions.assertEquals(Token.TokenTypes.NOTEQUAL, res.get(6).getType());
            Assertions.assertEquals(Token.TokenTypes.IF, res.get(7).getType());
            Assertions.assertEquals(Token.TokenTypes.PLUS, res.get(8).getType());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(9).getType());
            Assertions.assertEquals(Token.TokenTypes.WORD, res.get(10).getType());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(11).getType());
            Assertions.assertEquals(Token.TokenTypes.LESSTHAN, res.get(12).getType());
            Assertions.assertEquals(Token.TokenTypes.GREATERTHAN, res.get(13).getType());
            Assertions.assertEquals(Token.TokenTypes.COMMA, res.get(14).getType());
            Assertions.assertEquals(Token.TokenTypes.PLUS, res.get(15).getType());
            Assertions.assertEquals(Token.TokenTypes.MINUS, res.get(16).getType());
            Assertions.assertEquals(Token.TokenTypes.EQUAL, res.get(17).getType());
            Assertions.assertEquals(Token.TokenTypes.LPAREN, res.get(18).getType());
            Assertions.assertEquals(Token.TokenTypes.RPAREN, res.get(19).getType());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(20).getType());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(21).getType());
            Assertions.assertEquals(Token.TokenTypes.DOT, res.get(22).getType());
            Assertions.assertEquals(Token.TokenTypes.IF, res.get(23).getType());
            Assertions.assertEquals(Token.TokenTypes.IF, res.get(24).getType());
            Assertions.assertEquals(Token.TokenTypes.DOT, res.get(25).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void practice52() {
        var l = new Lexer(".333 . ifif .");
        try {
            var res = l.Lex();
            Assertions.assertEquals(5, res.size());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(0).getType());
            Assertions.assertEquals(Token.TokenTypes.DOT, res.get(1).getType());
            Assertions.assertEquals(Token.TokenTypes.IF, res.get(2).getType());
            Assertions.assertEquals(Token.TokenTypes.IF, res.get(3).getType());
            Assertions.assertEquals(Token.TokenTypes.DOT, res.get(4).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void practice6() {
        var l = new Lexer(".3");
        try {
            var res = l.Lex();
            Assertions.assertEquals(1, res.size());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(0).getType());


        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void practice1o1ex() {
        var l = new Lexer(".3452");
        try {
            var res = l.Lex();
            Assertions.assertEquals(1, res.size());
            Assertions.assertEquals(".3452", res.get(0).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(0).getType());

            for (var result : res)
                Assertions.assertEquals(Token.TokenTypes.NUMBER, result.getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    //my test codes ends here
}
