package com.eugenkiss.conexp2.gui;

import javax.swing.JLabel;

import com.eugenkiss.conexp2.ProgramState;

public class ContextView extends View {

    private static final long serialVersionUID = 1660117627650529212L;

    public ContextView(ProgramState state) {
        super(state);
        view = new JLabel("Hier Tabelle");
        settings = new JLabel("Context Settings");
        super.init();
    }

}
