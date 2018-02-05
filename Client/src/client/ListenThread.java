/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Agathe
 */
public class ListenThread implements Runnable {

    private Client client;

    public ListenThread(Client client) {
        this.client = client;
    }
    public void run(){
        try {
            //System.out.println("MyThread running");
            while (true){
            Thread.sleep(100);
            client.receiveMsg();
        }
        } catch (IOException ex) {
            Logger.getLogger(ListenThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
        }
    }
}
