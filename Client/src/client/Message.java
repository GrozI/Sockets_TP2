/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author Agathe
 */
public class Message {
    private int cmpt;
    private String pseudo;
    private String data;

    public int getCmpt() {
        return cmpt;
    }

    public String getPseudo() {
        return pseudo;
    }

    public String getData() {
        return data;
    }

    public void setCmpt(int cmpt) {
        this.cmpt = cmpt;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Message(int cmpt, String pseudo, String data) {
        this.cmpt = cmpt;
        this.pseudo = pseudo;
        this.data = data;
    }
    
}
