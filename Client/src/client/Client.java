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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
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
        //int size = receiveNbClients();
        //System.out.println(size);
        //while ((socketC != null)&& (size>0)){
        //    receiveClientsConnectes();
        //    size--;
        //}
        if (socketC!=null){
            receiveClientsConnectes();
        }
        
        //System.out.println(clientsConnectes);
    }
    public void sendPseudo() throws IOException{
        CharBuffer c = CharBuffer.wrap(pseudo);
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer buf = encoder.encode(c);
        socketC.write(buf);
        
    }
    public int receiveNbClients() throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(20);
        int bytesRead = socketC.read(buffer);
        String sizename = new String(buffer.array(), "UTF-8");
        int size = Integer.parseInt(sizename.trim());

        return size;

    }
    public void receiveClientsConnectes() throws IOException{
        //le serveur envoie d'abord le nombre de clients connectés
        ByteBuffer buffer;
        buffer = ByteBuffer.allocate(124);
        int bytesRead = socketC.read(buffer);
        if (bytesRead >0){
            //System.out.println("bytesRead");
            String name = new String(buffer.array(), "UTF-8");
            //System.out.println(name);
            String[] namelist = name.split(" ");
            //System.out.println(Arrays.toString(name.split(" ")));
            for (int i=0;i<namelist.length-1;i++){
                clientsConnectes.add(namelist[i].trim());
            }
            //System.out.println(clientsConnectes);
        }
    }
    public void setPseudo(){
        Scanner scanner = new Scanner(System.in);
        String ps;
        System.out.println("Choisissez un pseudo (8 caractères max).");
        ps = scanner.next();
        if (clientsConnectes.contains(ps)){
            System.out.println("Pseudo déjà pris. Rééssayez.");
            setPseudo();
        }
        else{
            pseudo = ps;
            System.out.println("Ok");
        }
    }
    public String getPseudo(){
        return this.pseudo;
    }

    public void sendMessage(String msg) throws CharacterCodingException, IOException{
        CharBuffer c = CharBuffer.wrap(msg);
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer buf = encoder.encode(c);
        socketC.write(buf);
        
    }
    public void printList(){
        System.out.println(clientsConnectes);
    }
    public void disconnect() throws IOException{
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        CharBuffer c = CharBuffer.wrap("#disconnect");
        ByteBuffer buf = ByteBuffer.allocate(20);
        buf = encoder.encode(c);
        socketC.write(buf);
        socketC.socket().close();
    }
}
