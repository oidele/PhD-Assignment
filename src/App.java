import java.util.Scanner;


public class App {
    public static void main(String[] args) throws Exception {
        boolean running = true;
        Scanner cmd = new Scanner(System.in);
        do {
            System.out.print("Java-xml>");
            String text = cmd.next();
            Interpreter interpreter = new Interpreter(text);
            Integer result = interpreter.parseAndExecute();
            if (result != null) {
                System.out.println(result);
            } else {
                System.out.println("Syntax error in code");
            }
            
        } while (running);
        cmd.close();
    }
}
