/*
 * Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 1996-2002.
 * All rights reserved. Software written by Ian F. Darwin and others.
 * $Id: LICENSE,v 1.8 2004/02/09 03:33:38 ian Exp $
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Java, the Duke mascot, and all variants of Sun's Java "steaming coffee
 * cup" logo are trademarks of Sun Microsystems. Sun's, and James Gosling's,
 * pioneering role in inventing and promulgating (and standardizing) the Java 
 * language and environment is gratefully acknowledged.
 * 
 * The pioneering role of Dennis Ritchie and Bjarne Stroustrup, of AT&T, for
 * inventing predecessor languages C and C++ is also gratefully acknowledged.
 */
 
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Display a file system in a JTree view
 * 
 * @version $Id: FileTree.java,v 1.9 2004/02/23 03:39:22 ian Exp $
 * @author Ian Darwin
 */
public class FileTree extends JPanel {
	File mainDir = null;
	JTree tree;
	ArrayList<String> acceptedExtensions = new ArrayList<String>();
	ArrayList<String> availableFiles = new ArrayList<String>();
	static ArrayList<String> foldersToIgnore = new ArrayList<String>();
	private boolean enabledFileTree = true;;

	public ArrayList<String> getFoldersToIgnore() {
		return foldersToIgnore;
	}

	public void setFoldersToIgnore(ArrayList<String> foldersToIgnore) {
		this.foldersToIgnore = foldersToIgnore;
	}

	public File getMainDir() {
		return mainDir;
	}

	public void setMainDir(File mainDir) {
		this.mainDir = mainDir;
	}

	public JTree getTree() {
		return tree;
	}

	public void setTree(JTree tree) {
		this.tree = tree;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Construct a FileTree */
	public FileTree(File dir) {
		mainDir = dir;
		setAcceptedExtensions();
		setFoldersToIgnore();
		setLayout(new BorderLayout());

		// Make a tree list with all the nodes, and make it a JTree
		tree = new JTree(addNodes(null, dir));

		// Add a listener
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (enabledFileTree) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
							.getPath().getLastPathComponent();
					displayFile(node);
				}

			}
		});

		// Lastly, put the JTree into a JScrollPane.
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.getViewport().add(tree);
		add(BorderLayout.CENTER, scrollpane);
	}

	private void setFoldersToIgnore() {
		foldersToIgnore.add(".svn");
		foldersToIgnore.add("build___");

	}

	private void setAcceptedExtensions() {
		acceptedExtensions.add("java");
		acceptedExtensions.add("groovy");
	}

	public static boolean isFolderAccepted(String folder) {
		for (String f : foldersToIgnore) {
			if (folder.equalsIgnoreCase(f))
				return false;
		}
		return true;
	}

	private boolean isExtensionAccepted(String extension) {
		for (String ext : acceptedExtensions) {
			if (extension.equalsIgnoreCase(ext))
				return true;
		}
		return false;
	}

	protected void displayFile(DefaultMutableTreeNode node) {
		if (node.isLeaf() && new File(node.getParent() + File.separator + node).isFile()) {
			System.out.println("File Location: " + node.getParent() + File.separator
					+ node);
			Main.addLineToLogFile("[File] opening: " + node.getParent() + File.separator
					+ node);
			String sourceCode = Main.readInFile(node.getParent() + File.separator + node);
			MainFrame.setTabbedPanelItems(node.getParent() + File.separator + node, node
					.toString(), sourceCode);

		}

	}

	/** Add nodes from under "dir" into curTop. Highly recursive. */
	DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir) {
		if (!dir.exists()) {
			return null;
		}
		String curPath = dir.getPath();
		DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(curPath);
		if (curTop != null) { // should only be null at root
			curTop.add(curDir);
		}
		Vector<String> ol = new Vector<String>();
		String[] tmp = dir.list();
		for (int i = 0; i < tmp.length; i++)
			ol.addElement(tmp[i]);
		Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
		File f;
		Vector<String> files = new Vector<String>();
		// Make two passes, one for Dirs and one for Files. This is #1.
		for (int i = 0; i < ol.size(); i++) {
			String thisObject = ol.elementAt(i);

			String newPath;
			if (curPath.equals("."))
				newPath = thisObject;
			else
				newPath = curPath + File.separator + thisObject;
			if ((f = new File(newPath)).isDirectory()) {
				if (isFolderAccepted(thisObject)) {
					addNodes(curDir, f);
				}
			} else {
				files.addElement(thisObject);
			}
		}
		// Pass two: for files.
		for (int fnum = 0; fnum < files.size(); fnum++) {
			String fileName = files.elementAt(fnum);
			if (fileName.contains(".")) {
				String ext = Main.getFileExtension(fileName);
				if (isExtensionAccepted(ext) && !fileName.startsWith("Test___")) {
					curDir
							.add(new DefaultMutableTreeNode(files
									.elementAt(fnum)));
					availableFiles.add(curDir + File.separator + fileName);
				}
			}
		}
		return curDir;
	}

	public ArrayList<String> getAvailableFiles() {
		return availableFiles;
	}

	public void setAvailableFiles(ArrayList<String> availableFiles) {
		this.availableFiles = availableFiles;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.enabledFileTree = enabled;
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(200, 400);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(200, 400);
	}
}
