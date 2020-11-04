package edu.cecar.dominoes2.Cliente.logica;

import edu.cecar.dominoes2.Cliente.vista.Main;
import edu.cecar.dominoes2.Cliente.vista.PanelFichaHorizonta;
import edu.cecar.dominoes2.Cliente.vista.PanelFichaVertical;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
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
import javax.swing.border.Border;

public class Peticion {

    Socket conexion;
    
    
    //esta variable controla cuando comenzar a pedir el turno al servidor, si esta variabel esta en true quiere decir que ya el cliente hizo
    // una jugada valida, de lo contrario permanecera en false para no solicitar mas turnos
    boolean controlTurno = true;

    public Peticion() {
    }

    public Peticion(Socket socket) {
        conexion = socket;
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
                usuario.setText(nombre);
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
            pv.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    System.out.println(pv.getArriba());
                    panel2.removeAll();

                    panel2.add(new PanelFichaHorizonta(pv.getArriba(), pv.getAbajo()));
                    panel2.revalidate();
                    panel2.repaint();
                }
            });
            panel.add(pv);
        }
        panel.revalidate();
        panel.repaint();

    }

    public void iniciarNotificadorTurno(Frame frame,JButton izquierda, JButton paso, JButton derecha) {
        new Thread(() -> {

            while (true) {
                if (controlTurno) {
                    JOptionPane.showMessageDialog(frame, recibirMensaje());
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

    public boolean jugarIzquierda(PanelFichaHorizonta panel) {
        boolean resultado = false;
        PanelFichaHorizonta pv = panel;

        mandarMensaje("jugarIzquierda");
        mandarMensaje(pv.getDerecha() + ";" + pv.getIzquierda());

        if (recibirMensaje().equals("0")) {
            JOptionPane.showMessageDialog(panel, "Jugada no valida");
        } else {
            resultado = true;
            controlTurno= true;
        }

        return resultado;
    }
    
    public boolean jugarDerecha(PanelFichaHorizonta panel) {
        boolean resultado = false;
        PanelFichaHorizonta pv = panel;

        mandarMensaje("jugarDerecha");
        mandarMensaje(pv.getDerecha() + ";" + pv.getIzquierda());

        if (recibirMensaje().equals("0")) {
            JOptionPane.showMessageDialog(panel, "Jugada no valida");
        } else {
            resultado = true;
            controlTurno= true;
        }

        return resultado;
    }
    
    public void paso(){
        mandarMensaje("paso");
        controlTurno= true;
    }

}
