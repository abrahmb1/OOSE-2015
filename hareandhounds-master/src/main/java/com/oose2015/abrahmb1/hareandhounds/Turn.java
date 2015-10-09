package com.oose2015.abrahmb1.hareandhounds;

/**
 * Created by dell on 06-09-2015.
 */
public class Turn {
    private String playerId;
    private int fromX;
    private int fromY;
    private int toX;
    private int toY;
    public Turn(String playerId,int fromX,int fromY,int toX,int toY){
        this.playerId=playerId;
        this.fromX=fromX;
        this.fromY=fromY;
        this.toX=toX;
        this.toY=toY;
    }
    public String getPlayerId(){ return playerId; }
    public int getFromX(){ return fromX; }
    public int getFromY(){ return fromY; }
    public int getToX(){ return toX; }
    public int getToY(){ return toY; }

    @Override
    public String toString() {
        return "Turn{" +
                "playerId='" + playerId + '\'' +
                ", fromX=" + fromX +
                ", fromY=" + fromY +
                ", toX=" + toX +
                ", toY=" + toY +
                '}';
    }
}
