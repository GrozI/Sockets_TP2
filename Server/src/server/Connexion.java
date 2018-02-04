/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;

/**
 *
 * @author gambicca
 */
public class Connexion {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = new Server();
        server.initialize(8080);
    }

}
