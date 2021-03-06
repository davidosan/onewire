package by.bsu.onewire.core.sheduler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.OneWireIOException;

import by.bsu.onewire.core.network.NetworkManager;

public class SimpleSchedulerImpl implements Scheduler {
    private Log log = LogFactory.getLog(SimpleSchedulerImpl.class);

    protected BlockingQueue<TaskContainer> queue;
    protected List<TaskContainer> repeatTasks;

    protected TaskTimeProcessor timeProcessor;
    
    protected NetworkManager networkManager;

    /**
     * Public constructor, create queue.
     */
    public SimpleSchedulerImpl() {
        queue = new LinkedBlockingQueue<TaskContainer>();
        repeatTasks = new LinkedList<TaskContainer>();
    }

    /**
     * Add task with default properties in queue.
     */
    @Override
    public void addTask(Task task) {
        addTask(task, getDefaultTaskProperties());
    }

    /**
     * Add task in queue. This task will be executed later in 1-Wire context.
     * 
     * @param properties
     *            bean object that should contain task execution properties
     */
    @Override
    public void addTask(Task task, TaskProperties properties) {
        try {
            TaskContainer container = new TaskContainer(task, properties);
            if (needRepeat(container)) {
                timeProcessor.updateTaskStartTime(container);
                synchronized (repeatTasks) {
                    repeatTasks.add(container);
                }
            }
            queue.put(container);
        } catch (InterruptedException e) {
            log.error("Task wait interrupted",e);
        }
    }
    
    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    /**
     * Execute next task from queue.
     */
    public void executeNextTask() {
        try {
            TaskContainer container = queue.take();
            Task task = container.getTask();
            executeTask(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Analyze repeat tasks list and add in queue tasks that re ready for
     * execution.
     */
    public void processRepeatTasks() {
        synchronized (repeatTasks) {
            Iterator<TaskContainer> iterator = repeatTasks.iterator();
            while (iterator.hasNext()) {
                TaskContainer container = iterator.next();
                if (readyForExecution(container)) {
                    if (!needRepeat(container)) {
                        iterator.remove();
                    }
                    timeProcessor.updateTaskStartTime(container);
                    try {
                        queue.put(container);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Execute task in 1-Wire context.
     */
    protected void executeTask(Task task) {
        task.setNetworkManager(networkManager);
        try {
            networkManager.startSession();
            task.execute();
            networkManager.endSession();
        } catch (OneWireIOException e) {
            log.error("Task execution failed.", e);
        } catch (OneWireException e) {
            log.error("Task execution failed.", e);
        }
        
    }

    /**
     * Check if task should be executed one more time.
     */
    protected boolean needRepeat(TaskContainer taskContainer) {
        return taskContainer.getProperties().isRepeat();
    }

    /**
     * Check if task is ready for execution.
     */
    protected boolean readyForExecution(TaskContainer taskContainer) {
        return timeProcessor.isTaskTime(taskContainer);
    }

    /**
     * Get default <code>TaskProperties</code> object for this
     * <code>Scheduler</code> implementation.
     */
    public static final TaskProperties getDefaultTaskProperties() {
        TaskProperties properties = new TaskProperties();
        properties.setRepeat(false);
        return properties;
    }

    public TaskTimeProcessor getTimeProcessor() {
        return timeProcessor;
    }

    public void setTimeProcessor(TaskTimeProcessor timeProcessor) {
        this.timeProcessor = timeProcessor;
    }

}
