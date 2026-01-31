package dev.chess.cheat.Network.Model;

public class GameState {
    private String type;
    private String moves;
    private String status;
    private int wtime;
    private int btime;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMoves() {
        return moves;
    }

    public void setMoves(String moves) {
        this.moves = moves;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getWtime() {
        return wtime;
    }

    public void setWtime(int wtime) {
        this.wtime = wtime;
    }

    public int getBtime() {
        return btime;
    }

    public void setBtime(int btime) {
        this.btime = btime;
    }
}