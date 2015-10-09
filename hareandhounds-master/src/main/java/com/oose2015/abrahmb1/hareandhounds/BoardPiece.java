package com.oose2015.abrahmb1.hareandhounds;

/**
 * Created by dell on 05-09-2015.
 */
public class BoardPiece {
    private String pieceType;
    private int x;
    private int y;
    public BoardPiece(String pieceType,int x, int y){
        this.pieceType=pieceType;
        this.x=x;
        this.y=y;
    }
    public String getPieceType(){ return pieceType; }
    public int getX(){ return x; }
    public int getY(){ return y; }
    public void setX(int x){ this.x = x; }
    public void setY(int y){ this.y = y; }

    @Override
    public String toString() {
        return "BoardPiece{" +
                "pieceType='" + pieceType + '\'' +
                ", x='" + x + '\'' +
                ", y=" + y +
                '}';
    }
}
