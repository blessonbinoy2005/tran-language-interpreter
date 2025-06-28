import java.util.List;
import java.util.Optional;

public class TokenManager {

    private List<Token> tokens; //
    private int ListIndex = 0;

    public TokenManager(List<Token> tokens) {
        this.tokens = tokens; //
    }

    public boolean done() {
        if (ListIndex >= tokens.size()) {
            return true;  //fal
        }
        return false;  //tru
    }

    //TODO check if listIndex < tokens.size using the done() method.
    public Optional<Token> matchAndRemove(Token.TokenTypes t) {
        if (tokens.get(ListIndex).getType() == t) {
            Optional<Token> opt = Optional.ofNullable(tokens.get(ListIndex));
            ListIndex++;
            return opt;
        }
        return Optional.empty();
    }

    public Optional<Token> peek(int i) {
        if (ListIndex < tokens.size()) {
            Optional<Token> opt = Optional.ofNullable(tokens.get(ListIndex + i));
            return opt;
        }
        return Optional.empty();
    }

    public boolean nextTwoTokensMatch(Token.TokenTypes first, Token.TokenTypes second) {
        if (tokens.get(ListIndex).getType() == first && tokens.get(ListIndex + 1).getType() == second) {
            return true;
        }
//        if (tokens.get(ListIndex).getType() == tokens.get(ListIndex++).getType()) {
//            return true;
//        }
        return false;
    }

    public int getCurrentLine() {
        return tokens.get(ListIndex).getLineNumber();
    }

    public int getCurrentColumn() {
        return tokens.get(ListIndex).getColumnNumber();
    }


}
