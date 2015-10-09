package com.oose2015.abrahmb1.hareandhounds;

/**
 * Created by dell on 05-09-2015.
 */
public class Player {
    private String gameId;
    private String playerId;
    private String pieceType;
    public Player(String gameId,String playerId,String pieceType){
        this.gameId=gameId;
        this.playerId=playerId;
        this.pieceType=pieceType;
    }
    public Player(){
        gameId="";
        playerId="";
        pieceType="";
    }
    public String getGameId(){ return gameId; }
    public String getPlayerId(){ return playerId; }
    public String getPieceType(){ return pieceType; }
}
