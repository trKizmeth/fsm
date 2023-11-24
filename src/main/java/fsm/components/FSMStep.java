package fsm.components;

import java.util.LinkedHashMap;

/**
 * FSMStep of the sequencer
 * code to be run on every step
 * @author massimilianoscaletti
 */
public abstract class FSMStep {
    String name;
    String description;
    int timeOut = -1;
    int cycleTime = -1;
    String callingStep = "";
    String timeOutStep = "";
    boolean isNotAForcedJump = true;
    String advanceTo = "";
    LinkedHashMap<String, FSMCondition> advanceConditions = new LinkedHashMap<>();
    long startTime;

    /**
     * Constructor sets the name of the step and the description -same as name- (used fo debug and logging)
     * @param name name of the step: used by the sequencer
     */
    public FSMStep(String name) {
        super();
        this.name = name;
        this.description = name;
    }

    /**
     * Constructor sets the name of the step and the description (used fo debug and logging)
     * @param name name of the step: used by the sequencer
     * @param description description of the step for debugging/logging purposes
     */
    public FSMStep(String name, String description) {
        super();
        this.name = name;
        this.description = description;
    }

    /**
     * Code to be run during the step execution
     */
    public abstract void stepRunCode();

    /**
     * Returns the name of the step to be used in the sequence
     * @return Name of the FSMStep as to be used in the sequence
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the custom time configured for the step
     * @return the custom cycle time delay
     */
    public int getCycleTime() {
        return cycleTime;
    }

    /**
     * Set a custom cycle time for this step
     * @param cycleTime cycle time in ms
     */
    public void setCycleTime(int cycleTime) {
        this.cycleTime = cycleTime;
    }

    /**
     * Return the name of the previous step <b>executed</b> in the sequence
     * @return the neame of the previus step as used in the sequence
     */
    public String getCallingStep() {
        return callingStep;
    }

    /**
     * Set the name of the calling.
     * Useful for debug to know what step triggered this
     * @param callingStep name of the caller
     */
    public void setCallingStep(String callingStep) {
        this.callingStep = callingStep;
    }

    /**
     * checks if a forced jumo has been requested from outside the sequence
     * and reset the request
     * @return
     */
    public boolean isNotAForcedJump() {
        boolean res = isNotAForcedJump;
        isNotAForcedJump = true;
        return res;
    }

    /**
     * Set the name of the exit step in case of timeout
     * @param timeOutStep the name of the step to jump in in case of timeout
     */
    public void setTimeOutStep(String timeOutStep) {
        this.timeOutStep = timeOutStep;
    }

    /**
     * Set the timeout in milliseconds
     * @param timeOut
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * returns thr actual countdown to step timeout
     * @return the countdown i nmillisencods
     */
    public long getCountDown(){
        if (timeOut == -1){
            return -1;
        }
        long delay = System.currentTimeMillis() - startTime;
        if (delay <= 0){
            return 0;
        }
        return delay;
    }

    /**
     * Code to be executed on reset call
     */
    public abstract void resetCode();

    /**
     * reset the main step variables
     */
    public void reset(){
        resetCode();
        startTime = System.currentTimeMillis();
        advanceTo = "";

    }

    /**
     * Run the step code and return if one of the advance advanceConditions has been met
     * @return true if sequence can go on
     */
    public boolean runStep(){
        boolean advance = false;
        stepRunCode();
        // Check step timeout
        if ( this.timeOut >= 0 && System.currentTimeMillis() - this.startTime > this.timeOut){
            reset();
            this.advanceTo = timeOutStep;
        } else
        {
            // Check advance advanceConditions
            this.advanceConditions.forEach((name, condition) ->{
                    if (condition.isCompare()) {
                        if (advanceTo.equals(""))
                            advanceTo = condition.getNext();
                    }
                }
            );

        }
        advance = !(advanceTo.equals(""));
        // if step is not over do step code
        //if (!advance)
        return advance;
    }



    /**
     * add an advancement condition
     * @param condition FSMCondition object to checked for advancing in the sequence
     */
    public void addAdvanceCondition(FSMCondition condition){
        // add the condition to the list
        advanceConditions.put(condition.getName(), condition);
    }


    /**
     * Returns the name of the next step to be executed as soon as one of the
     * advancement condition is met. step is not reset after reading the property
     * @return the name of the step to jump in
     */
    public String getNext(){
        return advanceTo;
    }

    /**
     * Returns the name of the next step to be executed as soon as one of the
     * advancement condition is met and execute the step reset procedure
     * @return the name of the step to jump in
     */
    public String getNextAndReset(){
        String aTo = advanceTo;
        reset();
        return aTo;
    }

    /**
     * returns a LinkedHashMap with the list of advance conditions defined for
     * the step
     * @return LinkedHashMap<String, FSMCondition>
     */
    public LinkedHashMap<String, FSMCondition> getAdvanceConditions() {
        return advanceConditions;
    }
}
