
/**
 * This class exists purely to allow the online IDE at replit.com to run this game. The software on replit.com wants
 * there to be a public class named Main that has a public static method name main.
 * */

public class Main {
    public static void main(String[] args) {
        Application app = null;
        try {
            app = new Application();
            app.run();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (app != null) {
                app.close(); // ensure release of OpenGL resources
            }
        }
    }
}
