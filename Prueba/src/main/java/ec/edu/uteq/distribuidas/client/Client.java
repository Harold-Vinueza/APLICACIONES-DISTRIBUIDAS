package ec.edu.uteq.distribuidas.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {

        try {

            System.out.println("Conectando al nodo...");

            Socket socket = new Socket("localhost", 5001);

            System.out.println("Conexión establecida.");

            DataOutputStream out =
                    new DataOutputStream(socket.getOutputStream());

            DataInputStream in =
                    new DataInputStream(socket.getInputStream());

            //String operation = "Movimiento_001";
            String operation = "TOKEN123|Movimiento_001";
            //String operation = "TOKEN_MALO|Movimiento_001";

            System.out.println("Enviando operación: " + operation);

            out.writeUTF(operation);
            out.flush();

            System.out.println("Esperando respuesta del nodo...");

            String response = in.readUTF();

            System.out.println("Respuesta del nodo: " + response);

            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {

            System.err.println("Error en el cliente:");
            e.printStackTrace();
        }
    }
}