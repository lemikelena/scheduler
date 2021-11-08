package scheduler;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;


interface Job {
    void work();
    int priority();
}

interface Scheduler {
    List<Job> completed();
    void waitAll() throws InterruptedException;
    void schedule(Job j);
    void scheduleMany(Job... jobs);
    void dispose() throws InterruptedException;
}

public class SchedulerImpl implements Scheduler {
    private final static int TOTAL_WOERKERS_COUNT = 1; 
    PriorityBlockingQueue<Job> jobs_queue = new PriorityBlockingQueue<Job>(10, new Comparator<Job>() {
        public int compare (Job j1, Job j2) {
            return j2.priority() - j1.priority();
        }
    });
    //private ReentrantLock mutex = new ReentrantLock();
    List <Job> jobs = Collections.synchronizedList(new ArrayList<>());
    List <Runnable> threads = new ArrayList <Runnable>();
    private final ExecutorService exec = Executors.newFixedThreadPool(TOTAL_WOERKERS_COUNT + 1); // +1 поток на управление очередью
    public volatile boolean stopped = false;

    public void dispose() throws InterruptedException {
        stopped = true;
        waitAll();
    }

    public void waitAll() throws InterruptedException {
        //threadSleep(1000);
        exec.awaitTermination(1, TimeUnit.SECONDS);
    }

    public SchedulerImpl() {
        Runnable task = new Runnable() {    //тот кто берет задачи и кладет их в очередь
            public void run() {
                try {
                    while (!stopped) {
                        Job j = jobs_queue.take();
                        Runnable job_task = () -> {
                            j.work();
                            jobs.add(j);
                        };
                        exec.execute(job_task);
                    }
                } catch (InterruptedException ex) {

                }
            }
        };
        exec.execute(task);
    }

    /*private void threadSleep(int milliseconds) {
        try {
            //50 should be enough
            Thread.sleep(100);
        } catch (Exception e) {
        }
    }*/

    public void schedule(Job j) {
        //jobs.add(j);
        jobs_queue.add(j);
    }

    public void scheduleMany(Job... jobs) {

    }


    public List <Job> completed() {
        return jobs;
    }

}

class ScheduleExt extends SchedulerImpl {
    public void scheduleMany(Job... jobs) {
        for (Job job : jobs) {
            schedule(job);
        }
    }
    
}
