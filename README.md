# fsm
Finite state machine for java automation. 
I wrote it some years ago, for my home automation experiments, and I used it also for general automation tasks. 

Permits to define the cycle time for steps execution, globally or over a single step.

For every step is defined a runCode() function and a reset() function:
+ runCode() executed at every cycle
+ resetCode() executed at start and after every execution of runCode()

It is possible define more advance condition to jumo to different steps based on the condition defined.

On every cycle before calling runCode() a function called threadOverallChecks() is called and must return true to continue the sequence or false to suspend it.
I use it for global updates or to synchronize external communications. 

There are also a threadSetup() and threadEnd() funcions that ar called when sequence is started and at the end or when the sequence is stopped manually.

For the moment in FSMTest examples of use can be found.

I will eventually add some example in the future.
