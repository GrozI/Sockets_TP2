/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author gambicca
 */
public class Server {

    private Selector selector = null;
    private Map<String, SocketChannel> clients = new HashMap<String, SocketChannel>();

    private ServerSocketChannel socketSC = null;
    private Map Channels = new HashMap();
    private int cmpt = 1;
    private List<Message> messageList = new ArrayList<Message>();
    private static Map<SocketChannel, String> users = new HashMap<SocketChannel, String>();

    public void initialize(int port) throws IOException, InterruptedException {
        selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 8080));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(256);

        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {

                SelectionKey key = iter.next();

                handleKey(socketSC, key);
                iter.remove();
            }
        }
    }


    public void handleKey(ServerSocketChannel ssc, SelectionKey key) throws IOException, InterruptedException {
        if (key.isAcceptable()) {
            SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ);

            //key.interestOps(SelectionKey.OP_ACCEPT);
            String welcome = "Bonjour ! Veuillez choisir un pseudo.";
            CharBuffer c = CharBuffer.wrap(welcome);
            CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
            ByteBuffer buf = encoder.encode(c);
            sc.write(buf);

        } else if (key.isReadable()) {
            SocketChannel sc = (SocketChannel) key.channel();
            key.interestOps(SelectionKey.OP_READ);
            listen(sc);
        }
    }

    public void listen(SocketChannel socket) throws IOException {

        ByteBuffer msg = ByteBuffer.allocate(2000);
        socket.read(msg);
        String message = new String(msg.array(), "UTF-8");
        message = message.trim();
        if (users.containsKey(socket)) {
            if (message.startsWith("#disconnect")) {
                removeUser(socket);
                socket.close();
            }
            if (message.startsWith("#list")) {
                sendList(socket);
            } else {
                Message m = handleMessage(socket, message);
                broadcastMessage(m);
            }
        } else {
            if (users.containsValue(message)) {
                String alert = "Le pseudo est déjà pris. Merci d'en choisir un autre.";
                CharBuffer c = CharBuffer.wrap(alert);
                CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
                ByteBuffer buf = encoder.encode(c);
                socket.write(buf);
            } else {
                users.put(socket, message);
                String welcome = "Bienvenue ! Ecrivez un message. Pour afficher la liste"
                        + " des commandes, tapez #.";
                CharBuffer c = CharBuffer.wrap(welcome);
                CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
                ByteBuffer buf = encoder.encode(c);
                socket.write(buf);
            }
        }

    }

    public Message handleMessage(SocketChannel socket, String message) {
        String pseudo = users.get(socket);
        Message m = new Message(cmpt, pseudo, message);
        return m;
    }

    public void broadcastMessage(Message m) throws CharacterCodingException, IOException {
        String msg = m.getCmpt() + "#" + m.getPseudo() + " : " + m.getData();
        msg = m.getPseudo() + " : " + m.getData();
        CharBuffer c = CharBuffer.wrap(msg);
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer buf = encoder.encode(c);
        for (SelectionKey key : selector.keys()) {
            if (!(key.channel() instanceof SocketChannel)) {
                continue;
            }
            SocketChannel socket = (SocketChannel) key.channel();
            if (!users.containsKey(socket)) {
                continue;
            }
            socket.write(buf);
            buf.rewind();
        }
        cmpt++;
    }

    public void broadcastServerMessage(String m) throws CharacterCodingException, IOException {
        CharBuffer c = CharBuffer.wrap(m);
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer buf = encoder.encode(c);
        for (SelectionKey key : selector.keys()) {
            if (!(key.channel() instanceof SocketChannel)) {
                continue;
            }
            SocketChannel socket = (SocketChannel) key.channel();
            if (!users.containsKey(socket)) {
                continue;
            }
            socket.write(buf);
            buf.rewind();
        }
    }

    public void sendList(SocketChannel socket) throws CharacterCodingException, IOException {

        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        CharBuffer c = null;
        String nameList = "#list_u";
        nameList = "";
        for (String name : users.values()) {
            nameList = nameList + name + " ";
        }
        c = CharBuffer.wrap(nameList);
        ByteBuffer buf = ByteBuffer.allocate(2000);
        buf = encoder.encode(c);
        socket.write(buf);
    }


    public void removeUser(SocketChannel socket) throws IOException {
        String username = users.get(socket);
        users.remove(socket);
        broadcastServerMessage(username + " s'est déconnecté.");
    }

    
}
