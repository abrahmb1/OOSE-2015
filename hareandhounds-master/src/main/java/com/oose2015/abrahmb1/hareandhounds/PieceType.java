package com.oose2015.abrahmb1.hareandhounds;

/**
 * Created by dell on 07-09-2015.
 */
public class PieceType {
    private String pieceType;
    public PieceType(String pieceType){
    this.pieceType = pieceType;
    }
    public String getPieceType(){ return pieceType; }

    @Override
    public String toString() {
        return "PieceType{" +
                "pieceType='" + pieceType + '\'' +
                '}';
    }
}
