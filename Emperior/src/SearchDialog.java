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


import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SearchDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JLabel searchLabel = null;
	private JTextField searchField = null;
	private JLabel jOpenedFilesLabel = null;
	private JButton searchButton = null;
	private JRadioButton allOpenedFilesButton = null;
	private JRadioButton allFilesButton = null;
	private ButtonGroup buttonGroup = new ButtonGroup();  //  @jve:decl-index=0:


	/**
	 * @param owner
	 */
	public SearchDialog(Frame owner) {
		super(owner);
		initialize();
	}

	public SearchDialog(Frame owner, String headerText) {
		super(owner, headerText);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		Dimension dialogSize = new Dimension(378, 164);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// Position des JFrames errechnen
		int top = (screenSize.height - dialogSize.height) / 2;
		int left = (screenSize.width - dialogSize.width) / 2;

		// Größe zuordnen
		this.setSize(dialogSize);

		// Position zuordnen
		this.setLocation(left, top);

		this.setVisible(true);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getSearchLabel(), null);
			jContentPane.add(getSearchField(), null);
			jContentPane.add(getJOpenedFilesLabel(), null);
			jContentPane.add(getSearchButton(), null);
			jContentPane.add(getAllOpenedFilesButton(), null);
			jContentPane.add(getAllFilesButton(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes searchLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getSearchLabel() {
		if (searchLabel == null) {
			searchLabel = new JLabel();
			searchLabel.setText("Search for:");
			searchLabel.setBounds(new Rectangle(29, 14, 63, 16));
		}

		return searchLabel;
	}

	/**
	 * This method initializes searchField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getSearchField() {
		if (searchField == null) {
			searchField = new JTextField();
			searchField.setSize(241, 20);
			searchField.setLocation(new Point(104, 12));
		}
		return searchField;
	}

	/**
	 * This method initializes jOpenedFilesLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getJOpenedFilesLabel() {
		if (jOpenedFilesLabel == null) {
			jOpenedFilesLabel = new JLabel();
			jOpenedFilesLabel.setBounds(new Rectangle(30, 47, 77, 16));
			jOpenedFilesLabel.setText("Search In:");
		}
		return jOpenedFilesLabel;
	}

	/**
	 * This method initializes searchButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSearchButton() {
		if (searchButton == null) {
			searchButton = new JButton();
			searchButton.setBounds(new Rectangle(137, 90, 102, 22));
			searchButton.setText("Search");
		}
		
		searchButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				search();
				
			}
			
		});
		
		return searchButton;
	}

	protected void search() {
		String searchString = searchField.getText();
		if(!searchString.equals("")){
			// TODO all opened, all files search
			searchIn(searchString);
		}
	}

	private void searchIn(String searchString) {
		if(allOpenedFilesButton.isSelected()){
			Main.addLineToLogFile("[Search] all opened files: " + searchString);
			searchInAllOpenedFiles(searchString);
		}
		else if(allFilesButton.isSelected()){
			Main.addLineToLogFile("[Search] all files: " + searchString);
			searchInAllFiles(searchString);
		}
		else
		{
			Main.addLineToLogFile("[Search] selected file: " + searchString);
			searchInSelectedFile(searchString);
		}
		
	}

	private void searchInAllFiles(String searchString) {
		ArrayList<String> availableFiles = MainFrame.jTree.getAvailableFiles();
		if(availableFiles.size() > 0){
			HashMap<Integer, String> foundPlaces = new HashMap<Integer, String>();
			MainFrame.consolePane.setText("Search Result:");
			for (String filePath: availableFiles) {
				foundPlaces = Main.searchInFile(filePath, searchString);
				searchInAction(foundPlaces, filePath);
			}
		}
		
	}

	private void searchInAllOpenedFiles(String searchString) {
		if(MainFrame.jTabbedPane.getTabCount() > 0){
			HashMap<Integer, String> foundPlaces = new HashMap<Integer, String>();
			MainFrame.consolePane.setText("Search Result:");
			for (String filePath: MainFrame.openedFiles.keySet()) {
				foundPlaces = Main.searchInFile(filePath, searchString);
				searchInAction(foundPlaces, filePath);
			} 
		}
		
	}

	private void searchInSelectedFile(String searchString) {
		if (MainFrame.jTabbedPane.getTabCount() > 0) {
			int selectedTab = MainFrame.jTabbedPane.getSelectedIndex();
			String filePath = MainFrame.jTabbedPane.getToolTipTextAt(selectedTab);
			HashMap<Integer, String> foundPlaces = new HashMap<Integer, String>();
			MainFrame.consolePane.setText("Search result:");
			foundPlaces = Main.searchInFile(filePath, searchString);
			searchInAction(foundPlaces, filePath);
		}
	}
	
	private void searchInAction(HashMap<Integer, String> foundPlaces, String filePath){
		if(foundPlaces.size() > 0){
			Main.addLineToLogFile("[Search] found in: " + filePath);
			MainFrame.consolePane.addText("\nFound Places in " + filePath + ":");
			TreeSet<Integer> lineNumbers = Main.generateSortedSet(foundPlaces.keySet());
			for (int line : lineNumbers) {
				MainFrame.consolePane.addText(line+": "+foundPlaces.get(line));
			}
		}
	}

	/**
	 * This method initializes allOpenedFilesButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getAllOpenedFilesButton() {
		if (allOpenedFilesButton == null) {
			allOpenedFilesButton = new JRadioButton();
			allOpenedFilesButton.setBounds(new Rectangle(104, 47, 125, 21));
			allOpenedFilesButton.setText("All opened Files");
			buttonGroup.add(allOpenedFilesButton);
		}
		return allOpenedFilesButton;
	}

	/**
	 * This method initializes allFilesButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getAllFilesButton() {
		if (allFilesButton == null) {
			allFilesButton = new JRadioButton();
			allFilesButton.setBounds(new Rectangle(236, 47, 90, 21));
			allFilesButton.setText("All Files");
			buttonGroup.add(allFilesButton);
		}
		return allFilesButton;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
