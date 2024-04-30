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

import common.AbstractInterpreter;

import ij.IJ;
import ij.Menus;

import java.io.File;
import java.io.PrintStream;

import org.jruby.Ruby;

public class JRuby_Interpreter extends AbstractInterpreter {

	Ruby rubyRuntime;

	protected Object eval(String text) throws Throwable {
		return rubyRuntime.evalScriptlet(text);
	}

	public void run( String ignored ) {
		// Strangely, this seems to always return null even if
		// there's an instance already running...
		if( null != Ruby.getCurrentInstance() ) {
			IJ.error("There is already an instance of "+
				 "the JRuby interpreter");
			return;
		}
		Thread.currentThread().setContextClassLoader(IJ.getClassLoader());
		super.run(ignored);
		setTitle("JRuby Interpreter");
		println("Starting JRuby ...");
		prompt.setEnabled(false);
		PrintStream stream = new PrintStream(out);
		rubyRuntime = Ruby.newInstance(System.in,stream,stream);
		println("done.");
		prompt.setEnabled(true);

		rubyRuntime.evalScriptlet(getStartupScript());
		importAll();
	}

	public static String getImageJRubyPath() {
		String pluginsPath = Menus.getPlugInsPath();
		return pluginsPath + "JRuby" + File.separator + "imagej.rb";
	}

	/* This sets up method_missing to find the right class for
	   anything beginning ij in the ij package.  (We could change
	   this to add other package hierarchies too, e.g.  those in
	   VIB.)  It also loads a file of Ruby equivalents to ImageJ
	   macro functions. */
	public static String getStartupScript() {
		String s =
			"require 'java'\n" +
			"module Kernel\n" +
			"  def ij\n" +
			"    JavaUtilities.get_package_module_dot_format('ij')\n" +
			"  end\n" +
			"end\n" +
			"imagej_functions_path = '"+getImageJRubyPath()+"'\n" +
			"require imagej_functions_path\n";
		return s;
	}

	protected String getImportStatement(String packageName, Iterable<String> classNames) {
		StringBuffer sb = new StringBuffer();
		if (!"".equals(packageName))
			packageName += ".";
		for (String className : classNames)
			sb.append("java_import '").append(packageName)
				.append(className).append("'\n");
		return sb.toString();
	}

	protected String getLineCommentMark() {
		return "#";
	}
}
