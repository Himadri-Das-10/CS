package Offload;

public class SeprateTask {

    private static SeprateTask seprateTask;

    private SeprateTask() {
    }

    public static SeprateTask getInstance() {

        if (seprateTask == null) {
            seprateTask = new SeprateTask();
        }

        return seprateTask;
    }

    public void offload(Runnable task) {

        Thread thread = new Thread(task);

        thread.setDaemon(true);

        thread.start();
    }
}
