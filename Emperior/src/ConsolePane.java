/*
 * Emperior
 * Copyright 2010 and beyond, Marvin Steinberg.
 *
 * caps is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class ConsolePane extends JPanel {

	private static final long serialVersionUID = 1L;
	private static JTextPane console;
	/**
	 * This is the default constructor
	 */
	public ConsolePane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setLayout(new BorderLayout());
		 
		JScrollPane scrollpane = new JScrollPane();
		console = new JTextPane();
		scrollpane.getViewport().add(console);
	    add(BorderLayout.CENTER, scrollpane);
		 
	}
	
	public static void setText(String input){
		console.setText(input);
	}
	
	public static void addText(String input){
		console.setText(console.getText() + "\n" +input);
	}
	
	
	 @Override
	 public Dimension getMinimumSize() {
	     return new Dimension(200, 200);
	   }

	   @Override
	 public Dimension getPreferredSize() {
	     return new Dimension(200, 200);
	   }

}
