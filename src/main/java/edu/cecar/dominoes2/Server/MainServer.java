/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cecar.dominoes2.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class MainServer {
    

    public static void main(String[] args) throws IOException {
         String[] fichas = {"0-0", "0-1", "0-2", "0-3", "0-4", "0-5", "0-6", "1-1", "1-2", "1-3", "1-4", "1-5", "1-6", "2-2", "2-3", "2-4", "2-5", "2-6", "3-3", "3-4", "3-5", "3-6", "4-4", "4-5", "4-6", "5-5", "5-6", "6-6"};

         
         ArrayList<String> fichasAleadorias = new ArrayList<>();
         
           for (String ficha : fichas) {
            fichasAleadorias.add(ficha);
        }
            ArrayList<Thread> clientes = new ArrayList<>();
        ServerSocket server = new ServerSocket(2020);
        System.out.println("Server inicado");

        for (int i = 0; i < 2; i++) {
            clientes.add(new Thread(new ServerHilo(server.accept(), i,fichasAleadorias)));
 
            clientes.get(i).start();
            System.out.println("llega un cliente");
        }
    }
}
