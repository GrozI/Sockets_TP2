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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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

    private SocketChannel socketC = null;
    private int cmpt = 0;

    public void connect(String hostname, int port) throws IOException {
        //ouverture socket client serveur
        socketC = SocketChannel.open();
        socketC.connect(new InetSocketAddress(hostname, port));
        socketC.configureBlocking(false);
    }

    public void receiveMsg() throws IOException {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(20000);
            int bytesRead = socketC.read(buffer);
            if (bytesRead > 0) {
                String msg = new String(buffer.array(), "UTF-8");
                msg = msg.trim();
                System.out.println(msg);
                if (msg.startsWith("#list_u")) {
                    System.out.println(msg.substring(7));
                }
            }
        } catch (IOException e) {
            socketC.close();
        }

    }

    public void sendLastAck() throws CharacterCodingException, IOException {
        String msg = "#" + cmpt;
        CharBuffer c = CharBuffer.wrap(msg);
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer buf = encoder.encode(c);
        socketC.write(buf);

    }

    public void sendMessage(String msg) throws CharacterCodingException, IOException {
        CharBuffer c = CharBuffer.wrap(msg);
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer buf = encoder.encode(c);
        try{
        socketC.write(buf);
        }
        catch (Exception e){
            System.out.println("Le serveur a crashé.");
            socketC.close();
        }
    }

    public void disconnect() throws IOException {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        CharBuffer c = CharBuffer.wrap("#disconnect");
        ByteBuffer buf = ByteBuffer.allocate(200);
        buf = encoder.encode(c);
        socketC.write(buf);
        socketC.close();
    }
    public SocketChannel getSocket(){
        return socketC;
    }
}
