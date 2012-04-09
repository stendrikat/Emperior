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


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.swing.JEditorPane;
import javax.swing.WindowConstants;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
 
public class Main {

	/**
	 * @param args
	 */
	public static MainFrame mainFrame;
	public static File logFile;
	public static File logDir;
	public static HashMap<String, String> commandMap = new HashMap<String, String>();
	public static String operatingSystem;
	
	public static String applicant;
	public static int activeType;
	public static List<String> tasktypes = new ArrayList<String>();
	public static int activeTask;
	public static List<String> tasks = new ArrayList<String>();
	public static String experimentFilesFolder;
	public static List<String> manualOrder = new ArrayList<String>();
	public static int manualOrderPos = 0;
	public static boolean adminmode = false;
	
	
	
	public static void main(String[] args) throws Exception {
		
		mainFrame = new MainFrame();
		operatingSystem = System.getProperty("os.name");
		
		CommandLineParser parser = new BasicParser();
		Options options = new Options();
		options.addOption("h", "help", false, "Print this usage information");
		options.addOption("t", "test", true,
						"Name of the Bat-File which is executed for Compilation and Unit-Testing");
		options.addOption("r", "run", true,
		"Name of the Bat-File which is executed for Compilation and running the project");
		options.addOption("f", "folder", true,
		"Name of the Folder in which the exercises for the Experiment are stored");
		
		CommandLine commandLine = parser.parse(options, args);
		// read from command line
		if(commandLine.getOptions().length > 0){
			readCommandLine(commandLine, options);
		}
		// read from property file
		else{
			readPropertyFile();
		}
		
		initLogging();
		
		checkBatFile();
		
		

		mainFrame.init();     
		
		mainFrame.setSize(800, 600);
		mainFrame.setVisible(true);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				addLineToLogFile("[Close] Emperior");
				updateResumeTask();
				System.exit(0);
			}
		});
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		addLineToLogFile("[Start] Emperior");
		//makeContiniousCopys();

	}
	
	private static void checkBatFile() throws Exception {
		File batFile = new File(mainFrame.getBatFilePath());
		if(!batFile.exists())		
			throw new Exception("Bat file does not exist ("+batFile.getAbsolutePath()+")");
		if(!batFile.canExecute())
			throw new Exception("[Linux] Bat file can not be executed - set it as application ("+batFile.getAbsolutePath()+")");
	}

	private static void readPropertyFile() throws Exception {
		Properties properties = new Properties(); 
		try { 
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream("Emperior.properties"));
			properties.load(stream);
			stream.close();
			
			String[] manualOrder_arr_source = null;
			
			if(properties.getProperty("test") != null)
				commandMap.put("test", properties.getProperty("test"));
			if(properties.getProperty("run") != null)
				commandMap.put("run", properties.getProperty("run"));
			if(properties.getProperty("folder") != null)
				commandMap.put("folder", properties.getProperty("folder"));
			
			if(properties.getProperty("applicant") != null)
				applicant = properties.getProperty("applicant");
			if(properties.getProperty("tasktypes") != null){
				String[] tempArray = properties.getProperty("tasktypes").split(",");
				
				for(String s : tempArray){
					tasktypes.add(s);
				}
			}
			if(properties.getProperty("startwithtype") != null)
				activeType = Integer.parseInt(properties.getProperty("startwithtype"));
			if(properties.getProperty("manualorder") != null){
				String source = properties.getProperty("manualorder");
				manualOrder_arr_source = source.split(",");
			}
			
			String resumetask = "";
			
			if(properties.getProperty("resumetask") != null){
				resumetask = properties.getProperty("resumetask");
			}
			
			if(properties.getProperty("adminmode") != null){
				adminmode = properties.getProperty("adminmode").equals("1");
			}
			
			handleCommands();
			
			if(manualOrder_arr_source != null){
				boolean correctFormat = true;
				
				for(String s: manualOrder_arr_source){
					correctFormat = checkManualOrderFormat(s);
					
					if(!correctFormat)
						break;
				}
				
				if(correctFormat){
					for(String s: manualOrder_arr_source){
						manualOrder.add(s);
					}
				}
				
				if(!adminmode){
					if(tasks.size() != 0){
					
						if(manualOrder != null && manualOrder.size() != 0){
						
							String[] name_parts;
						
							String firsttask = "";
						
							if(resumetask.trim().length() != 0){
								//System.out.println(resumetask);
								name_parts = resumetask.split("_");
								firsttask = resumetask;
							
								manualOrderPos = manualOrder.indexOf(resumetask);
							}else{
								name_parts = manualOrder.get(0).split("_");
								firsttask = manualOrder.get(0);
								manualOrderPos = 0;
							}
						
						
							activeTask = tasks.indexOf(name_parts[1]);
							activeType = tasktypes.indexOf(name_parts[0]);
						
							//System.out.println(activeTask + " " + activeType);
						
							mainFrame.setExperimentFilesFolderPath(experimentFilesFolder + File.separator + firsttask);
						}else{
						
							String[] name_parts;
						
							if(resumetask.trim().length() != 0){
								name_parts = resumetask.split("_");
								activeTask = tasks.indexOf(name_parts[1]);
								activeType = tasktypes.indexOf(name_parts[0]);
							}else{
								activeTask = 0;
							}
						
						
							String newTask =  tasktypes.get(activeType) + "_" + tasks.get(activeTask);
							mainFrame.setExperimentFilesFolderPath(experimentFilesFolder + File.separator + newTask);
						}
					}
				}else{
					mainFrame.setExperimentFilesFolderPath(experimentFilesFolder);
				}
			}
			

		} 
		catch (IOException e) {} 
		
	}
	
	public static void handleCommands() throws Exception{
		if(commandMap.get("test") != null){
			String batFileName = commandMap.get("test");
			if(existsInFileSystem(batFileName, true))
				mainFrame.setBatFilePath(batFileName);
			else
				throw new Exception(batFileName + " is not a file");
			
		}
		
		if(commandMap.get("run") != null){
			String runBatFileName = commandMap.get("run");
			if(existsInFileSystem(runBatFileName, true))
				mainFrame.setRunBatFilePath(runBatFileName);
			else
				throw new Exception(runBatFileName + " is not a file");
			
		}
		
		if(commandMap.get("folder") != null){
			experimentFilesFolder = commandMap.get("folder");
			if(existsInFileSystem(experimentFilesFolder, true)){
				
				discoverTasks(experimentFilesFolder);
				
				
			}else
				throw new Exception(experimentFilesFolder + " is not a folder");
		}
		
		
	}

	public static void readCommandLine(CommandLine commandLine, Options options) throws Exception{
		if (commandLine.hasOption('h')) {
			HelpFormatter f = new HelpFormatter();
            f.printHelp("OptionsTip", options);
			System.exit(0);
		}
		if (commandLine.hasOption('t')) {
			commandMap.put("test", commandLine.getOptionValue('t'));
		}
		if (commandLine.hasOption('r')) {
			commandMap.put("run", commandLine.getOptionValue('r'));
		}
		if (commandLine.hasOption('f')) {
			commandMap.put("folder", commandLine.getOptionValue('f'));
		}
		handleCommands();
	}

	
	public static File getTargetLocation(){
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
		String dateStr = format.format(new Date(System.currentTimeMillis()));
		return new File(logDir + File.separator + dateStr);
	}
	
	public static void removeTabbedPaneIcon(){
		final Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				int index = MainFrame.jTabbedPane.getSelectedIndex();
				if(MainFrame.jTabbedPane.getIconAt(index) != null)
				{
					MainFrame.jTabbedPane.setIconAt(index, null);
					timer.cancel();
				}
			}
		};
		timer.schedule(task, 100, 60000);

	}
	
	private static boolean existsInFileSystem(String filePath, boolean isDirectory) {
		File f = new File(filePath);
		if(f.exists())
		{
			if(isDirectory){
				if(f.isDirectory())
					return true;
			}
			else{
				if(f.isFile())
					return true;
			}
		}
			
		return false;
	}

	public static String readInFile(String filePath) {
		File f = new File(filePath);
		
		BufferedReader test;
		String fileContent = "";
		try {
			test = new BufferedReader(new FileReader(f));
			String input = "";
			while ((input = test.readLine()) != null) {
				fileContent += input + "\n";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileContent;
	}

	public static HashMap<Integer, String> searchInFile(String filePath,
			String searchString) {
		HashMap<Integer, String> foundPlaces = new HashMap<Integer, String>();
		File f = new File(filePath);
		BufferedReader test;
		try {
			test = new BufferedReader(new FileReader(f));
			String input = "";
			int line = 1;
			while ((input = test.readLine()) != null) {
				if (input.contains(searchString))
					foundPlaces.put(line, input.trim());
				line++;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return foundPlaces;
	}

	public static String getFileExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1, fileName
				.length());
	}

	private static String getCurrentTime() {
		Date date = new Date();
		// Festlegung des Formats:
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd.MM.yyyy HH:mm:ss.S");
		return dateFormat.format(date);
	}

	public static void addLineToLogFile(String input) {
		if(!adminmode){
			FileWriter fstream;
			try {
				fstream = new FileWriter(logFile, true);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(getCurrentTime() + " {" + applicant + "} " + "<" + Main.tasks.get(Main.activeTask) + "_" + Main.tasktypes.get(Main.activeType) + "> " + input);
				out.newLine();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static TreeSet<Integer> generateSortedSet(Set<Integer> lineNumbers) {
		TreeSet<Integer> treeSet = new TreeSet<Integer>();
		for (Integer lineNumber : lineNumbers) {
			treeSet.add(lineNumber);
		}
		return treeSet;
	}

	
	public static void initLogging() {
		if(!adminmode){
			logDir = new File("logs");
			if(!logDir.exists())
				logDir.mkdir();
			
			logDir = new File(logDir + File.separator + Main.applicant);
			
			if(!logDir.exists())
				logDir.mkdir();
			
			logDir = new File(logDir + File.separator + Main.tasktypes.get(Main.activeType) + "_" + Main.tasks.get(Main.activeTask));
			
			if(!logDir.exists())
				logDir.mkdir();
			
			logFile = new File(logDir + File.separator + "experiment.log");
			File compilerOutputFile = new File(logDir + File.separator + "compileOutput.log");
			try {
				if(!compilerOutputFile.exists())
					compilerOutputFile.createNewFile();
				
				if (!logFile.exists())
					logFile.createNewFile();
				else {
					double fileSizeInMB = (double) logFile.length() / (1024 * 1024);
					if (fileSizeInMB >= 1) {
						File newFile = new File(System.currentTimeMillis() + "_"
								+ logFile.getName());
						System.out.println(newFile.getAbsolutePath());
						logFile.renameTo(newFile);
						logFile = new File(logDir + File.separator + "experiment.log");
						logFile.createNewFile();

					}

				}
				
				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void backupCompleteProject(){
		File mainDir = mainFrame.getFileTree().getMainDir();
		try {
			copyDirectory(mainDir, getTargetLocation());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void makeContiniousCopys() {
		new Thread(){
			public void run(){
				final Timer timer = new Timer();
				TimerTask task = new TimerTask() {
					@Override
					public void run() {
						if(mainFrame.actionPerformed)
						{
							System.out.println("Mache nun Kopie vom aktuellen File");
							int selection = mainFrame.jTabbedPane.getSelectedIndex();
							String filePath = mainFrame.jTabbedPane.getToolTipTextAt(selection);
							
							try {
								copyFile(filePath, getTargetLocation());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						MainFrame.actionPerformed = false;
					}
				};
				timer.schedule(task, 0, 3000);
			}
			
		}.start();
		

	}

	public static void copyFile(String filePath, File targetLocation) throws Exception {
		File f = new File(filePath);
		if(f.isFile()){
			
			targetLocation.mkdirs();
			
			System.out.println(filePath);
			
//			String [] pathElements = filePath.split("/");
//			String [] pathElements = filePath.split("\\\\|/");
			String [] pathElements = filePath.split("\\\\");
			
//			if(pathElements.length > 0){
//				System.out.println("[test] " + targetLocation + File.separator + filePath.substring(0, filePath.lastIndexOf(System.getProperty("file.separator"))));
//				targetLocation = new File(targetLocation + File.separator + filePath.substring(0, filePath.lastIndexOf(System.getProperty("file.separator"))));
//				targetLocation.mkdirs();
//			}
			
			JEditorPane editor = mainFrame.editors.get(filePath);
			
			String copypath = targetLocation + File.separator + pathElements[pathElements.length-1];
			
			System.out.println("[test] " + copypath);
			
			Main.addLineToLogFile("[File] saving to: " + copypath);
			
			FileWriter fstream = new FileWriter(copypath);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(editor.getText());
			// Close the output stream
			out.close();
			
			
		}
		
	}
	
	public static void copyFiles(Set<String> openedFilesNames,
			File targetLocation) throws Exception {
		for (String filePath : openedFilesNames) {
			copyFile(filePath, targetLocation);
		}
		
	}

	// If targetLocation does not exist, it will be created.
	public static void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {
		System.out.println(sourceLocation.getName() + " - "
				+ targetLocation.getName());
		if (sourceLocation.isDirectory()) {
			if(FileTree.isFolderAccepted(sourceLocation.getName())){
				if (!targetLocation.exists()) {
					targetLocation.mkdirs();
				}

				String[] children = sourceLocation.list();
				for (int i = 0; i < children.length; i++) {
					copyDirectory(new File(sourceLocation, children[i]), new File(
							targetLocation, children[i]));
				}
			}
			
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			
			Main.addLineToLogFile("[File] saving to: " + targetLocation);
		}
	}

	private static void discoverTasks(String taskFolder){
		File tasksFolder = new File(taskFolder);
		
		if(tasksFolder.isDirectory()){
			for(File task : tasksFolder.listFiles()){
				String taskname = task.getName().substring(task.getName().indexOf("_") + 1);
				if(!tasks.contains(taskname))
					tasks.add(taskname);
				
			}
		}
	}
	
	private static boolean checkManualOrderFormat(String manualOrder){
		
		boolean correctTask = false;
		boolean correctType = false;
		
		if(manualOrder.split("_").length != 2)
			return false;
		
		for(String s : tasks){
			if(manualOrder.contains(s)){
				correctTask = true;
				break;
			}
		}
		
		for(String s : tasktypes){
			if(manualOrder.contains(s)){
				correctType = true;
				break;
			}
		}
		
		if(correctTask && correctType)
			return true;
		else
			return false;
		
	}
	
	private static void updateResumeTask(){
		if(!adminmode){
			Properties properties = new Properties(); 
			try { 
				BufferedInputStream stream = new BufferedInputStream(new FileInputStream("Emperior.properties"));
				properties.load(stream);
				properties.setProperty("resumetask", tasktypes.get(activeType) + "_" + tasks.get(activeTask));
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("Emperior.properties"));
				properties.store(out, "");
				out.close();
				stream.close();
			}catch(Exception e){
				
			}
		}
	}

}
