package fsm.components;

/**
 *  FSMCondition of the sequencer step
 *  condition to be met for jumping to another step
 * @author massimilianoscaletti
 */
public abstract class FSMCondition{
    protected abstract boolean compare();
    String description;
    String next;
    String name;
    boolean jumpNext = false;

    /**
     * Constructor for AdvanceCondition
     * @param name Name of the condition ( for debug and log purposes)
     * @param nextStep Name of the step to jump if condition is met. "<b>end</b>" means stop the sequence
     */
    public FSMCondition(String name, String nextStep) {
        super();
        this.setName(name);
        this.description = name;
        this.next = nextStep;

    }

    /**
     * Returns the step to jump to in case of this condition is met
     * @return name of the step
     */
    public String getNext(){
        return next;
    }

    /**
     * Set the name of the next step of this condition is met
     * @param next Name of the next step
     */
    public void setNext(String next) {
        this.next = next;
    }

    /**
     * gets the name of the condition
     * @return name of the next step
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of the condition
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * sets a description for the condition
     * useful for debug
     * @return description of the condition
     */
    public String getDescription() {
        return description;
    }

    /**
     * sets a description for the condition
     * @param description description for the condition
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     Constructor for AdvanceCondition
     * @param name Name of the condition ( for debug and log purposes)
     * @param description Description of the condition ( for debug and log purposes)
     * @param nextStep Name of the step to jump if condition is met. "<b>end</b>" means stop the sequence
     */
    public FSMCondition(String name, String description, String nextStep) {
        super();
        this.setName(name);
        this.description = description;
        this.next = nextStep;
    }

    /**
     * Force a jump to the next step
     * The jump it's executed in the isCompare() method
     */
    public void setJumpNext() {
        this.jumpNext = true;
    }

    /**
     * Checks the jump conditions as defined in the compare function
     * @return true if advancement condition is met
     */
    public boolean isCompare(){
        // check if it is requested to force the jump to next step
        if (jumpNext){
            jumpNext = false;
            return true;
        }
        return compare();
    }


}
