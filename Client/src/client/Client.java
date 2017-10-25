/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.*;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author gambicca
 */
public class Client {
    String pseudo;
    ArrayList<String> clientsConnectes = new ArrayList<String>();
    SocketChannel socketC;
    public void connect(String hostname, int port) throws IOException{
        //ouverture socket client serveur
        socketC = SocketChannel.open();
        socketC.connect(new InetSocketAddress(hostname, port));
        //envoi pseudo au serveur
        sendPseudo();
        //reception listeclient[pseudo]
    }
    public void sendPseudo() throws IOException{
        CharBuffer c = CharBuffer.wrap(pseudo);
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer buf = encoder.encode(c);
        socketC.write(buf);
        
    }
    public void receiveClientsConnectes() throws IOException{
        //le serveur envoie d'abord le nombre de clients connectés
        ByteBuffer buffer = ByteBuffer.allocate(48);
        int bytesRead = socketC.read(buffer);
        
        //puis chaque pseudo
        
    }
    public void getPseudo(){
        Scanner scanner = new Scanner(System.in);
        String ps;
        System.out.println("Choisissez un pseudo (8 caractères max).");
        ps = scanner.next();
        if (clientsConnectes.contains(ps)){
            System.out.println("Pseudo déjà pris. Rééssayez.");
            getPseudo();
        }
        else{
            pseudo = ps;
            System.out.println("Ok");
        }
    }
    

    public void sendMessage(){
        
        
    }
    public void disconnect() throws IOException{
        socketC.socket().close();
    }
}
