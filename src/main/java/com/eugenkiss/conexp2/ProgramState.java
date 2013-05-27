package com.eugenkiss.conexp2;

import com.eugenkiss.conexp2.model.FormalContext;

/**
 * Contains context, lattice, implications, filePath, snapshots etc.
 * <p>
 * Why 'ProgramState'? "Dependency Injection", e.g. for testing purposes a component can be passed
 * a "MockProgramState" very easily and it is better to have a central place for the program's
 * state as opposed to have it scattered throughout different classes.
 * If you want you can see this class as the "Model" in an MVC context.
 *
 */
public class ProgramState {

    public String filePath;
    public FormalContext context;

}
