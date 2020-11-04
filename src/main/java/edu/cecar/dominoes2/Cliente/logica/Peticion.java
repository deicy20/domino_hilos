package edu.cecar.dominoes2.Cliente.logica;

import edu.cecar.dominoes2.Cliente.vista.Main;
import edu.cecar.dominoes2.Cliente.vista.PanelFichaHorizonta;
import edu.cecar.dominoes2.Cliente.vista.PanelFichaVertical;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.TextArea;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

public class Peticion {

    Socket conexion;
    JPanel tableroMesa;

    //esta variable controla cuando comenzar a pedir el turno al servidor, si esta variabel esta en true quiere decir que ya el cliente hizo
    // una jugada valida, de lo contrario permanecera en false para no solicitar mas turnos
    boolean controlTurno = true;

    public Peticion() {
    }

    public Peticion(Socket socket, JPanel panel) {
        conexion = socket;
        tableroMesa = panel;
    }

    public void mandarMensaje(String mensaje) {
        try {
            new DataOutputStream(conexion.getOutputStream()).writeUTF(mensaje);
        } catch (IOException ex) {
            Logger.getLogger(Peticion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String recibirMensaje() {

        try {
            return new DataInputStream(conexion.getInputStream()).readUTF();
        } catch (IOException ex) {
            Logger.getLogger(Peticion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public Boolean validarNombre(JLabel usuario) {
        JOptionPane.showMessageDialog(null, recibirMensaje());
        //mandarMensaje("validarNombre");
        while (true) {
            String nombre = JOptionPane.showInputDialog("ingresa tu nombre");

            mandarMensaje(nombre);
            String[] mensajeRespuesta = recibirMensaje().split(";");
            System.out.println(mensajeRespuesta[0] + " " + mensajeRespuesta[1]);
            JOptionPane.showMessageDialog(null, mensajeRespuesta[1]);
            if (mensajeRespuesta[0].equals("1")) {
                usuario.setText("Nombre jugador: " + nombre);
                usuario.revalidate();
                usuario.repaint();

                break;
            }

        }
        return false;

    }

    public void pedirFichas(JPanel panel, JPanel panel2) {

        String mensja = recibirMensaje();
        String[] fichas = mensja.split(";");

        for (String ficha : fichas) {
            String[] numero = ficha.split("-");
            PanelFichaVertical pv = new PanelFichaVertical(numero[0], numero[1]);
            pv.setName(numero[0] + "" + numero[1]);

            pv.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    System.out.println(pv.getArriba());
                    panel2.removeAll();
                    PanelFichaHorizonta ph = new PanelFichaHorizonta(pv.getArriba(), pv.getAbajo());

                    ph.setName(pv.getName());
                    //System.out.println("ph: "+ph.getName() +" pv: "+ pv.getName());

                    panel2.add(ph);
                    panel2.revalidate();
                    panel2.repaint();
                }
            });
            panel.add(pv);
        }
        panel.revalidate();
        panel.repaint();

    }

    public void iniciarNotificadorTurno(Frame frame, JButton izquierda, JButton paso, JButton derecha) {
        new Thread(() -> {

            while (true) {
                if (controlTurno) {
                    String[] mens = recibirMensaje().split(";");
                    if (mens.equals("1")) {

                        JOptionPane.showMessageDialog(frame, mens[1]);
                        System.exit(1);
                        break;
                    }
                    JOptionPane.showMessageDialog(frame, mens[1]);
                    actualizarTablero();
                    izquierda.setEnabled(true);
                    paso.setEnabled(true);
                    derecha.setEnabled(true);
                    controlTurno = false;

                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }).start();
    }

    public boolean jugarIzquierda(JPanel panelContenedor, PanelFichaHorizonta panel) {
        boolean resultado = false;
        PanelFichaHorizonta pv = panel;

        mandarMensaje("jugarIzquierda");
        mandarMensaje(pv.getDerecha() + ";" + pv.getIzquierda());

        if (recibirMensaje().equals("0")) {
            JOptionPane.showMessageDialog(panel, "Jugada no valida");
        } else {

            for (Component cp : panelContenedor.getComponents()) {

                if (cp.getName().equals(panel.getName())) {
                    panelContenedor.remove(cp);
                    break;
                }
            }

            panelContenedor.revalidate();
            panelContenedor.repaint();

            resultado = true;
            controlTurno = true;
        }

        return resultado;
    }

    public boolean jugarDerecha(JPanel panelContenedor, PanelFichaHorizonta panel) {
        boolean resultado = false;
        PanelFichaHorizonta pv = panel;

        mandarMensaje("jugarDerecha");
        mandarMensaje(pv.getDerecha() + ";" + pv.getIzquierda());

        if (recibirMensaje().equals("0")) {
            JOptionPane.showMessageDialog(panel, "Jugada no valida");
        } else {

            for (Component cp : panelContenedor.getComponents()) {

                if (cp.getName().equals(panel.getName())) {
                    panelContenedor.remove(cp);
                    break;
                }
            }

            panelContenedor.revalidate();
            panelContenedor.repaint();

            resultado = true;
            controlTurno = true;
        }

        return resultado;
    }

    public void actualizarTablero() {
        String mensaje = recibirMensaje();
        System.out.println("mensjae -" + mensaje + "-");
        String[] fichasActualizacion = mensaje.split(";");
        if (fichasActualizacion.length > 0) {
            tableroMesa.removeAll();
            tableroMesa.add(Box.createHorizontalGlue());
            for (int i = 0; i < fichasActualizacion.length - 1; i += 2) {

                if (fichasActualizacion[i].equals(fichasActualizacion[i + 1])) {
                    PanelFichaVertical fichaVertical = new PanelFichaVertical(fichasActualizacion[i], fichasActualizacion[i + 1]);
                    tableroMesa.add(fichaVertical);
                } else {
                    PanelFichaHorizonta fichaHorizonta = new PanelFichaHorizonta(fichasActualizacion[i], fichasActualizacion[i + 1]);
                    tableroMesa.add(fichaHorizonta);
                }
                System.out.println(fichasActualizacion[i] + "--" + fichasActualizacion[i + 1]);
            }
            tableroMesa.add(Box.createHorizontalGlue());
            tableroMesa.revalidate();
            tableroMesa.repaint();
        }
    }

    public void paso() {
        mandarMensaje("paso");
        controlTurno = true;
    }

    public void puntos(JTextArea area) {

        String[] mensaje = recibirMensaje().split("@");
        if (mensaje.length > 2) {
            area.setText("");
            for (String datos : mensaje) {
                String[] datosSeparados = datos.split(";");

                area.append("Jugador-> " + datosSeparados[0] + " G: " + datosSeparados[1] + " P: " + datosSeparados[2] + " \n");
            }
        }
    }

}
