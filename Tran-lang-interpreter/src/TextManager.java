public class TextManager {

    private String text;
    private int position;

    public TextManager(String input) {
        text = input;
        position = 0;
    }

    public boolean isAtEnd() {
        if (position >= text.length()) {
            return false;
        }
        return true;
    }

    public char peekCharacter() {
        return text.charAt(position);
    }

    public char peekCharacter(int distance) {
        if (position + distance <= text.length()) {
            return text.charAt(position + distance); //(text.length() - 1)
        } else {
            return '~';
        }
    }

    public String returnpeekCharacterString(int distance) {
        String a = "";
        if (position + distance <= text.length()) {
            a =  a + text.charAt(position + distance); //(text.length() - 1)
            return a;
        } else {
            return null;
        }
    }

    public char getCharacter() {
        return text.charAt(position++);
    }



}
