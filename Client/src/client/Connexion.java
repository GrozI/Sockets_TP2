/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author gambicca
 */
public class Connexion {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Client client = new Client();
        System.out.println("Bonjour !");
        client.setPseudo();
        client.connect("127.0.0.1", 8080);
        System.out.println("Ecrivez un message. #disconnect pour se déconnecter, "
                + "#list pour afficher la liste de clients connectés");
        ListenThread thread = new ListenThread(client);
        thread.start();
        Scanner scanner = new Scanner(System.in);
        boolean connecte = true;
        while (connecte) {
            //client.receiveMsg();
            String message = scanner.next();
            message += scanner.nextLine();
            if (message.startsWith("#")) {
                switch (message.substring(1).toLowerCase()) {
                    case "disconnect": {
                        client.disconnect();
                        connecte = false;
                        break;
                    }
                    case "list": {
                        //client.printList();
                        client.receiveList();
                        break;
                    }
                }
            } else {
                //System.out.println(client.getPseudo() + " : " + message);
                client.sendMessage(message);
            }
        }
    }
}
