package ec.edu.uteq.distribuidas.consensus;

import ec.edu.uteq.distribuidas.model.NodeStatus;

import java.util.Map;

public class BullyElection {

    public static int electLeader(
            int myId,
            Map<Integer, NodeStatus> nodes
    ) {

        int leader = myId;

        for (Map.Entry<Integer, NodeStatus> entry
                : nodes.entrySet()) {

            if (entry.getValue().isActive()) {

                if (entry.getKey() > leader) {
                    leader = entry.getKey();
                }
            }
        }

        Coordinator.setCoordinatorId(leader);

        return leader;
    }
}