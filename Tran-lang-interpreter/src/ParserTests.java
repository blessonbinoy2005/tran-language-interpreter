import AST.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ParserTests {
    private TranNode LexAndParse(String input, int tokenCount) throws Exception {
        var l = new Lexer(input);
        var tokens = l.Lex();
        assertEquals(tokenCount, tokens.size());
        var tran = new TranNode();
        var p = new Parser(tran, tokens);
        p.Tran();
        return tran;
    }

    @Test
    public void testInterface() throws Exception {
        var t = LexAndParse("interface someName\r\n\tupdateClock()\r\n\tsquare() : number s", 15);
        assertEquals(1, t.Interfaces.size());
        assertEquals(2, t.Interfaces.getFirst().methods.size());
    }

    //parse 1 test starts here
    @Test
    public void testInterface2() throws Exception {
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Token.TokenTypes.INTERFACE, 1, 1, "interface"));
        tokens.add(new Token(Token.TokenTypes.WORD, 1, 11, "someName"));
        tokens.add(new Token(Token.TokenTypes.NEWLINE, 1, 19));
        tokens.add(new Token(Token.TokenTypes.INDENT, 2, 1));
        tokens.add(new Token(Token.TokenTypes.WORD, 2, 2, "updateClock"));
        tokens.add(new Token(Token.TokenTypes.LPAREN, 2, 13));

        tokens.add(new Token(Token.TokenTypes.WORD, 3, 13, "number"));
        tokens.add(new Token(Token.TokenTypes.WORD, 3, 13, "number"));

        tokens.add(new Token(Token.TokenTypes.COMMA, 3, 13));
        tokens.add(new Token(Token.TokenTypes.WORD, 3, 13, "number"));
        tokens.add(new Token(Token.TokenTypes.WORD, 3, 13, "number"));

        tokens.add(new Token(Token.TokenTypes.RPAREN, 2, 14));
        tokens.add(new Token(Token.TokenTypes.NEWLINE, 2, 15));
        tokens.add(new Token(Token.TokenTypes.WORD, 3, 2, "square"));
        tokens.add(new Token(Token.TokenTypes.LPAREN, 3, 8));
        tokens.add(new Token(Token.TokenTypes.RPAREN, 3, 9));
        tokens.add(new Token(Token.TokenTypes.COLON, 3, 11));
        tokens.add(new Token(Token.TokenTypes.WORD, 3, 13, "number"));
        tokens.add(new Token(Token.TokenTypes.WORD, 3, 20, "s"));

        tokens.add(new Token(Token.TokenTypes.COMMA, 3, 13));
        tokens.add(new Token(Token.TokenTypes.WORD, 3, 13, "number"));
        tokens.add(new Token(Token.TokenTypes.WORD, 3, 13, "number"));

        tokens.add(new Token(Token.TokenTypes.DEDENT, 4, 23));

        var tran = new TranNode();
        var p = new Parser(tran, tokens);
        p.Tran();
        assertEquals(1, tran.Interfaces.size());
        assertEquals(2, tran.Interfaces.getFirst().methods.size());
    }

    @Test
    public void testParserConstructor() throws Exception {
        // Given an input string and expected token count
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Token.TokenTypes.INTERFACE, 1, 1, "interface"));
        tokens.add(new Token(Token.TokenTypes.WORD, 1, 11, "someName"));
        tokens.add(new Token(Token.TokenTypes.NEWLINE, 1, 19));
        tokens.add(new Token(Token.TokenTypes.INDENT, 2, 1));
        tokens.add(new Token(Token.TokenTypes.WORD, 2, 2, "updateClock"));
        tokens.add(new Token(Token.TokenTypes.LPAREN, 2, 13));
        tokens.add(new Token(Token.TokenTypes.RPAREN, 2, 14));
        tokens.add(new Token(Token.TokenTypes.NEWLINE, 2, 15));
        tokens.add(new Token(Token.TokenTypes.WORD, 3, 2, "square"));
        tokens.add(new Token(Token.TokenTypes.LPAREN, 3, 8));
        tokens.add(new Token(Token.TokenTypes.RPAREN, 3, 9));
        tokens.add(new Token(Token.TokenTypes.COLON, 3, 11));
        tokens.add(new Token(Token.TokenTypes.WORD, 3, 13, "number"));
        tokens.add(new Token(Token.TokenTypes.WORD, 3, 20, "s"));
        tokens.add(new Token(Token.TokenTypes.DEDENT, 4, 23));

        var tran = new TranNode();
        var p = new Parser(tran, tokens);

        // Create a TranNode
        TranNode tranNode = new TranNode();

        // Create the Parser with the TranNode and tokens
        Parser parser = new Parser(tranNode, tokens);

    }

    // Helper method to create tokens
    private Token createToken(Token.TokenTypes type, int line, int column, String value) {
        return new Token(type, line, column, value);
    }

    private Token createToken(Token.TokenTypes type, int line, int column) {
        return new Token(type, line, column);
    }


    @Test
    public void testMatchAndRemove() {
        Token token1 = createToken(Token.TokenTypes.WORD, 1, 1, "hello");
        Token token2 = createToken(Token.TokenTypes.NUMBER, 1, 2, "123");
        TokenManager tokenManager = new TokenManager(new LinkedList<>(Arrays.asList(token1, token2)));

        // Check if the first token matches and is removed
        Optional<Token> matchedToken = tokenManager.matchAndRemove(Token.TokenTypes.WORD);
        assertTrue(matchedToken.isPresent(), "Token should match WORD and be removed");
        assertEquals(token1, matchedToken.get(), "The matched token should be the first token");

        // Check if the second token is now the first
        Optional<Token> nextToken = tokenManager.matchAndRemove(Token.TokenTypes.NUMBER);
        assertTrue(nextToken.isPresent(), "Token should match NUMBER and be removed");
        assertEquals(token2, nextToken.get(), "The next token should be the second token");

        // Check if token manager is empty
        assertTrue(tokenManager.done(), "Token manager should be empty");
    }

    @Test
    public void testPeek() {
        Token token1 = createToken(Token.TokenTypes.WORD, 1, 1, "hello");
        Token token2 = createToken(Token.TokenTypes.NUMBER, 1, 2, "123");
        TokenManager tokenManager = new TokenManager(new LinkedList<>(Arrays.asList(token1, token2)));

        // Check peeking the first token
        Optional<Token> peekToken = tokenManager.peek(0);
        assertTrue(peekToken.isPresent(), "First token should be peeked");
        assertEquals(token1, peekToken.get(), "The first peeked token should be the first token");

        // Check peeking the second token
        Optional<Token> secondPeekToken = tokenManager.peek(1);
        assertTrue(secondPeekToken.isPresent(), "Second token should be peeked");
        assertEquals(token2, secondPeekToken.get(), "The second peeked token should be the second token");


    }

    @Test
    public void testNextTwoTokensMatch() {
        Token token1 = createToken(Token.TokenTypes.WORD, 1, 1, "hello");
        Token token2 = createToken(Token.TokenTypes.NUMBER, 1, 2, "123");
        TokenManager tokenManager = new TokenManager(new LinkedList<>(Arrays.asList(token1, token2)));

        // Check if the first two tokens match the given types
        assertTrue(tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.NUMBER),
                "First two tokens should match WORD and NUMBER");


    }

    @Test
    public void testGetCurrentLine() {
        Token token1 = createToken(Token.TokenTypes.WORD, 1, 1, "hello");
        TokenManager tokenManager = new TokenManager(new LinkedList<>(Arrays.asList(token1)));

        // Check if the current line is returned correctly
        assertEquals(1, tokenManager.getCurrentLine(), "The current line should be 1");
    }

    @Test
    public void getCurrentColumnNumber() {
        Token token1 = createToken(Token.TokenTypes.WORD, 1, 5, "hello");
        TokenManager tokenManager = new TokenManager(new LinkedList<>(Arrays.asList(token1)));

        // Check if the current column is returned correctly
        assertEquals(5, tokenManager.getCurrentColumn(), "The current column should be 5");
    }
    //parse 1 test ends here

    @Test
    public void testClassWithOneMethod() throws Exception {
        var t = LexAndParse("class Tran\r\n\thelloWorld()\r\n\t\tx = 1 + 1", 16);
        assertEquals(1, t.Classes.size());
        assertEquals(1, t.Classes.getFirst().methods.size());
        assertEquals(1, t.Classes.getFirst().methods.getFirst().statements.size());
    }

    @Test
    public void testClassWithMultipleMembers() throws Exception {
        var t = LexAndParse("class Tran\n" +
                "\tnumber w\n" +
                "\tstring x\n" +
                "\tboolean y\n" +
                "\tcharacter z", 16);
        assertEquals(1, t.Classes.size());
        assertEquals(4, t.Classes.getFirst().members.size());
        var m = t.Classes.getFirst().members;
        assertEquals("number", m.getFirst().declaration.type);
        assertEquals("w", m.getFirst().declaration.name);
        assertEquals("string", m.get(1).declaration.type);
        assertEquals("x", m.get(1).declaration.name);
        assertEquals("boolean", m.get(2).declaration.type);
        assertEquals("y", m.get(2).declaration.name);
        assertEquals("character", m.get(3).declaration.type);
        assertEquals("z", m.get(3).declaration.name);
    }

    @Test
    public void testClassWithMethodsAndMembers() throws Exception {
        var t = LexAndParse("class Tran\n" +
                        "\tnumber w\n" +
                        "\tstring x\n" +
                        "\tboolean y\n" +
                        "\tcharacter z\n" +
                        "\thelloWorld()\n" +
                        "\t\tx = 1 + 1"
                , 28);
        assertEquals(1, t.Classes.size());
        var m = t.Classes.getFirst().members;
        assertEquals(4, t.Classes.getFirst().members.size()); // scramble test order to break the "duplicate code" warning
        assertEquals("boolean", m.get(2).declaration.type);
        assertEquals("y", m.get(2).declaration.name);
        assertEquals("character", m.get(3).declaration.type);
        assertEquals("z", m.get(3).declaration.name);
        assertEquals("string", m.get(1).declaration.type);
        assertEquals("x", m.get(1).declaration.name);
        assertEquals("number", m.getFirst().declaration.type);
        assertEquals("w", m.getFirst().declaration.name);

        assertEquals(1, t.Classes.getFirst().methods.size());
        assertEquals(1, t.Classes.getFirst().methods.getFirst().statements.size());
    }

    @Test
    public void testClassIf() throws Exception {
        var t = LexAndParse("class Tran\n" +
                        "\thelloWorld()\n" +
                        "\t\tif n>100\n" +
                        "\t\t\tkeepGoing = false"
                , 21);
        assertEquals(1, t.Classes.size());
        var myClass = t.Classes.getFirst();
        assertEquals(1, myClass.methods.size());
        var myMethod = myClass.methods.getFirst();
        assertEquals(1, myMethod.statements.size());
        assertEquals("AST.IfNode", myMethod.statements.getFirst().getClass().getName());
        assertTrue(((IfNode)(myMethod.statements.getFirst())).elseStatement.isEmpty());
    }

    @Test
    public void testClassIfElse() throws Exception {
        var t = LexAndParse("class Tran\n" +
                        "\thelloWorld()\n" +
                        "\t\tif n>100\n" +
                        "\t\t\tkeepGoing = false\n" +
                        "\t\telse\n" +
                        "\t\t\tkeepGoing = true\n"
                , 30);
        assertEquals(1, t.Classes.size());
        var myClass = t.Classes.getFirst();
        assertEquals(1, myClass.methods.size());
        var myMethod = myClass.methods.getFirst();
        assertEquals(1, myMethod.statements.size());
        assertEquals("AST.IfNode", myMethod.statements.getFirst().getClass().getName());
        assertTrue(((IfNode)(myMethod.statements.getFirst())).elseStatement.isPresent());
        assertEquals(1,((IfNode)(myMethod.statements.getFirst())).elseStatement.orElseThrow().statements.size());
    }

    @Test
    public void testLoopVariable() throws Exception {
        var t = LexAndParse("class Tran\n" +
                        "\thelloWorld()\n" +
                        "\t\tloop n\n" +
                        "\t\t\tkeepGoing = false\n"
                , 20);
        assertEquals(1, t.Classes.size());
        var myClass = t.Classes.getFirst();
        assertEquals(1, myClass.methods.size());
        var myMethod = myClass.methods.getFirst();
        assertEquals(1, myMethod.statements.size());
        Assertions.assertInstanceOf(LoopNode.class, myMethod.statements.getFirst());
    }

    @Test
    public void testLoopCondition() throws Exception {
        var t = LexAndParse("class Tran\n" +
                        "\thelloWorld()\n" +
                        "\t\tloop n<100\n" +
                        "\t\t\tkeepGoing = false\n"
                , 22);
        assertEquals(1, t.Classes.size());
        var myClass = t.Classes.getFirst();
        assertEquals(1, myClass.methods.size());
        var myMethod = myClass.methods.getFirst();
        assertEquals(1, myMethod.statements.size());
        Assertions.assertInstanceOf(LoopNode.class, myMethod.statements.getFirst());
        Assertions.assertInstanceOf(CompareNode.class, ((LoopNode) myMethod.statements.getFirst()).expression);
    }

    @Test
    public void testLoopConditionWithVariable() throws Exception {
        var t = LexAndParse("class Tran\n" +
                        "\thelloWorld()\n" +
                        "\t\tloop c = n<100\n" +
                        "\t\t\tkeepGoing = false\n"
                , 24);
        assertEquals(1, t.Classes.size());
        var myClass = t.Classes.getFirst();
        assertEquals(1, myClass.methods.size());
        var myMethod = myClass.methods.getFirst();
        assertEquals(1, myMethod.statements.size());
        Assertions.assertInstanceOf(LoopNode.class, myMethod.statements.getFirst());
        Assertions.assertInstanceOf(CompareNode.class, ((LoopNode) myMethod.statements.getFirst()).expression);
        assertTrue(((LoopNode) myMethod.statements.getFirst()).assignment.isPresent());
    }

    @Test
    public void testMethodCallWithMulitpleVariables() throws Exception {
        var t = LexAndParse("class Tran\n" +
                        "\thelloWorld()\n" +
                        "\t\ta,b,c,d,e = doSomething()\n"
                , 25);
        assertEquals(1, t.Classes.size());
        var myClass = t.Classes.getFirst();
        assertEquals(1, myClass.methods.size());
        var myMethod = myClass.methods.getFirst();
        assertEquals(1, myMethod.statements.size());
        var firstStatement = myMethod.statements.getFirst();
        Assertions.assertInstanceOf(MethodCallStatementNode.class, firstStatement);
        assertEquals(5,((MethodCallStatementNode) firstStatement).returnValues.size());
    }
}
