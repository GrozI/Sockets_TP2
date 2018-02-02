/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Agathe
 */
public class ListenThread extends Thread {
    private Client client;
    
    public ListenThread(Client client){
        this.client = client;
    }
    public void run(){
        try {
            //System.out.println("MyThread running");
            while (true){
            Thread.sleep(1000);
            client.receiveMsg();
        }
        } catch (IOException ex) {
            Logger.getLogger(ListenThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ListenThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
