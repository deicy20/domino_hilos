/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cecar.dominoes2.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainServer {

    ArrayList<Socket> conexiones = new ArrayList<Socket>();
    HashMap<String, String> puntos = new HashMap<String, String>();

    ServerSocket server;
    int turno = 0;
    int numJugadores = 4;
    int cantidadFichas =7;
    String[] fichas = {"0-0", "0-1", "0-2", "0-3", "0-4", "0-5", "0-6", "1-1", "1-2", "1-3", "1-4", "1-5", "1-6", "2-2", "2-3", "2-4", "2-5", "2-6", "3-3", "3-4", "3-5", "3-6", "4-4", "4-5", "4-6", "5-5", "5-6", "6-6"};
    ArrayList<String> nombres = new ArrayList<String>();
    ArrayList<String> fichasAleadorias = new ArrayList<String>();
    ArrayList<String> fichasEnMesa = new ArrayList<String>();
    ArrayList<ArrayList<String>> fichasJugador = new ArrayList<ArrayList<String>>();
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

        try {
            cargarDatos();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        server = new ServerSocket(2020);
        System.out.println("Server inicado");

        for (int i = 0; i < numJugadores; i++) {
            conexiones.add(server.accept());
            fichasJugador.add(new ArrayList<String>());
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
            darFichas(i);
            siguienteTurno();
        }
        String puntosEnviar = "";
        for (String string : puntos.keySet()) {
            puntosEnviar += string + ";" + puntos.get(string) + "@";
            System.out.println("" + string);
        }
        for (int i = 0; i < numJugadores; i++) {
            enviarMensaje(puntosEnviar);
            siguienteTurno();
        }
        while (true) {

            if (controlTurno) {
                int pos = ganador();
                if (pos < 0) {
                    enviarMensaje("0;turno");
                    actualizacion();
                } else {
                    guardarDatos(pos);
                    enviarMensaje("1; ganador el jugador: " + nombres.get(pos));
                }
            }
            controlTurno = false;
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
            System.out.println("ganador" + ganador());

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

    private void darFichas(int j) {

        String fichas = "";
        for (int i = 0; i < cantidadFichas; i++) {
            int pos = (int) (Math.random() * ((fichasAleadorias.size())));
            System.out.println(fichasAleadorias.get(pos));
            fichasJugador.get(j).add(fichasAleadorias.get(pos));
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
                removerFichaCliente(ficha[0] + "-" + ficha[1]);
                resultado = "1";

            } else if (fichasEnMesa.get(0).equals(ficha[1])) {

                fichasEnMesa.add(0, ficha[1]);
                fichasEnMesa.add(0, ficha[0]);
                removerFichaCliente(ficha[0] + "-" + ficha[1]);
                resultado = "1";

            }

        } else {
            fichasEnMesa.add(0, ficha[0]);
            fichasEnMesa.add(0, ficha[1]);
            removerFichaCliente(ficha[0] + "-" + ficha[1]);
            resultado = "1";

        }

        System.out.println("------------");
        for (String string : fichasEnMesa) {
            System.out.println(string);
        }
        System.out.println("------------");

        enviarMensaje(resultado);

        if (resultado.equals("1")) {
            siguienteTurno();
            controlTurno = true;
        }
    }

    private void validarDerecha(String[] ficha) {

        String resultado = "0";
        int ultimaFicha = fichasEnMesa.size() - 1;
        if (fichasEnMesa.size() > 0) {
            if (fichasEnMesa.get(ultimaFicha).equals(ficha[0])) {
                fichasEnMesa.add(ficha[0]);
                fichasEnMesa.add(ficha[1]);
                removerFichaCliente(ficha[0] + "-" + ficha[1]);
                resultado = "1";

            } else if (fichasEnMesa.get(ultimaFicha).equals(ficha[1])) {

                fichasEnMesa.add(ficha[1]);
                fichasEnMesa.add(ficha[0]);
                removerFichaCliente(ficha[0] + "-" + ficha[1]);
                resultado = "1";

            }

        } else {
            fichasEnMesa.add(ficha[1]);
            fichasEnMesa.add(ficha[0]);
            removerFichaCliente(ficha[0] + "-" + ficha[1]);
            resultado = "1";

        }
        /*
        System.out.println("------------");
        for (String string : fichasEnMesa) {
            System.out.println(string);
        }
        System.out.println("------------");
         */
        enviarMensaje(resultado);

        if (resultado.equals("1")) {
            siguienteTurno();
            controlTurno = true;
        }

    }

    private void removerFichaCliente(String ficha) {

        String[] fichaPartida = ficha.split("-");

        if (!fichasJugador.get(turno).remove(ficha)) {
            fichasJugador.get(turno).remove(fichaPartida[1] + "-" + fichaPartida[0]);
        }

        System.out.println("-----------------fichas restantes---------- ficha a eliminar" + ficha);
        for (ArrayList<String> arrayList : fichasJugador) {
            for (String string : arrayList) {
                System.out.println(string);
            }

        }
        System.out.println("-----------------fichas restantes----------");
    }

    private void paso() {
        siguienteTurno();
        controlTurno = true;
    }

    private void actualizacion() {
        String resultado = "";

        for (String string : fichasEnMesa) {
            resultado += string + ";";
        }
        enviarMensaje(resultado);
    }

    private int ganador() {
        if (fichasEnMesa.size() <= 0) {
            return -1;
        }

        for (int i = 0; i < fichasJugador.size(); i++) {

            if (fichasJugador.get(i).size() <= 0) {
                return i;
            }

        }
        int[] puntoJugador = new int[numJugadores];

        if (!isContinuar()) {

            int j = 0;
            for (ArrayList<String> arrayList : fichasJugador) {

                for (String ficha : arrayList) {
                    String[] numFichas = ficha.split("-");
                    puntoJugador[j] += Integer.valueOf(numFichas[0]) + Integer.valueOf(numFichas[1]);
                }
                j++;
            }

            System.out.println("puntos------------------");

            for (int i : puntoJugador) {
                System.out.println(i);
            }
            System.out.println("--------------------");
            return obtenerGanador(puntoJugador);
        }
        return -1;
    }

    public int obtenerGanador(int[] puntoFichas) {
        int minValue = puntoFichas[0];
        int pos = 0;
        for (int i = 1; i < puntoFichas.length; i++) {
            if (puntoFichas[i] < minValue) {
                minValue = puntoFichas[i];
                pos = i;
            }
        }
        return pos;

    }

    private boolean isContinuar() {
        for (ArrayList<String> arrayList : fichasJugador) {
            for (String valor : arrayList) {

                String[] numeros = valor.split("-");
                if (fichasEnMesa.get(0).equals(numeros[0]) || fichasEnMesa.get(0).equals(numeros[1]) || fichasEnMesa.get(fichasEnMesa.size() - 1).equals(numeros[0]) || fichasEnMesa.get(fichasEnMesa.size() - 1).equals(numeros[1])) {
                    System.out.println("iscontinuar: true");
                    return true;
                } else {
                    System.out.println("iscontinuar: false");
                }
            }
        }
        return false;
    }

    private void cargarDatos() throws FileNotFoundException, IOException, ClassNotFoundException {
        File file = new File("temp");
        FileInputStream f = new FileInputStream(file);
        ObjectInputStream s = new ObjectInputStream(f);
        puntos = (HashMap<String, String>) s.readObject();
        s.close();
    }

    private void guardarDatos(Integer posGanador) {

        FileOutputStream f = null;
        try {

            for (int i = 0; i < numJugadores; i++) {

                if (i == posGanador) {
                    if (puntos.containsKey(nombres.get(i))) {
                        String[] datos = puntos.get(nombres.get(i)).split(";");
                        int ganadas = Integer.valueOf(datos[0]) + 1;
                        int perdidas = Integer.valueOf(datos[1]);
                        puntos.replace(nombres.get(i), ganadas + ";" + perdidas);
                    } else {
                        puntos.put(nombres.get(i), 1 + ";" + 0);
                    }

                } else {
                    if (puntos.containsKey(nombres.get(i))) {
                        String[] datos = puntos.get(nombres.get(i)).split(";");
                        int ganadas = Integer.valueOf(datos[0]);
                        int perdidas = Integer.valueOf(datos[1]) + 1;
                        puntos.replace(nombres.get(i), ganadas + ";" + perdidas);
                    } else {
                        puntos.put(nombres.get(i), 0 + ";" + 1);
                    }

                }

            }
            if (new File("temp").exists()) {
                new File("temp").delete();
            }

            File file = new File("temp");
            f = new FileOutputStream(file);
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(puntos);
            s.flush();
            f.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
