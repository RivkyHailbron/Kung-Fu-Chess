package chess.server;

/**
 * נקודת הכניסה הראשית לשרת השח
 */
public class ChessServerMain {
    
    public static void main(String[] args) {
        ServerConfig config = ServerConfig.getInstance();
        config.printConfiguration();
        
        int port = config.getPort();
        
        // קבלת פורט מפרמטרי הקמה אם קיימים
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0]);
                System.err.println("Using default port: " + config.getPort());
                port = config.getPort();
            }
        }
        
        System.out.println("=== KFChess Server ===");
        System.out.println("Starting server on port: " + port);
        
        try {
            ChessServer server = new ChessServer();
            server.start(port);
            
            System.out.println("Server started successfully!");
            System.out.println("Waiting for players to connect...");
            System.out.println("Press Ctrl+C to stop the server");
            
            // Keep the server running
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
