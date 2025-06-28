import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Lexer {

    private TextManager textManager;
    private HashMap <String, Token.TokenTypes> tokenMaps = new HashMap<String, Token.TokenTypes>();
    int lineNumber = 1;
    int characterPosition = 0;
    int globalindent;
    int x;

    public void addValuesinHashMaps () {
        tokenMaps.put("=", Token.TokenTypes.ASSIGN);
        tokenMaps.put(",", Token.TokenTypes.COMMA);
        tokenMaps.put("-", Token.TokenTypes.MINUS);
        tokenMaps.put("+", Token.TokenTypes.PLUS);
        tokenMaps.put("(", Token.TokenTypes.LPAREN);
        tokenMaps.put(")", Token.TokenTypes.RPAREN);
        tokenMaps.put(":", Token.TokenTypes.COLON);
//        tokenMaps.put(".", Token.TokenTypes.DOT);
        tokenMaps.put("*", Token.TokenTypes.TIMES);
        tokenMaps.put("/", Token.TokenTypes.DIVIDE);
        tokenMaps.put("%", Token.TokenTypes.MODULO);
        tokenMaps.put("==", Token.TokenTypes.EQUAL);
        tokenMaps.put("!=", Token.TokenTypes.NOTEQUAL);
        tokenMaps.put("<", Token.TokenTypes.LESSTHAN);
        tokenMaps.put("<=", Token.TokenTypes.LESSTHANEQUAL);
        tokenMaps.put(">", Token.TokenTypes.GREATERTHAN);
        tokenMaps.put(">=", Token.TokenTypes.GREATERTHANEQUAL);
        tokenMaps.put("accessor", Token.TokenTypes.ACCESSOR);
        tokenMaps.put("mutator", Token.TokenTypes.MUTATOR);
        tokenMaps.put("implements", Token.TokenTypes.IMPLEMENTS);
        tokenMaps.put("class", Token.TokenTypes.CLASS);
        tokenMaps.put("interface", Token.TokenTypes.INTERFACE);
        tokenMaps.put("loop", Token.TokenTypes.LOOP);
        tokenMaps.put("if", Token.TokenTypes.IF);
        tokenMaps.put("else", Token.TokenTypes.ELSE);
        tokenMaps.put("indent", Token.TokenTypes.INDENT);
        tokenMaps.put("dedent", Token.TokenTypes.DEDENT);
//        tokenMaps.put("\n", Token.TokenTypes.NEWLINE);
        tokenMaps.put("", Token.TokenTypes.QUOTEDSTRING);
        tokenMaps.put("true", Token.TokenTypes.TRUE);
        tokenMaps.put("false", Token.TokenTypes.FALSE);
        tokenMaps.put("new", Token.TokenTypes.NEW);
        tokenMaps.put("private", Token.TokenTypes.PRIVATE);
        tokenMaps.put("shared", Token.TokenTypes.SHARED);
        tokenMaps.put("construct", Token.TokenTypes.CONSTRUCT);
        tokenMaps.put("&&", Token.TokenTypes.AND);
        tokenMaps.put("||", Token.TokenTypes.OR);
        tokenMaps.put("!", Token.TokenTypes.NOT);
    }

    public Lexer(String input) {
        textManager = new TextManager(input);
        addValuesinHashMaps();
    }

    public List<Token> Lex() throws Exception {
        var retVal = new LinkedList<Token>();
        while (textManager.isAtEnd()) {
            char c = textManager.getCharacter();
            characterPosition++;
            if (Character.isLetter(c)) {
                Token newToken = parseWord(c);
                retVal.add(newToken);
            } else if (Character.isDigit(c)) {
                Token newToken = parseNumber(c);
                retVal.add(newToken);
            } else if (c == '\"') {
                Token newToken = QuotedString(c);
                retVal.add(newToken);
            } else if (c == '\'') {
                Token newToken = SingleQuote(c);
                retVal.add(newToken);
            } else if (c == '\n') {

                if (textManager.peekCharacter(4) == '\n') {
                    while (textManager.peekCharacter() == ' ') {
                        characterPosition++;
                        textManager.getCharacter();
                    }
                }

                lineNumber++;
                while (textManager.isAtEnd() && textManager.peekCharacter(0) == '\n') {
                    textManager.getCharacter();
                    lineNumber++;
                    characterPosition =  0;
                }
                characterPosition =  0;
                retVal.add(new Token(Token.TokenTypes.NEWLINE, lineNumber, characterPosition));
                if (textManager.isAtEnd()) {
                    char peekforindent = textManager.peekCharacter();
                    retVal.addAll(Indentation(peekforindent));

                }
            } else if (tokenMaps.containsKey(Character.toString(c)) && c != '-') {
                Token newToken = parsePunctuation(Character.toString(c));
                retVal.add(newToken);
            } else if (c == '|') {
                char nextChar = textManager.peekCharacter();
                if (nextChar != '|') {
                    throw new SyntaxErrorException("OR operator doesn't have the correct amount of vertical bars." , lineNumber, characterPosition);
                }
                Token newToken = parsePunctuation(Character.toString(c));
                retVal.add(newToken);
                textManager.getCharacter();
                characterPosition++;
            } else if (c == '&') {
                char nextChar = textManager.peekCharacter();
                if (nextChar != '&') {
                    throw new SyntaxErrorException("AND operator doesn't have the correct amount of ampersand." , lineNumber, characterPosition);
                }
                Token newToken = parsePunctuation(Character.toString(c));
                retVal.add(newToken);
                textManager.getCharacter();
                characterPosition++;
            } else if (c == '.') {
                if (textManager.isAtEnd()) {
                    char nextchar = textManager.peekCharacter();
                        if (Character.isDigit(nextchar)) {
                            Token newToken = parseNumber(c);
                            retVal.add(newToken);
                        } else {
                            retVal.add(new Token(Token.TokenTypes.DOT, lineNumber, characterPosition));
                        }
                } else {
                    retVal.add(new Token(Token.TokenTypes.DOT, lineNumber, characterPosition));
                }
            } else if (c == '{') {
//                boolean curlyBracesChecker = parseComments();
//                if (curlyBracesChecker) {
//                    throw new SyntaxErrorException("Comment not closed!!", lineNumber, characterPosition);
//                }
                parseComments();
            } else if (c == '}') {
                throw new SyntaxErrorException("The comment is not closed by a '}'." , lineNumber, characterPosition);
            } else if (c == '-') {
                retVal.add(new Token(Token.TokenTypes.MINUS, lineNumber, characterPosition));
            }
        }
        while (globalindent > 0) {

            retVal.add(new Token(Token.TokenTypes.DEDENT, lineNumber, characterPosition));
            globalindent--;
        }
        return retVal;
    }

    public List<Token> Indentation(char c) throws Exception {
        int localindent = 0;
        int localspace = 0;
        int localLineSpaceCount = 0;
        var retVal1 = new LinkedList<Token>();
        while ((c == ' ' || c == '\t') && textManager.isAtEnd()) {
            if (textManager.isAtEnd()) {
                c = textManager.peekCharacter();
            }
            if (c == ' ' || c == '\t') {    //(c == ' ' || c == '\t')

                if (textManager.isAtEnd()) {
                    textManager.getCharacter();
                    characterPosition++;
                }

                if (c == '\t') {
                    localspace = localspace + 4;
                    localLineSpaceCount = localLineSpaceCount + 4;
                } else {
                    localspace++;
                    localLineSpaceCount++;
                }

                if (localspace == 4){
                    localindent++;
                    localspace = 0;
//                    globalindent++;

                }
            }
        }
        int a = 0;
        int indentDifference = localindent - globalindent;

        if (indentDifference > 0) {
            a++;
            while (indentDifference > 0) {    //globalindent
                retVal1.add(new Token(Token.TokenTypes.INDENT, lineNumber, characterPosition));
//            x++;
                globalindent++;
                indentDifference--;
            }
        } else if (localindent < globalindent) {
            int difference = Math.abs(localindent - globalindent);
            while (difference > 0) {
                retVal1.add(new Token(Token.TokenTypes.DEDENT, lineNumber, characterPosition));
                globalindent--;
                difference--;
            }
        }

        if (localLineSpaceCount % 4 != 0) {
            throw new SyntaxErrorException("Indent doesn't have the correct amount of spaces.", lineNumber, characterPosition);
        }
        return retVal1;
    }

    public Token SingleQuote(char a) throws Exception {
        String CurrentWord = "";
        char currentChar = textManager.getCharacter();
        characterPosition++;
        CurrentWord = CurrentWord + currentChar;
        char nextchar = textManager.getCharacter();
        characterPosition++;
//        char p = textManager.peekCharacter(1);
        if (nextchar != '\'') {
            SyntaxErrorException exc = new SyntaxErrorException("This character has more than 1 chars." , lineNumber, characterPosition);
            System.out.println(exc);
            throw exc;
//            throw new SyntaxErrorException("This character has more than 1 chars." , lineNumber, characterPosition);
        }
        return new Token(Token.TokenTypes.QUOTEDCHARACTER, lineNumber, characterPosition, CurrentWord);
    }

    public Token QuotedString(char a) throws Exception {
        String CurrentWord = "";
        while (textManager.isAtEnd()) {
            char currentChar = textManager.getCharacter();
            characterPosition++;
            if (currentChar == '\n') {
                lineNumber++;
                characterPosition = 0;
                //currentChar = textManager.getCharacter();
                //characterPosition++;
            }
            if (currentChar == '"'){
                return new Token(Token.TokenTypes.QUOTEDSTRING, lineNumber, characterPosition, CurrentWord);
            }
            CurrentWord = CurrentWord + currentChar;
        }
        return new Token(Token.TokenTypes.QUOTEDSTRING, lineNumber, characterPosition);
    }

    public void parseComments() throws Exception {
        char next = textManager.peekCharacter();
        while (next != '}') {
            next = textManager.getCharacter();
            if (next == '\n') {
                characterPosition = 0;
                lineNumber++;
            } else {
                characterPosition++;
            }
//            characterPosition++;
        }
    }

    public Token parseWord(char a) throws Exception {
        String CurrentWord = "";
        CurrentWord = CurrentWord + a;
        char currentChar = a;
        while (Character.isLetter(currentChar) || a != '"') {
            if (!textManager.isAtEnd()) {
                return new Token(Token.TokenTypes.WORD, lineNumber, characterPosition, CurrentWord);
            }
            char nextChar = textManager.peekCharacter();
            if (Character.isLetter(nextChar) || nextChar == '"') {
                if (nextChar == '"') {
                    return new Token(Token.TokenTypes.QUOTEDSTRING, lineNumber, characterPosition, CurrentWord);
                }
                currentChar = textManager.getCharacter();
                characterPosition++;
                CurrentWord = CurrentWord + currentChar;
                if (tokenMaps.containsKey(CurrentWord)) {
                    Token newToken = parsePunctuation(CurrentWord);
                    return newToken;
                }
            } else {
                return new Token(Token.TokenTypes.WORD, lineNumber, characterPosition, CurrentWord);
            }
        }
        return new Token(Token.TokenTypes.WORD, lineNumber, characterPosition, CurrentWord);
    }


    public Token parseNumber(char a) throws Exception {
        boolean morethanoneDot = false;
        String CurrentWord = "";
        CurrentWord = CurrentWord + a;
        char currentChar = a;
        while (Character.isDigit(currentChar) || currentChar == '.' ) {
            if (!textManager.isAtEnd()) {
                return new Token(Token.TokenTypes.NUMBER, lineNumber, characterPosition, CurrentWord);
            }
            char nextChar = textManager.peekCharacter();
            if (Character.isDigit(nextChar)) {
                currentChar = textManager.getCharacter();
                characterPosition++;
                CurrentWord = CurrentWord + currentChar;
            } else if (nextChar == '.' && !morethanoneDot) {
                morethanoneDot = true;
                char next = textManager.peekCharacter(1);
                if (Character.isDigit(next)) {
                    currentChar = textManager.getCharacter();
                    characterPosition++;
                    CurrentWord = CurrentWord + currentChar;
                }
            } else if(nextChar == '.') {
                return new Token(Token.TokenTypes.NUMBER, lineNumber, characterPosition, CurrentWord);
            } else {
            return new Token(Token.TokenTypes.NUMBER, lineNumber, characterPosition, CurrentWord);
            }
        }
        return new Token(Token.TokenTypes.NUMBER, lineNumber, characterPosition, CurrentWord);
    }

    public Token parsePunctuation(String a) throws Exception {
        String CurrentWord = "";
        CurrentWord = CurrentWord + a;
        if (a == "|") {
            if (textManager.isAtEnd()) {
                char nextChar = textManager.peekCharacter();
                CurrentWord = CurrentWord + nextChar;
                if (CurrentWord == "||") {
                    return new Token(Token.TokenTypes.OR, lineNumber, characterPosition, CurrentWord);
                }
            }
        }
        if (a == "&") {
            if (textManager.isAtEnd()) {
                char nextChar = textManager.peekCharacter();
                CurrentWord = CurrentWord + nextChar;
                if (CurrentWord == "&&") {
                    return new Token(Token.TokenTypes.OR, lineNumber, characterPosition, CurrentWord);
                }
            }
        }
        try {
            char next = textManager.peekCharacter();
        } catch (Exception e) {
            if (tokenMaps.containsKey(CurrentWord)) {
                return new Token(tokenMaps.get(CurrentWord), lineNumber, characterPosition, CurrentWord);
            }
            return new Token(Token.TokenTypes.WORD, lineNumber, characterPosition, CurrentWord);
        }
        char next = textManager.peekCharacter(0);
        if (textManager.peekCharacter() == '=') {
            next = textManager.getCharacter();
            characterPosition++;
        }
        String NextCurrentWord = "";
        NextCurrentWord = NextCurrentWord + CurrentWord;
        NextCurrentWord = NextCurrentWord + next;
        if (tokenMaps.containsKey(NextCurrentWord)) {
            return new Token(tokenMaps.get(NextCurrentWord), lineNumber, characterPosition, CurrentWord);
        }
        if (tokenMaps.containsKey(CurrentWord)) {
            return new Token(tokenMaps.get(CurrentWord), lineNumber, characterPosition, CurrentWord);
        }
        return new Token(Token.TokenTypes.WORD, lineNumber, characterPosition, CurrentWord);
    }

}   //end of lexer class
