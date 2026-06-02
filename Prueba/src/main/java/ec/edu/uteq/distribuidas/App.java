package ec.edu.uteq.distribuidas;

import ec.edu.uteq.distribuidas.core.Node;

public class App {

    public static void main(String[] args) {

        if (args.length != 2) {

            System.out.println(
                    "Uso: java App <nodeId> <port>"
            );

            return;
        }

        int nodeId = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);

        Node node = new Node(nodeId, port);

        node.start();
    }
}