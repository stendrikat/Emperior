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


import java.io.InputStreamReader;

import javax.swing.SwingUtilities;

class ProcessOutput extends Thread
{

    private InputStreamReader output;

    private boolean processRunning;

    private ConsolePane cta;

    private StringBuffer data;
    private Process p;

    public ProcessOutput(Process p, ConsolePane cta)
    {
        this.p = p;
    	setDaemon(true);
        output = new InputStreamReader(p.getInputStream());
        this.cta = cta;
        processRunning = true;
        data = new StringBuffer();
    }

    public void run()
    {
        try
        {
            /*
             * Loop as long as there is output from the process to be displayed or as long as the
             * process is still running even if there is presently no output.
             */
            while (output.ready() || processRunning)
            {

                // If there is output get it and display it.
                if (output.ready())
                {
                    char[] array = new char[255];
                    int num = output.read(array);
                    if (num != -1)
                    {
                        String s = new String(array, 0, num);
                        data.append(s);
                        SwingUtilities.invokeAndWait(new ConsoleWrite(cta, s));
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Problem writing to standard output.");
            System.err.println(e);
        }
    }

    public void done()
    {
        processRunning = false;
        if(p.exitValue() == 1){
        	MainFrame.displayCompileInfoInConsole();
        }        	
    }

    public String getData()
    {
        return data.toString();
    }
}