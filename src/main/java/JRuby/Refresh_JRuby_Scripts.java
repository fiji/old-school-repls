/* -*- mode: java; c-basic-offset: 8; indent-tabs-mode: t; tab-width: 8 -*- */

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

package JRuby;

import common.RefreshScripts;

import ij.IJ;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.jruby.Ruby;

public class Refresh_JRuby_Scripts extends RefreshScripts {

	public void run(String arg) {
		setLanguageProperties(".rb","Ruby");
		setVerbose(false);
		super.run(arg);
	}

	public void runScript(String filename) {
		try {
			// runScript(InputStream) will close the stream
			runScript(new FileInputStream(filename));
		} catch( IOException e ) {
			throw new RuntimeException("Couldn't open the script: "+filename);
		}
	}

	/** Will consume and close the stream. */
	public void runScript(InputStream istream) {
		runScript(istream, "");
	}

	/** Will consume and close the stream. */
	public void runScript(InputStream istream, String filename) {
		System.out.println("Starting JRuby in runScript()...");
		Thread.currentThread().setContextClassLoader(IJ.getClassLoader());
		Ruby rubyRuntime = Ruby.newInstance(System.in, new PrintStream(super.out), new PrintStream(super.err));
		System.out.println("Done.");
		rubyRuntime.evalScriptlet(JRuby_Interpreter.getStartupScript());

		try {
			rubyRuntime.runFromMain(istream, filename);
		} catch( Throwable t ) {
			printError(t);
		} finally {
			try {
				istream.close();
			} catch (Exception e) {
				System.out.println("JRuby runScript could not close the stream!");
				e.printStackTrace();
			}
		}

		// Undesirably this throws an exception, so just let the 
		// JRuby runtime get finalized whenever...

		// rubyRuntime.evalScriptlet("exit");
	}
}
