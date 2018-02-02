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
    int cmpt;

    public void connect(String hostname, int port) throws IOException {
        //ouverture socket client serveur
        socketC = SocketChannel.open();
        socketC.connect(new InetSocketAddress(hostname, port));
        //envoi pseudo au serveur
        sendPseudo();
        if (socketC != null) {
            receiveClientsConnectes();
        }
    }

    public void sendPseudo() throws IOException {
        CharBuffer c = CharBuffer.wrap(pseudo);
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer buf = encoder.encode(c);
        socketC.write(buf);

    }

    public int receiveNbClients() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        int bytesRead = socketC.read(buffer);
        String sizename = new String(buffer.array(), "UTF-8");
        int size = Integer.parseInt(sizename.trim());

        return size;

    }

    public void receiveMsg() throws IOException {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(20000);
            int bytesRead = socketC.read(buffer);
            if (bytesRead !=0){
            String msg = new String(buffer.array(), "UTF-8");
            msg = msg.trim();
            if (msg.startsWith("#list")) {
                System.out.println(msg);
                
            } else if (msg.contains("#")){
                int compteur = Integer.parseInt(msg.substring(0, msg.indexOf("#")));

                if (cmpt == compteur - 1) {
                    cmpt = compteur;
                    //sendLastAck();
                    msg = msg.substring(msg.indexOf("#") + 1);
                    System.out.println(msg);
                } else {
                    sendLastAck();

                }
            }
            }
        } catch (IOException e) {
            System.out.println("");
        }

    }

    public void sendLastAck() throws CharacterCodingException, IOException {
        String msg = "#" + cmpt;
        CharBuffer c = CharBuffer.wrap(msg);
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer buf = encoder.encode(c);
        socketC.write(buf);

    }

    public void receiveClientsConnectes() throws IOException {
        //le serveur envoie d'abord le nombre de clients connectés
        ByteBuffer buffer;
        buffer = ByteBuffer.allocate(124);
        int bytesRead = socketC.read(buffer);
        if (bytesRead > 0) {
            String name = new String(buffer.array(), "UTF-8");
            String[] namelist = name.split(" ");
            for (int i = 0; i < namelist.length - 1; i++) {
                clientsConnectes.add(namelist[i].trim());
            }
        }
    }

    public void setPseudo() {
        Scanner scanner = new Scanner(System.in);
        String ps;
        System.out.println("Choisissez un pseudo (8 caractères max).");
        ps = scanner.next();
        if (clientsConnectes.contains(ps)) {
            System.out.println("Pseudo déjà pris. Rééssayez.");
            setPseudo();
        } else {
            pseudo = ps;
            System.out.println("Ok");
        }
    }

    public String getPseudo() {
        return this.pseudo;
    }

    public void sendMessage(String msg) throws CharacterCodingException, IOException {
        CharBuffer c = CharBuffer.wrap(msg);
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer buf = encoder.encode(c);
        socketC.write(buf);

    }

    public void printList() {
        System.out.println(clientsConnectes);
    }

    public void receiveList() throws CharacterCodingException, IOException {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        CharBuffer c = CharBuffer.wrap("#list");
        ByteBuffer buf = ByteBuffer.allocate(20);
        buf = encoder.encode(c);
        socketC.write(buf);
    }

    public void disconnect() throws IOException {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        CharBuffer c = CharBuffer.wrap("#disconnect");
        ByteBuffer buf = ByteBuffer.allocate(20);
        buf = encoder.encode(c);
        socketC.write(buf);
        socketC.socket().close();
    }
}
