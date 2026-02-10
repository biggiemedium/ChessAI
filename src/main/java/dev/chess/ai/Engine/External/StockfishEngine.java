package dev.chess.ai.Engine.External;

import java.io.*;
import java.util.List;

/**
 * Interface to communicate with Stockfish chess engine via UCI protocol
 */
public class StockfishEngine {

    private Process stockfishProcess;
    private BufferedReader reader;
    private BufferedWriter writer;
    private boolean isReady;

    private static final String STOCKFISH_PATH = "PATH";

    public StockfishEngine() {
        this.isReady = false;
    }

    /**
     * Start the Stockfish engine process
     */
    public void start() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(STOCKFISH_PATH);
        pb.redirectErrorStream(true);

        stockfishProcess = pb.start();

        reader = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(stockfishProcess.getOutputStream()));

        // Initialize UCI
        sendCommand("uci");
        waitForResponse("uciok");

        // Set up new game
        sendCommand("isready");
        waitForResponse("readyok");

        isReady = true;
        System.out.println("Stockfish engine started successfully");
    }

    /**
     * Stop the Stockfish engine
     */
    public void stop() {
        try {
            if (writer != null) {
                sendCommand("quit");
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (stockfishProcess != null) {
                stockfishProcess.destroy();
            }
            isReady = false;
            System.out.println("Stockfish engine stopped");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get best move with depth-based search
     *
     * @param moveHistory UCI move history
     * @param depth search depth (typically 10-20 for strong play)
     * @return best move in UCI notation
     */
    public String getBestMoveByDepth(List<String> moveHistory, int depth) throws IOException {
        if (!isReady) {
            throw new IllegalStateException("Engine not started. Call start() first.");
        }

        // Set position
        StringBuilder positionCmd = new StringBuilder("position startpos");
        if (!moveHistory.isEmpty()) {
            positionCmd.append(" moves");
            for (String move : moveHistory) {
                positionCmd.append(" ").append(move);
            }
        }
        sendCommand(positionCmd.toString());

        // Request best move with depth
        sendCommand("go depth " + depth);

        // Read response until we get bestmove
        String bestMove = null;
        String line;
        while ((line = readLine()) != null) {
            System.out.println("Stockfish: " + line);

            if (line.startsWith("bestmove")) {
                String[] parts = line.split(" ");
                if (parts.length > 1) {
                    bestMove = parts[1];
                }
                break;
            }
        }

        return bestMove;
    }

    /**
     * Set Stockfish options (e.g., skill level, threads, hash size)
     */
    public void setOption(String name, String value) throws IOException {
        sendCommand("setoption name " + name + " value " + value);
    }

    /**
     * Start a new game
     */
    public void newGame() throws IOException {
        sendCommand("ucinewgame");
        sendCommand("isready");
        waitForResponse("readyok");
    }

    /**
     * Send a command to Stockfish
     */
    private void sendCommand(String command) throws IOException {
        writer.write(command + "\n");
        writer.flush();
        System.out.println("Sent to Stockfish: " + command);
    }

    /**
     * Read a line from Stockfish
     */
    private String readLine() throws IOException {
        return reader.readLine();
    }

    /**
     * Wait for a specific response from Stockfish
     */
    private void waitForResponse(String expectedResponse) throws IOException {
        String line;
        while ((line = readLine()) != null) {
            System.out.println("Stockfish: " + line);
            if (line.contains(expectedResponse)) {
                return;
            }
        }
    }

    public boolean isReady() {
        return isReady;
    }
}