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
    private ServerSocketChannel socketSC = null;
    private int cmpt = 1;
    private List<Message> messageList = new ArrayList<Message>();
    //Map SocketChannel de l'utilisateur <-> pseudo
    private static Map<SocketChannel, String> users = new HashMap<SocketChannel, String>();
    //Map nom de la chatroom  <-> SocketChannel de l'admin
    private Map<String, SocketChannel> chatrooms = new HashMap<String, SocketChannel>();
    //Map SocketChannel utilisateurs connectes <-> String nom chatroom
    private Map<SocketChannel, String> usersInChatroom = new HashMap<SocketChannel, String>();

    public void initialize(int port) throws IOException, InterruptedException {
        selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 8080));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

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
            if (message.startsWith("#")) {
                handleCmd(socket, message);
            } else {
                Message m = handleMessage(socket, message);
                broadcastMessage(socket, m);
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
                String welcome = "Bienvenue ! Pour afficher la liste"
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

    public void handleCmd(SocketChannel socket, String message) throws IOException {
        if (message.length() == 1) {
            String cmd = "LISTE DES COMMANDES :\n"
                    + "------------------------------\n"
                    + "#disconnect \t: se déconnecter \n"
                    + "#list_u \t: afficher la liste des utilisateurs connectés \n"
                    + "#list_c \t: afficher la liste des chatrooms ouvertes \n"
                    + "#create A \t: créer la chatroom A \n"
                    + "#join A \t: rejoindre la chatroom A \n"
                    + "#leave \t\t: partir de la chatroom actuelle \n"
                    + "#delete A \t: supprimer la chatroom A \n"
                    + "------------------------------\n";
            CharBuffer c = CharBuffer.wrap(cmd);
            CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
            ByteBuffer buf = encoder.encode(c);
            socket.write(buf);
        } else if (message.startsWith("#disconnect")) {
            removeUser(socket);
            socket.close();
        } else if (message.startsWith("#list_u")) {
            if (!usersInChatroom.containsKey(socket)) {
                String error = "Vous n'êtes dans aucune chatroom.";
                CharBuffer c = CharBuffer.wrap(error);
                CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
                ByteBuffer buf = encoder.encode(c);
                socket.write(buf);
            } else {
                sendList(socket);
            }
        } else if (message.startsWith("#list_c")) {
            sendChatroomList(socket);
        } else if (message.startsWith("#join")) {
            String chatroom = message.substring(6).trim();
            if (chatrooms.containsKey(chatroom)) {
                usersInChatroom.put(socket, chatroom);
            } else {
                String error = "Cette chatroom n'existe pas. Peut-être vouliez-vous la créer ?";
                CharBuffer c = CharBuffer.wrap(error);
                CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
                ByteBuffer buf = encoder.encode(c);
                socket.write(buf);
            }

        } else if (message.startsWith("#create")) {
            String chatroom = message.substring(8).trim();
            //on rajoute l'utilisateur comme admin
            if (!chatrooms.containsKey(chatroom)) {
                chatrooms.putIfAbsent(chatroom, socket);
                usersInChatroom.put(socket, chatroom);
            } else {
                String error = "Une chatroom existe déjà sous ce nom. Peut-être vouliez-vous la rejoindre ?";
                CharBuffer c = CharBuffer.wrap(error);
                CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
                ByteBuffer buf = encoder.encode(c);
                socket.write(buf);
            }

        } else if (message.startsWith("#leave")) {
            if (usersInChatroom.size() > 0 && usersInChatroom.containsKey(socket)) {
                usersInChatroom.remove(socket);
            } else {
                String error = "Vous n'êtes actuellement dans aucune chatroom.";
                CharBuffer c = CharBuffer.wrap(error);
                CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
                ByteBuffer buf = encoder.encode(c);
                socket.write(buf);
            }
        } else if (message.startsWith("#delete")) {
            String chatroom = message.substring(8).trim();
            if (chatrooms.containsKey(chatroom)) {
                if (chatrooms.get(chatroom) == socket) {
                    chatrooms.remove(chatroom);
                    for (Map.Entry<SocketChannel, String> entry : usersInChatroom.entrySet()) {
                        if (entry.getValue().equals(chatroom)) {
                            usersInChatroom.remove(entry.getKey());
                        }
                    }
                } else {
                    String error = "Vous n'avez pas le droit de supprimer cette chatroom.";
                    CharBuffer c = CharBuffer.wrap(error);
                    CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
                    ByteBuffer buf = encoder.encode(c);
                    socket.write(buf);
                }
            } else {
                String error = "Cette chatroom n'existe pas.";
                CharBuffer c = CharBuffer.wrap(error);
                CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
                ByteBuffer buf = encoder.encode(c);
                socket.write(buf);
            }
        } else {
            String error = "Commande non reconnue. Taper # pour "
                    + "afficher la liste des commandes.";
            CharBuffer c = CharBuffer.wrap(error);
            CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
            ByteBuffer buf = encoder.encode(c);
            socket.write(buf);
        }
    }

    public void broadcastMessage(SocketChannel sourceSocket, Message m) throws CharacterCodingException, IOException {
        String msg = m.getCmpt() + "#" + m.getPseudo() + " : " + m.getData();
        msg = m.getPseudo() + " : " + m.getData();
        CharBuffer c = CharBuffer.wrap(msg);
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer buf = encoder.encode(c);

        if (!usersInChatroom.containsKey(sourceSocket)) {
            String error = "Vous n'êtes dans aucune chatroom. Merci d'en joindre une"
                    + " ou d'en créer une.";
            c = CharBuffer.wrap(error);
            encoder = Charset.forName("UTF-8").newEncoder();
            buf = encoder.encode(c);
            sourceSocket.write(buf);
        } else {
            String chatroom = usersInChatroom.get(sourceSocket);
            for (SelectionKey key : selector.keys()) {
                if (!(key.channel() instanceof SocketChannel)) {
                    continue;
                }
                SocketChannel socket = (SocketChannel) key.channel();
                if (!users.containsKey(socket)) {
                    continue;
                }
                if (!usersInChatroom.get(socket).equals(chatroom)) {
                    continue;
                }
                socket.write(buf);
                buf.rewind();
            }
            cmpt++;
        }
    }

    public void broadcastServerMessage(SocketChannel sourceSocket, String m) throws CharacterCodingException, IOException {
        CharBuffer c = CharBuffer.wrap(m);
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer buf = encoder.encode(c);
        if (usersInChatroom.containsKey(sourceSocket)) {
            String chatroom = usersInChatroom.get(sourceSocket);
            for (SelectionKey key : selector.keys()) {
                if (!(key.channel() instanceof SocketChannel)) {
                    continue;
                }
                SocketChannel socket = (SocketChannel) key.channel();
                if (!users.containsKey(socket)) {
                    continue;
                }
                if (!usersInChatroom.get(socket).equals(chatroom)) {
                    continue;
                }
                socket.write(buf);
                buf.rewind();
            }
        }
    }

    public void sendList(SocketChannel socket) throws CharacterCodingException, IOException {

        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        CharBuffer c = null;
        String nameList = "#list_u";
        nameList = "";
        String chatroom = usersInChatroom.get(socket);
        for (Map.Entry<SocketChannel, String> entry : usersInChatroom.entrySet()) {
            if (entry.getValue().equals(chatroom)) {
                SocketChannel sc = entry.getKey();
                String username = users.get(sc);
                nameList = nameList + username + " ";
            }
        }
        c = CharBuffer.wrap(nameList);
        ByteBuffer buf = ByteBuffer.allocate(2000);
        buf = encoder.encode(c);
        socket.write(buf);
    }

    public void sendChatroomList(SocketChannel socket) throws CharacterCodingException, IOException {
        String chatroomList = "";
        for (String chatroom : chatrooms.keySet()) {
            chatroomList = chatroomList + chatroom + " ";
        }
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        CharBuffer c = CharBuffer.wrap(chatroomList);
        ByteBuffer buf = ByteBuffer.allocate(2000);
        buf = encoder.encode(c);
        socket.write(buf);
    }

    public void removeUser(SocketChannel socket) throws IOException {
        String username = users.get(socket);
        users.remove(socket);
        broadcastServerMessage(socket, username + " s'est déconnecté.");
    }

}
