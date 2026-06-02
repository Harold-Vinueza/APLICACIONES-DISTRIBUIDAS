package ec.edu.uteq.distribuidas.core;

import ec.edu.uteq.distribuidas.consensus.BullyElection;
import ec.edu.uteq.distribuidas.consensus.Coordinator;
import ec.edu.uteq.distribuidas.model.NodeInfo;
import ec.edu.uteq.distribuidas.model.NodeStatus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Node {

    private final int nodeId;
    private final int port;
    private final LamportClock clock;
    private final NodeInfo[] peers;

    private final Map<Integer, NodeStatus> nodeStatusMap =
            new HashMap<>();

    public Node(int nodeId, int port) {

        this.nodeId = nodeId;
        this.port = port;
        this.clock = new LamportClock();

        this.peers = new NodeInfo[]{
                new NodeInfo(1, 5001),
                new NodeInfo(2, 5002),
                new NodeInfo(3, 5003)
        };

        for (NodeInfo peer : peers) {
            if (peer.getNodeId() != nodeId) {
                nodeStatusMap.put(
                        peer.getNodeId(),
                        new NodeStatus()
                );
            }
        }

        Coordinator.setCoordinatorId(3);
    }

    public void start() {

        startHeartbeatSender();
        startFailureDetector();

        System.out.println(
                "Nodo N" + nodeId +
                        " escuchando en puerto " + port
        );

        System.out.println(
                "Coordinador actual: N" +
                        Coordinator.getCoordinatorId()
        );

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {

                Socket clientSocket = serverSocket.accept();

                try {

                    DataInputStream in =
                            new DataInputStream(clientSocket.getInputStream());

                    DataOutputStream out =
                            new DataOutputStream(clientSocket.getOutputStream());

                    String message = in.readUTF();

                    /*
                     * HEARTBEAT
                     */
                    if (message.startsWith("HEARTBEAT|")) {

                        int senderId =
                                Integer.parseInt(message.split("\\|")[1]);

                        NodeStatus status =
                                nodeStatusMap.get(senderId);

                        if (status != null) {
                            status.updateHeartbeat();
                            status.setActive(true);
                        }

                        out.writeUTF("HB_OK");
                        out.flush();
                        continue;
                    }

                    /*
                     * REPLICA
                     */
                    if (message.startsWith("REPLICA|")) {

                        String[] parts = message.split("\\|");

                        int receivedLamport =
                                Integer.parseInt(parts[1]);

                        String operation = parts[2];

                        int newTime = clock.update(receivedLamport);

                        System.out.println(
                                "[Lamport=" + newTime + "] N" +
                                        nodeId + " recibió réplica: " +
                                        operation
                        );

                        out.writeUTF("REPLICA_OK");
                        out.flush();
                        continue;
                    }

                    /*
                     * CLIENTE
                     */
                    String[] parts = message.split("\\|");

                    if (parts.length != 2) {

                        out.writeUTF("ERROR: FORMATO INVALIDO");
                        out.flush();
                        continue;
                    }

                    String token = parts[0];
                    String operation = parts[1];

                    if (!TokenValidator.validate(token)) {

                        out.writeUTF("ERROR: TOKEN INVALIDO");
                        out.flush();
                        continue;
                    }

                    int timestamp = clock.increment();

                    System.out.println(
                            "[Lamport=" + timestamp + "] Nodo N" +
                                    nodeId + " recibió operación: " +
                                    operation
                    );

                    replicateOperation(operation);

                    out.writeUTF("OK");
                    out.flush();

                } finally {
                    clientSocket.close();
                }
            }

        } catch (IOException e) {

            System.err.println("Error en nodo N" + nodeId);
            e.printStackTrace();
        }
    }

    private void replicateOperation(String operation) {

        for (NodeInfo peer : peers) {

            if (peer.getNodeId() == nodeId) continue;

            try {

                Socket socket =
                        new Socket("localhost", peer.getPort());

                DataOutputStream out =
                        new DataOutputStream(socket.getOutputStream());

                out.writeUTF(
                        "REPLICA|" +
                                clock.getTime() +
                                "|" +
                                operation
                );

                out.flush();
                socket.close();

            } catch (Exception e) {
                System.out.println("No se pudo replicar a N" + peer.getNodeId());
            }
        }
    }

    /*
     * HEARTBEATS
     */
    private void startHeartbeatSender() {

        Thread thread = new Thread(() -> {

            while (true) {

                try {

                    for (NodeInfo peer : peers) {

                        if (peer.getNodeId() == nodeId) continue;

                        try {

                            Socket socket =
                                    new Socket("localhost", peer.getPort());

                            DataOutputStream out =
                                    new DataOutputStream(socket.getOutputStream());

                            out.writeUTF("HEARTBEAT|" + nodeId);

                            out.flush();
                            socket.close();

                        } catch (Exception ignored) {}
                    }

                    Thread.sleep(5000);

                } catch (Exception ignored) {}
            }

        });

        thread.setDaemon(true);
        thread.start();
    }

    /*
     * FALLAS + BULLY
     */
    private void startFailureDetector() {

        Thread thread = new Thread(() -> {

            while (true) {

                try {

                    long now = System.currentTimeMillis();

                    for (Map.Entry<Integer, NodeStatus> entry
                            : nodeStatusMap.entrySet()) {

                        NodeStatus status = entry.getValue();

                        long elapsed =
                                now - status.getLastHeartbeat();

                        if (elapsed > 10000 && status.isActive()) {

                            status.setActive(false);

                            System.out.println(
                                    "Nodo N" + entry.getKey() +
                                            " marcado como CAIDO"
                            );

                            if (entry.getKey() ==
                                    Coordinator.getCoordinatorId()) {

                                System.out.println(
                                        "Coordinador caído → iniciando elección Bully..."
                                );

                                int newLeader =
                                        BullyElection.electLeader(
                                                nodeId,
                                                nodeStatusMap
                                        );

                                Coordinator.setCoordinatorId(newLeader);

                                System.out.println(
                                        "Nuevo coordinador: N" + newLeader
                                );
                            }
                        }
                    }

                    Thread.sleep(3000);

                } catch (Exception ignored) {}
            }

        });

        thread.setDaemon(true);
        thread.start();
    }
}