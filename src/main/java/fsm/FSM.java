package fsm;

import fsm.components.FSMStep;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FSM Finite State Machine 
 * @author massimilianoscaletti
 * version 1.0.0
 */
public abstract class FSM implements Runnable{
    public static final String END = "end";
    public static final String ABORT = "abort";
    private boolean run = false;
    private boolean aborted = false;
    private int stepsDelay;
    private int status = 0;
    private ConcurrentHashMap<String, FSMStep> steps = new ConcurrentHashMap<>();
    private Thread fsmThread;
    protected String actualStep;
    private String startStep;
    private String fsmName;
    private int actualRow = 0;
    private boolean stepChanged = false;

    /**
     * Called before entering int the main loop
     */
    protected abstract void threadSetup();

    /**
     * Called inside the main loop
     * It can be used to override the actualStep, doing other operations
     * and skipping checks. to abort or end the sequence 
     * put "abort"or "end"in actuelStep in the code.
     * Returning false skips the steps execution. <b> risk of deadklock if no timeout is set </b>
     * @return true if sequence can continue, false if it must jump the step code
     */
    protected abstract boolean threadOverallChecks();

    /**
     * Called after the main loop
     */
    protected abstract void threadEnd();

    /**
     * Constructor
     * @param fsmName name of the sequence
     * @param startStep name of the first step of the sequence
     * @param stepsDelay default cycle delay
     */
    public FSM(String fsmName, String startStep, int stepsDelay) {
        super();
        this.stepsDelay = stepsDelay;
        this.startStep = startStep;
        this.setName(fsmName);
    }

    /**
     * Sequence aborted
     * @return true if the sequence has been aborted
     */
    public boolean isAborted() {
        return aborted;
    }

    /**
     * default cycle delay. 
     * if can be override by the cylecTime property of the Step object
     * @param stepsDelay milliseconds of sleep
     */
    public void setStepsDelay(int stepsDelay) {
        this.stepsDelay = stepsDelay;
    }

    /**
     * Name of the step to start from the sequence
     * @param startStep name of the step (use <i>step</i>.getName() to reduce mistakes)
     */
    public void setStartStep(String startStep) {
        this.startStep = startStep;
    }

    /**
     * Name of the sequence (Finite States Machine)
     * @param name Name of the sequence
     */
    public void setName(String name) {
        fsmName = name;
    }

    public String getName() {
        return fsmName;
    }

    /**
     * FSMStep actually being executed
     * @return name of step
     */
    public String getActualStep() {
        return actualStep;
    }

    /**
     * First step run in the sequence
     * @return name of the first step
     */
    public String getStartStep() {
        return startStep;
    }

    /**
     * Start the execution of the sequence
     */
    public void start(){
        if (!this.run){
            fsmThread = new Thread(this, getName());
            fsmThread.start();
        }
    }

    /**
     * Stops the sequence
     */
    public void stop(){
        this.run = false;
        status = 0;
    }

    /**
     * Sequence main loop
     */
    @Override
    public void run() {
        // initialize fsm variables
        // reset aborted flag
        aborted = false;
        // set actual status to 1
        status = 1;
        // execute setup of thread
        // set the start step name
        actualStep = startStep;
        // run the setup function for the sequence
        threadSetup();
        // reset all the steps
        steps.forEach(new BiConsumer<String, FSMStep>() {
                          @Override
                          public void accept(String t, FSMStep u) {
                              u.reset();
                          }
                      }
        );
        // initialization completed
        // set run to true
        run = true;
        // enter in loop
        while (run){
            // check that there are steps to execute
            if (steps.size() == 0){
                status = -1;
                actualStep = ABORT;
            }
            // check that the actual step exists
            if (!steps.containsKey(actualStep)){
                status = -2;
                actualStep = ABORT;
            }
            // execute the overall checks 
            if (!threadOverallChecks() && !actualStep.equals(ABORT)) {
                try {
                    // if the step has a custom delayTime use is otherwise use the FSM default
                    Thread.sleep(stepsDelay);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FSM.class.getName()).log(Level.SEVERE, null, ex);
                }
                continue;
            }

            if (!actualStep.equals(END) && !actualStep.equals(ABORT)){
                //System.out.println("[FSM] FSMStep running: " + steps.get(actualStep).getName());
                // Run the the actual step and check if any advance condition is verified
                stepChanged = false; // memory used to force fsm cycle time on changing step

                if (steps.get(actualStep).runStep()){
                    stepChanged = true;
                    // if the advance conditions are met
                    // save the actual step
                    String memory = actualStep;
                    // get the name of the next step and reset the internal variables of the step
                    actualStep = steps.get(actualStep).getNextAndReset();
                    // if the name of the step is not "end" set the actual step as last step and go on
                    if (!actualStep.equals(END) && !actualStep.equals(ABORT)){
                        if (actualStep.equals("") || actualStep.equals(null) ||
                                !steps.containsKey(actualStep)
                            ){
                            actualStep=ABORT;
                            status = -2;
                        }
                    }

                }
            }

            // if the actual step name is not "end" wait the cycle delay time
            if (!actualStep.equals(END) && !actualStep.equals(ABORT)){
                try {
                    // if the step has a custom delayTime use is otherwise use the FSM default
                    if (steps.get(actualStep).getCycleTime() > 0 && !stepChanged){
                        Thread.sleep(steps.get(actualStep).getCycleTime());
                    } else {
                        Thread.sleep(stepsDelay);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(FSM.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (actualStep.equals(END)){
                // if the actual step is "end" set the status to 2 and exit loop
                status = 2;
                break;
            } else {
                // abort requested
                aborted = true;
                break;
            }

        }
        // reset all the steps
        steps.forEach(new BiConsumer<String, FSMStep>() {
                          @Override
                          public void accept(String t, FSMStep u) {
                              u.reset();
                          }
                      }
        );
        // if status is not 2 (clean exit) set the status to -3 (forced)
        if(status == 1) status = -3;
        threadEnd();
        // reset the run variable before exiting
        run = false;
    }

    /**
     * Returns the loop state
     * @return true = running/false = idle
     */
    public boolean isRunning() {
        return run;
    }

    /**
     * Returns the status of the sequence
     * @return 0 = never run 
     *        -1 = no steps to be executed
     *        -2 = error wrong step name
     *        -3 = forced stop
     *         1 = running
     *         2 = clean exit

     */
    public int getStatus() {
        return status;
    }

    public void add(FSMStep step){
        addStep(step);
    }

    /**
     * Add a step to the sequence
     * @param step instance of FSMStep object
     */
    public void addStep(FSMStep step){
        this.steps.put(step.getName(), step);
    }

    /**
     * Returns the requested step object
     * @param name name of the step
     * @return FSMStep object or null if not found
     */
    public FSMStep getStep(String name){
        if (steps.containsKey(name)){
            return steps.get(name);
        }
        return null;
    }

    /**
     * Returns the actual state in a human readable faorm
     * @param code status code of FSM
     * @return String containing a description of the status
     */
    public static String statusString(int code){
        switch(code){
            case 0: return "never run";
            case -1: return "No steps in sequence";
            case -2: return "Wrong step name";
            case -3: return "Forced stop";
            case 1: return "Running";
            case 2: return "Clean exit";
        }
        return "invalid code";
    }
}