/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author gambicca
 */
public class Server {
    Map clientsConnectes = new HashMap();
    ServerSocketChannel socketSC;
    Map Channels = new HashMap();
    
    public void connect(int port) throws IOException{
        socketSC = ServerSocketChannel.open();
        socketSC.socket().bind(new InetSocketAddress(port));
        socketSC.configureBlocking(false);
        while(true){
            SocketChannel socketC = socketSC.accept();
            if (socketC != null){
                ByteBuffer pseudoBuf = ByteBuffer.allocate(48);
                socketC.read(pseudoBuf);
                System.out.println(pseudoBuf);
                String pseudo = new String(pseudoBuf.array(), "UTF-8");
                pseudo = pseudo.trim();
                System.out.println(pseudo);
                addClient(pseudo, socketC);
            }
        }
    }
    public void addClient(String pseudo, SocketChannel socket){
        clientsConnectes.put(pseudo, socket);
        System.out.println(clientsConnectes);
    }
    
    
}
