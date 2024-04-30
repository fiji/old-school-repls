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

import common.AbstractInterpreter;

import ij.IJ;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;

import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.ParserFacade;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 * A dynamic Jython interpreter for ImageJ.
 * It'd be nice to have TAB expand ImageJ class names and methods.
 *
 * {@code
 * $ PATH=/usr/local/jdk1.5.0_14/bin:$PATH javac -classpath .:../../ij.jar:../jython21/jython.jar Jython_Interpreter.java Refresh_Jython_List.java
 * $ jar cf Jython_Interpreter.jar *class plugins.config
 * }
 * @author Albert Cardona
 */
public class Jython_Interpreter extends AbstractInterpreter {
	protected PythonInterpreter pi;
	protected PyDictionary globals = new PyDictionary();
	protected PySystemState pystate = new PySystemState();

	public Jython_Interpreter() { }

	public Jython_Interpreter(PythonInterpreter pi) {
		this.pi = pi;
	}

	public void run(String arg) {
		super.run(arg);
		super.window.setTitle("Jython Interpreter");
		super.prompt.setEnabled(false);
		print("Starting Jython ...");
		// Create a python interpreter that can load classes from plugin jar files.
		ClassLoader classLoader = IJ.getClassLoader();
		if (classLoader == null)
			classLoader = getClass().getClassLoader();
		PySystemState.initialize(System.getProperties(), System.getProperties(), new String[] { }, classLoader);
		pystate.setClassLoader(classLoader);
		pi = new PythonInterpreter(globals, pystate);
		//redirect stdout and stderr to the screen for the interpreter
		pi.setOut(out);
		pi.setErr(out);
		//pre-import all ImageJ java classes and TrakEM2 java classes
		importAll();
		// fix back on closing
		super.window.addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					pi.setOut(System.out);
					pi.setErr(System.err);
				}
			}
		);
		super.prompt.setEnabled(true);
		super.prompt.requestFocus();
		println("... done.");
	}

	@Override
	protected void windowClosing() {
		super.windowClosing();
		try {
			if (null != pi) pi.cleanup();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/** Evaluate python code. */
	protected Object eval(String text) {
		// Ensure MacOSX and Windows work fine until the alternative is tested
		if ( ! IJ.isLinux()) {
			pi.exec(text);
			return null;
		}

		// A. Prints None
		//Py.setSystemState(pystate);
		//PyObject po = Py.runCode(Py.compile_flags(text, "<string>", CompileMode.exec, Py.getCompilerFlags(0, false)), pi.getLocals(), globals);
		//Py.flushLine();
		//return po;

		// B. Prints None
		//return pi.eval(Py.compile_flags(text, "<string>", CompileMode.exec, Py.getCompilerFlags(0, false)));

		// C. Works! Prints to stdout the last evaluated expression if it is meaningful
		// (for example def something doesn't print, but a single number does.)
		CompilerFlags cflags = Py.getCompilerFlags(0, false);
		String filename = "<string>";
		pi.eval(Py.compile_flags(ParserFacade.parse(text, CompileMode.exec, filename, cflags), 
						Py.getName(), filename, true, true, cflags));
		return null;

	}

	/** Returns an ArrayList of String, each entry a possible word expansion. */
	protected ArrayList expandStub(String stub) {
		final ArrayList al = new ArrayList();
		PyObject py_vars = pi.eval("vars().keys()");
		if (null == py_vars) {
			p("No vars to search into");
			return al;
		}
		String[] vars = (String[])py_vars.__tojava__(String[].class);
		for (int i=0; i<vars.length; i++) {
			if (vars[i].startsWith(stub)) {
				//System.out.println(vars[i]);
				al.add(vars[i]);
			}
		}
		Collections.sort(al, String.CASE_INSENSITIVE_ORDER);
		System.out.println("stub: '" + stub + "'");
		return al;
	}

	protected String getImportStatement(String packageName, Iterable<String> classNames) {
		StringBuffer buffer = new StringBuffer();
		for (String className : classNames) {
			if (buffer.length() > 0)
				buffer.append(", ");
			buffer.append(className);
		}
		return "".equals(packageName) ?
			"import " + buffer + "\n":
			"from " + packageName + " import " + buffer + "\n";
	}

	protected String getLineCommentMark() {
		return "#";
	}
}
