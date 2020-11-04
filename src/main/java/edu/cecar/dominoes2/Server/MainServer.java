/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cecar.dominoes2.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainServer {

    ArrayList<Socket> conexiones = new ArrayList<Socket>();
    ServerSocket server;
    int turno = 0;
    int numJugadores = 2;
    String[] fichas = {"0-0", "0-1", "0-2", "0-3", "0-4", "0-5", "0-6", "1-1", "1-2", "1-3", "1-4", "1-5", "1-6", "2-2", "2-3", "2-4", "2-5", "2-6", "3-3", "3-4", "3-5", "3-6", "4-4", "4-5", "4-6", "5-5", "5-6", "6-6"};
    ArrayList<String> nombres = new ArrayList<String>();
    ArrayList<String> fichasAleadorias = new ArrayList<String>();
    ArrayList<String> fichasEnMesa = new ArrayList<String>();
    String primerMensaje = "";
    boolean controlTurno = true;

    public static void main(String[] args) {
        try {
            new MainServer().iniciarServer();
        } catch (IOException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public MainServer() {
    }

    private void iniciarServer() throws IOException {

        server = new ServerSocket(2020);
        System.out.println("Server inicado");

        for (int i = 0; i < numJugadores; i++) {
            conexiones.add(server.accept());
            System.out.println("llega un cliente");
        }

        for (int i = 0; i < numJugadores; i++) {
            enviarMensaje("Inicia partida");
            validarNombre();
            siguienteTurno();
        }
        for (String ficha : fichas) {
            fichasAleadorias.add(ficha);
        }

        for (int i = 0; i < numJugadores; i++) {
            darFichas();
            siguienteTurno();
        }

        while (true) {

           if(controlTurno) enviarMensaje("turno");
           controlTurno=false;
            switch (recibirMensaje()) {

                case "jugarDerecha":
                    validarDerecha(recibirMensaje().split(";"));
                    break;
                case "paso":
                    paso();
                    break;
                case "jugarIzquierda":
                    System.out.println("entra");
                    validarIzquierda(recibirMensaje().split(";"));
                    break;
            }

            

        }
    }

    private void enviarMensaje(String mensaje) {
        try {
            new DataOutputStream(conexiones.get(turno).getOutputStream()).writeUTF(mensaje);
        } catch (IOException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String recibirMensaje() {

        try {
            return new DataInputStream(conexiones.get(turno).getInputStream()).readUTF();
        } catch (IOException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    private void siguienteTurno() {
        turno++;

        if (turno >= numJugadores) {
            turno = 0;
        }
    }

    private void validarNombre() {
        while (true) {
            String nombre = recibirMensaje();

            if (!nombres.contains(nombre)) {
                nombres.add(nombre);
                enviarMensaje("1; nombre registrado");
                break;
            } else {
                enviarMensaje("0;no es posible utilizar ese nombre");
            }
        }

    }

    private void darFichas() {

        String fichas = "";
        for (int i = 0; i < 7; i++) {
            int pos = (int) (Math.random() * ((fichasAleadorias.size())));
            System.out.println(fichasAleadorias.get(pos));
            fichas += fichasAleadorias.get(pos) + ";";
            fichasAleadorias.remove(pos);
        }
        enviarMensaje(fichas);

    }

    private void validarIzquierda(String[] ficha) {
        String resultado = "0";
        if (fichasEnMesa.size() > 0) {
            if (fichasEnMesa.get(0).equals(ficha[0])) {
                fichasEnMesa.add(0, ficha[0]);
                fichasEnMesa.add(0, ficha[1]);
                resultado = "1";
                

            } else if (fichasEnMesa.get(0).equals(ficha[1])) {

                fichasEnMesa.add(0, ficha[1]);
                fichasEnMesa.add(0, ficha[0]);
                resultado = "1";
               

            }

        } else {
            fichasEnMesa.add(0, ficha[0]);
            fichasEnMesa.add(0, ficha[1]);
            resultado = "1";
            
        }

        System.out.println("------------");
        for (String string : fichasEnMesa) {
            System.out.println(string);
        }
        System.out.println("------------");

        enviarMensaje(resultado);
        
        if(resultado.equals("1")){  
            siguienteTurno();
            controlTurno=true;
        }
    }
    
        private void validarDerecha(String[] ficha) {
        String resultado = "0";
        int ultimaFicha = fichasEnMesa.size()-1;
        if (fichasEnMesa.size() > 0) {
            if (fichasEnMesa.get(ultimaFicha).equals(ficha[0])) {
                fichasEnMesa.add(ficha[0]);
                fichasEnMesa.add(ficha[1]);
                resultado = "1";
                

            } else if (fichasEnMesa.get(ultimaFicha).equals(ficha[1])) {

                fichasEnMesa.add(ficha[1]);
                fichasEnMesa.add(ficha[0]);
                resultado = "1";
               

            }

        } else {
            fichasEnMesa.add(ficha[1]);
            fichasEnMesa.add(ficha[0]);
            resultado = "1";
            
        }

        System.out.println("------------");
        for (String string : fichasEnMesa) {
            System.out.println(string);
        }
        System.out.println("------------");

        enviarMensaje(resultado);
        
        if(resultado.equals("1")){  
            siguienteTurno();
            controlTurno=true;
        }
    }
        
        private void paso(){
            siguienteTurno();
            controlTurno=true;
        }
    
}
