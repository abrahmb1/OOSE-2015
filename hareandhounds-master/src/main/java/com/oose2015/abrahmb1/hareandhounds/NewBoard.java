package com.oose2015.abrahmb1.hareandhounds;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by dell on 11-09-2015.
 */
public class NewBoard {

    private int itemId;
    private String gameId;
    private String player1Id;
    private String pieceType1;
    private String player2Id;
    private String pieceType2;
    private int hareX;
    private int hareY;
    private int hound1X;
    private int hound1Y;
    private int hound2X;
    private int hound2Y;
    private int hound3X;
    private int hound3Y;
    private String state;

    public NewBoard(NewBoard obj){
        this.itemId=obj.getItemId();
        this.gameId=obj.getGameId();
        this.hareX=obj.getHareX();
        this.hareY=obj.getHareY();
        this.player1Id=obj.getPlayer1Id();
        this.player2Id=obj.getPlayer2Id();
        this.hound1X=obj.getHound1X();
        this.hound1Y=obj.getHound1Y();
        this.hound2X=obj.getHound2X();
        this.hound2Y=obj.getHound2Y();
        this.hound3X=obj.getHound3X();
        this.hound3Y=obj.getHound3Y();
        this.pieceType1=obj.getPieceType1();
        this.pieceType2=obj.getPieceType2();
        this.state=obj.getState();
    }

    public NewBoard(int itemId,String gameId,String player1Id,String pieceType1,String player2Id,String pieceType2,
                    int hareX,int hareY,int hound1X,int hound1Y,int hound2X,int hound2Y, int hound3X,
                    int hound3Y, String state){
        this.itemId=itemId;
        this.gameId=gameId;
        this.hareX=hareX;
        this.hareY=hareY;
        this.player1Id=player1Id;
        this.player2Id=player2Id;
        this.hound1X=hound1X;
        this.hound1Y=hound1Y;
        this.hound2X=hound2X;
        this.hound2Y=hound2Y;
        this.hound3X=hound3X;
        this.hound3Y=hound3Y;
        this.pieceType1=pieceType1;
        this.pieceType2=pieceType2;
        this.state=state;
    }
    public int getItemId(){return itemId;}
    public String getGameId(){return gameId;}
    public String getPlayer1Id(){return player1Id;}
    public String getPlayer2Id(){return player2Id;}
    public String getPieceType1(){return pieceType1;}
    public String getPieceType2(){return pieceType2;}
    public int getHareX(){return hareX;}
    public int getHareY(){return hareY;}
    /*public int getHoundX(int i){
        if(i==1)
            return hound1X;
        if(i==2)
            return hound2X;
        else
            return hound3X;
    }
    public int getHoundY(int i){
        if(i==1)
            return hound1Y;
        if(i==2)
            return hound2Y;
        return hound3Y;
    }*/
    public int getHound1X(){return hound1X;}
    public int getHound1Y(){return hound1Y;}
    public int getHound2X(){return hound2X;}
    public int getHound2Y(){return hound2Y;}
    public int getHound3X(){return hound3X;}
    public int getHound3Y(){return hound3Y;}
    public String getState(){return state;}
    public void setHareX(int x){hareX=x;}
    public void setHareY(int y){hareY=y;}
    /*public void setHoundX(int i,int x){
        if(i==1)
            hound1X=x;
        if(i==2)
            hound2X=x;
        if(i==3)
            hound3X=x;
    }
    public void setHoundY(int i,int y){
        if(i==1)
            hound1Y=y;
        if(i==2)
            hound2Y=y;
        if(i==3)
            hound3Y=y;
    }*/
    public void setHound1X(int x){hound1X=x;}
    public void setHound1Y(int y){hound1Y=y;}
    public void setHound2X(int x){hound2X=x;}
    public void setHound2Y(int y){hound2Y=y;}
    public void setHound3X(int x){hound3X=x;}
    public void setHound3Y(int y){hound3Y=y;}
    public void setState(String state){this.state=state;}
    public void setItemId(int x){itemId=x;}

    @Override
    public int hashCode() {
        int result = 31*hareX + 29*hareY;
        result = result + 31*hound1X+29*hound1Y;
        result = result + 31*hound2X+29*hound2Y;
        result = result + 31*hound3X+29*hound3Y;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewBoard board = (NewBoard) o;
        if ((hareX != board.getHareX())||(hareY != board.getHareY())) return false;
        Set<String> set1 = new HashSet<String>();
        String str1 = Integer.toString(board.getHound1X()) + Integer.toString(board.getHound1Y());
        String str2 = Integer.toString(board.getHound2X()) + Integer.toString(board.getHound2Y());
        String str3 = Integer.toString(board.getHound3X()) + Integer.toString(board.getHound3Y());
        set1.add(str1);
        set1.add(str2);
        set1.add(str3);
        String s1 = Integer.toString(hound1X) + Integer.toString(hound1Y);
        String s2 = Integer.toString(hound2X) + Integer.toString(hound2Y);
        String s3 = Integer.toString(hound3X) + Integer.toString(hound3Y);
        if((!set1.contains(s1))||(!set1.contains(s2))||(!set1.contains(s3)))
            return false;
        return true;
    }
}
