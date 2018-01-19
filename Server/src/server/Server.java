/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author gambicca
 */
public class Server {

    Map<String, SocketChannel> clientsConnectes = new HashMap<String, SocketChannel>();
    ServerSocketChannel socketSC;
    Map Channels = new HashMap();

    public void initialize(int port) throws IOException {
        socketSC = ServerSocketChannel.open();
        socketSC.socket().bind(new InetSocketAddress(port));
        socketSC.configureBlocking(false);
        while (true) {
            SocketChannel socketC = socketSC.accept();
            if (socketC != null) {
                ByteBuffer pseudoBuf = ByteBuffer.allocate(48);
                socketC.read(pseudoBuf);
                //System.out.println(pseudoBuf);
                String pseudo = new String(pseudoBuf.array(), "UTF-8");
                pseudo = pseudo.trim();
                //System.out.println(pseudo);
                addClient(pseudo, socketC);
                sendList(pseudo);
                listen(socketC);
            }
        }
    }

    public void listen(SocketChannel socket) throws IOException {

        ByteBuffer msg = ByteBuffer.allocate(48);
        socket.read(msg);
        System.out.println("coucou");
        String message = new String(msg.array(), "UTF-8");
        if (message.startsWith("#disconnect")){
            removeClient(socket);
        }
        else {
            System.out.println(message);
        }

    }

    public void addClient(String pseudo, SocketChannel socket) {
        clientsConnectes.put(pseudo, socket);
        //System.out.println(clientsConnectes);
    }

    public void sendList(String pseudo) throws CharacterCodingException, IOException {
        if (clientsConnectes.containsKey(pseudo)) {
            SocketChannel socket = clientsConnectes.get(pseudo);
            System.out.println("Clients connectés : " + clientsConnectes.size());

//            int size = clientsConnectes.size();
//            String sizename = Integer.toString(size);
//            CharBuffer sizec = CharBuffer.wrap(sizename);
            CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
//            ByteBuffer s = encoder.encode(sizec);
            //socket.write(s);
            for (String name : clientsConnectes.keySet()) {
                CharBuffer c = CharBuffer.wrap(name + " ");
                ByteBuffer buf = ByteBuffer.allocate(124);
                buf = encoder.encode(c);
                socket.write(buf);
            }
            //socket.write(ByteBuffer.allocate(0));
        }
    }

    public void removeClient(SocketChannel socket) {
        if (clientsConnectes.containsValue(socket)){
            for (String pseudo : clientsConnectes.keySet()){
                if (clientsConnectes.get(pseudo).equals(socket)){
                    clientsConnectes.remove(pseudo);
                }
            }
        }
        System.out.println(clientsConnectes);
    }
}
