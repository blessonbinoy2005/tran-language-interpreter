import AST.TranNode;
import Interpreter.Interpreter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("args has " + args.length + " arguments");
        for (var a : args)
            System.out.println("    " + a);

        String filePath = "./src/myFile.tran"; // File path
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n"); // Append each line with a newline
            }
        } catch (IOException e) {
            System.err.println("An error occurred while reading the file: " + e.getMessage());
        }

        System.out.println("File Content:\n" + content);

        var l  = new Lexer(content.toString());
        try {
            var tokens = l.Lex();
            var tran = new TranNode();
            var p = new Parser(tran,tokens);
            p.Tran();
            System.out.println(tran.toString());
            var i = new Interpreter(tran);
            i.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }



    }
}
