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

package Clojure;

import common.RefreshScripts;

import ij.IJ;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Refresh_Clojure_Scripts extends RefreshScripts {

	public void run(String arg) {
		setLanguageProperties(".clj", "Clojure");
		setVerbose(false);
		super.run(arg);
	}

	public void runScript(String path) {
		runScript(path, null);
	}

	public boolean runScript(String path, Map<String,Object> vars) {
		if (!path.endsWith(".clj") || !new File(path).exists()) {
			IJ.log("Not a clojure script or not found: " + path);
			return false;
		}
		Clojure_Interpreter ci = new Clojure_Interpreter();
		PrintWriter pout = new PrintWriter(super.out);
		try {
			if (IJ.isWindows()) path = path.replace('\\', '/');
			ci.init();
			if (null == vars) {
				vars = new HashMap<String,Object>();
			}
			// Redirect output to whatever it was set to with super.setOutputStreams
			vars.put("*out*", pout);
			ci.pushThreadBindings(vars); // into clojure.core namespace
			Object res = ci.evaluate(new StringBuilder("(load-file \"").append(path).append("\")").toString());
			if (null != res) {
				String s = res.toString().trim();
				if (s.length() > 0 && !"nil".equals(s)) {
					IJ.log(s); // Not using pout.print(s); pout.flush(); because it will print all the declarations, and it's annoying.
				}
			}
			return true;
		} catch (Throwable error) {
			printError(error);
			return false;
		} finally {
			ci.destroy();
		}
	}

	/** Will consume and close the stream. */
	public void runScript(final InputStream istream) {
		Clojure_Interpreter ci = new Clojure_Interpreter();
		PrintWriter pout = new PrintWriter(super.out);
		try {
			ci.init();

			// Redirect output to whatever it was set to with super.setOutputStreams
			HashMap<String,Object> vars = new HashMap<String,Object>();
			vars.put("*out*", pout);
			ci.pushThreadBindings(vars); // into clojure.core namespace

			Object res = ci.evaluate(istream);
			if (null != res) {
				String s = res.toString().trim();
				if (s.length() > 0 && !"nil".equals(s)) {
					IJ.log(s);
				}
			}
		} catch (Throwable error) {
			printError(error);
		} finally {
			ci.destroy();
		}
	}
}
