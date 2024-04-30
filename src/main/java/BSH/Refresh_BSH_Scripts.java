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

package BSH;

import bsh.EvalError;
import bsh.Interpreter;

import common.RefreshScripts;

import ij.IJ;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Refresh_BSH_Scripts extends RefreshScripts {

	public void run(String arg) {
		setLanguageProperties(".bsh","BeanShell");
		setVerbose(false);
		super.run(arg);
	}

	/** Runs the script at path */
	public void runScript(String path) {
		try {
			if (!path.endsWith(".bsh") || !new File(path).exists()) {
				IJ.log("Not a BSH script or not found: " + path);
				return;
			}
			// The stream will be closed by runScript(InputStream)
			runScript(new BufferedInputStream(new FileInputStream(new File(path))), path);
		} catch (Throwable error) {
			printError(error);
		}
	}

	/** Will consume and close the stream. */
	public void runScript(InputStream istream) {
		runScript(istream, null);
	}

	/** Will consume and close the stream. */
	public void runScript(InputStream istream, String sourceFileName) {
		try {
			Interpreter interpreter = new Interpreter();
			interpreter.setOut(new PrintStream(out));
			interpreter.setErr(new PrintStream(err));
			BSH_Interpreter bsh = new BSH_Interpreter();
			interpreter.eval(bsh.getImportStatement());
			interpreter.eval(new InputStreamReader(istream),
				interpreter.getNameSpace(), sourceFileName);
		} catch (Throwable error) {
			if (error instanceof EvalError)
				((EvalError)error).setMessage(error.toString());
			printError(error);
		}
	}
}
