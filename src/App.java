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
            System.out.println("Result from running program:  " + result);
        } while (running);
        cmd.close();
    }
}
