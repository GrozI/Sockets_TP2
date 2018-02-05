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
        //System.out.println("Bonjour !");
        //client.setPseudo();
        client.connect("127.0.0.1", 8080);

        Thread t = new Thread(new ListenThread(client));
        t.start();
        Scanner scanner = new Scanner(System.in);
        boolean connecte = true;
        while (connecte) {
            //client.receiveMsg();
            String message = scanner.next();
            message += scanner.nextLine();
            if (message.startsWith("#")) {
                if (message.length() == 1) {
                    System.out.println("LISTE DES COMMANDES :\n"
                            + "------------------------------\n"
                            + "#disconnect \t: se déconnecter \n"
                            + "#list_u \t: afficher la liste des utilisateurs connectés \n"
                            + "#list_c \t: afficher la liste des chatrooms ouvertes \n"
                            + "#create A \t: créer la chatroom A \n"
                            + "#join A \t: rejoindre la chatroom A \n"
                            + "------------------------------\n");
                } else {
                    switch (message.substring(1).toLowerCase()) {
                        case "disconnect": {
                            client.disconnect();
                            connecte = false;
                            t.interrupt();
                            break;
                        }
                        case "list_u": {
                            //client.printList();
                            client.askList();
                            break;
                        }
                        default: {
                            System.out.println("Commande non reconnue. Taper # pour "
                                    + "afficher la liste des commandes.");
                        }
                    }
                }
            } else {
                client.sendMessage(message);
            }
        }
    }
}
