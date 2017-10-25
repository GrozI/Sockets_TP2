/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;

/**
 *
 * @author gambicca
 */
public class Connexion2 {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Client client = new Client();
        System.out.println("Bonjour2 !!!");
        client.getPseudo();
        client.connect("127.0.0.1", 8080);
    }
}
