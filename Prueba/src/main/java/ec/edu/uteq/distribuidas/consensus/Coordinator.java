package ec.edu.uteq.distribuidas.consensus;

public class Coordinator {

    private static volatile int coordinatorId = 3;

    public static int getCoordinatorId() {
        return coordinatorId;
    }

    public static void setCoordinatorId(int id) {
        coordinatorId = id;
    }
}