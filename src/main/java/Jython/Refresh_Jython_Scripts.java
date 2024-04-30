/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2007 - 2024 Fiji developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package Jython;

import common.RefreshScripts;

import ij.IJ;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 * 1 - looks for python script files under the ImageJ/plugins/jython folder
 * 2 - updates the Plugins / Jython submenu, a MenuItem for each script
 * 3 - listens to that submenu MenuItem items and launches the python scripts when called
 * <p>
 * To create a shortcut to a Python plugin a macro can be done to pass appropriate arguments to the Launch_Python_Script class, or tweak ImageJ, or a thousand not-so-straighforward ways.
 * </p>
 *
 * @author Albert Cardona
 */
public class Refresh_Jython_Scripts extends RefreshScripts {

	public void run(String arg) {
		setLanguageProperties(".py", "Jython");
		setVerbose(false);
		super.run(arg);
	}

	/** Run a jython script in its own separate interpreter and namespace. */
	public void runScript(String path) {
		try {
			// runScript(InputStream) will close the stream
			runScript(new BufferedInputStream(new FileInputStream(new File(path))));
		} catch (Throwable t) {
			printError(t);
		}
	}

	/** Will consume and close the stream. */
	public void runScript(InputStream istream) {
		Thread.currentThread().setContextClassLoader(IJ.getClassLoader());
		PythonInterpreter PI = null;
		try {
			ClassLoader classLoader = IJ.getClassLoader();
			if (classLoader == null)
				classLoader = getClass().getClassLoader();
			PySystemState.initialize(System.getProperties(), System.getProperties(), new String[] { }, classLoader);
			PySystemState pystate = new PySystemState();
			pystate.setClassLoader(classLoader);
			PI = new PythonInterpreter(new PyDictionary(), pystate);
			PI.setOut(this.out);
			PI.setErr(this.err);
			new Jython_Interpreter(PI).importAll();
			PI.execfile(istream);
		} catch (PyException e) {
			try {
				err.write(e.toString().getBytes());
			} catch (Exception e2) {
				e.printStackTrace();
			}
		} catch (Throwable t) {
			printError(t);
		} finally {
			try {
				istream.close();
			} catch (Exception e) {
				System.out.println("Jython runScript could not close the stream!");
				e.printStackTrace();
			}
			try {
				if (null != PI) PI.cleanup();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
