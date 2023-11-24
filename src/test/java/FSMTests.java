import fsm.FSM;
import fsm.components.FSMCondition;
import fsm.components.FSMStep;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FSMTests {
    boolean threadSetupRun;
    boolean threadOverallChecksRun;
    boolean threadEndRun;
    int counter = 0;
    long delaymemory;

    private void initStatusVariables(){
        threadSetupRun = false;
        threadOverallChecksRun = false;
        threadEndRun = false;
        counter = 0;
    }

    @Test
    void fsmInstantiation(){
        initStatusVariables();
        System.out.println("[TEST] fsmInstantiation");
        FSM fsm = new FSM("test", "firstStep", 100) {
            @Override
            protected void threadSetup() {

            }

            @Override
            protected boolean threadOverallChecks() {
                return true;
            }

            @Override
            protected void threadEnd() {
            }
        };
        Assertions.assertNotNull(fsm);
    }

    @Test
    void fsmFunctionsCalled(){
        initStatusVariables();
        System.out.println("[TEST] fsmFunctionsCalled");
        FSM fsm = new FSM("test", "firstStep", 100) {
            @Override
            protected void threadSetup() {
                threadSetupRun = true;
                addStep(new FSMStep("firstStep", "First step") {
                    @Override
                    public void stepRunCode() {
                    }

                    @Override
                    public void resetCode() {
                    }
                });
                getStep("firstStep").addAdvanceCondition(
                        new FSMCondition("completed", "complete the first step", FSM.END) {
                            @Override
                            protected boolean compare() {
                                return true;
                            }
                        }
                );
            }

            @Override
            protected boolean threadOverallChecks() {
                threadOverallChecksRun = true;
                return true;
            }

            @Override
            protected void threadEnd() {
                threadEndRun = true;
            }
        };
        Assertions.assertNotNull(fsm);
        fsm.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(threadSetupRun);
        Assertions.assertTrue(threadOverallChecksRun);
        Assertions.assertTrue(threadEndRun);
    }

    @Test
    void fsmRun(){
        initStatusVariables();
        System.out.println("[TEST] fsmRun");
        FSM fsm = new FSM("test", "firstStep", 100) {
            @Override
            protected void threadSetup() {
                threadSetupRun = true;
                addStep(new FSMStep("firstStep", "First step") {
                    @Override
                    public void stepRunCode() {
                        System.out.println("[RunCode] IN: Counter = " + counter);
                        counter++;
                        System.out.println("[RunCode] OUT: Counter = " + counter);
                    }

                    @Override
                    public void resetCode() {
                        System.out.println("[ResetCode] IN: Counter = " + counter);
                        System.out.println("[ResetCode] called by " + Thread.currentThread().getStackTrace()[3].getFileName());
                        System.out.println("[ResetCode]   method: " + Thread.currentThread().getStackTrace()[3].getMethodName());
                        System.out.println("[ResetCode]     line: " + Thread.currentThread().getStackTrace()[3].getLineNumber());
                        counter++;
                        System.out.println("[ResetCode] OUT: Counter = " + counter);
                    }
                });
                getStep("firstStep").addAdvanceCondition(
                        new FSMCondition("completed", "complete the first step", FSM.END) {
                            @Override
                            protected boolean compare() {
                                return true;
                            }
                        }
                );
            }

            @Override
            protected boolean threadOverallChecks() {
                threadOverallChecksRun = true;
                return true;
            }

            @Override
            protected void threadEnd() {
                threadEndRun = true;
            }
        };
        Assertions.assertNotNull(fsm);

        fsm.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        delaymemory = System.currentTimeMillis();
        while (fsm.isRunning()){
            if (System.currentTimeMillis() - delaymemory > 1000){
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Assertions.assertEquals(4, counter);
    }

    @Test
    void fsmCheckMoreSteps(){
        initStatusVariables();
        System.out.println("[TEST] fsmCheckMoreSteps");
        FSM fsm = new FSM("test", "firstStep", 100) {
            @Override
            protected void threadSetup() {
                addStep(new FSMStep("firstStep", "First step") {
                    @Override
                    public void stepRunCode() {
                        System.out.println("[RunCode] Step = " + getName());
                        System.out.println("[RunCode] IN: Counter = " + counter);
                        counter++;
                        System.out.println("[RunCode] OUT: Counter = " + counter);
                    }

                    @Override
                    public void resetCode() {
                    }
                });
                getStep("firstStep").addAdvanceCondition(
                        new FSMCondition("completed", "complete the first step", "secondStep") {
                            @Override
                            protected boolean compare() {
                                return true;
                            }
                        }
                );
                addStep(new FSMStep("secondStep", "Second step") {
                    @Override
                    public void stepRunCode() {
                        System.out.println("[RunCode] Step = " + getName());
                        System.out.println("[RunCode] IN: Counter = " + counter);
                        counter++;
                        System.out.println("[RunCode] OUT: Counter = " + counter);
                    }

                    @Override
                    public void resetCode() {
                    }
                });
                getStep("secondStep").addAdvanceCondition(
                        new FSMCondition("completed", "complete the first step", FSM.END) {
                            @Override
                            protected boolean compare() {
                                return true;
                            }
                        }
                );
            }

            @Override
            protected boolean threadOverallChecks() {
                return true;
            }

            @Override
            protected void threadEnd() {
            }
        };
        Assertions.assertNotNull(fsm);

        fsm.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        delaymemory = System.currentTimeMillis();
        while (fsm.isRunning()){
            if (System.currentTimeMillis() - delaymemory > 1000){
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Assertions.assertEquals(2, counter);
    }
    @Test
    void fsmMissingStep(){
        initStatusVariables();
        System.out.println("[TEST] fsmMissingStep");
        FSM fsm = new FSM("test", "firstStep", 100) {
            @Override
            protected void threadSetup() {
                addStep(new FSMStep("firstStep", "First step") {
                    @Override
                    public void stepRunCode() {
                        System.out.println("[RunCode] Step = " + getName());
                        System.out.println("[RunCode] IN: Counter = " + counter);
                        counter++;
                        System.out.println("[RunCode] OUT: Counter = " + counter);
                    }

                    @Override
                    public void resetCode() {
                    }
                });
                getStep("firstStep").addAdvanceCondition(
                        new FSMCondition("completed", "complete the first step", "secondStep") {
                            @Override
                            protected boolean compare() {
                                return true;
                            }
                        }
                );
                addStep(new FSMStep("secondStep", "Second step") {
                    @Override
                    public void stepRunCode() {
                        System.out.println("[RunCode] Step = " + getName());
                        System.out.println("[RunCode] IN: Counter = " + counter);
                        counter++;
                        System.out.println("[RunCode] OUT: Counter = " + counter);
                    }

                    @Override
                    public void resetCode() {
                    }
                });
                getStep("secondStep").addAdvanceCondition(
                        new FSMCondition("completed", "complete the first step", "FSM.END") {
                            @Override
                            protected boolean compare() {
                                return true;
                            }
                        }
                );
            }

            @Override
            protected boolean threadOverallChecks() {
                return true;
            }

            @Override
            protected void threadEnd() {
            }
        };
        Assertions.assertNotNull(fsm);

        fsm.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        delaymemory = System.currentTimeMillis();
        while (fsm.isRunning()){
            if (System.currentTimeMillis() - delaymemory > 1000){
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Assertions.assertEquals(-2, fsm.getStatus());
    }

@Test
    void fsmForcedJump(){
        initStatusVariables();
        System.out.println("[TEST] fsmForcedJump");

         FSM fsm = new FSM("test", "firstStep", 100) {
            @Override
            protected void threadSetup() {
                addStep(new FSMStep("firstStep", "First step") {
                    boolean oneshot = true;
                    @Override
                    public void stepRunCode() {
                        if (oneshot) {
                            oneshot = false;
                            System.out.println("[RunCode] Step = " + getName());
                            System.out.println("[RunCode] IN: Counter = " + counter);
                            counter++;
                            System.out.println("[RunCode] OUT: Counter = " + counter);
                        }
                    }

                    @Override
                    public void resetCode() {
                    }
                });
                getStep("firstStep").addAdvanceCondition(
                        new FSMCondition("completed", "complete the first step", "secondStep") {
                            @Override
                            protected boolean compare() {
                                return false;
                            }
                        }
                );
                addStep(new FSMStep("secondStep", "Second step") {
                    @Override
                    public void stepRunCode() {
                        System.out.println("[RunCode] Step = " + getName());
                        System.out.println("[RunCode] IN: Counter = " + counter);
                        counter++;
                        System.out.println("[RunCode] OUT: Counter = " + counter);
                    }

                    @Override
                    public void resetCode() {
                    }
                });
                getStep("secondStep").addAdvanceCondition(
                        new FSMCondition("completed", "complete the first step", "FSM.END") {
                            @Override
                            protected boolean compare() {
                                return true;
                            }
                        }
                );
            }

            @Override
            protected boolean threadOverallChecks() {
                return true;
            }

            @Override
            protected void threadEnd() {
            }
        };
        Assertions.assertNotNull(fsm);

        fsm.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        delaymemory = System.currentTimeMillis();
        boolean oneshot = true;
        while (fsm.isRunning()){
            if (System.currentTimeMillis() - delaymemory > 1000 && oneshot){
                oneshot = false;
                fsm.getStep(fsm.getActualStep()).getAdvanceConditions().get("completed").setJumpNext();
                System.out.println("[TEST] fired setJumpNext on condition");
            }
            if (System.currentTimeMillis() - delaymemory > 2000){
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Assertions.assertEquals(2, counter);
    }


}
