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

package emblcmci.scalainterp;

import common.RefreshScripts;

import ij.IJ;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import scala.collection.immutable.List;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;

/**
 * ImageJ plugin for loading scala scripts in defined folder (Scripts)
 * under Fiji root. 
 * Loads scala script file (.scala file) and executes.
 *
 * Sample Scala script:
 *
 * {@code
 * import ij._
 * println ("Hello from external script :-)")
 * IJ.log("test ij")
 * IJ.log(IJ.getVersion())
 * val imp = IJ.openImage("http://imagej.nih.gov/ij/images/blobs.gif")
 * imp.show()
 * IJ.wait(2000)
 * imp.close()
 * }
 *
 * @author Kota Miura
 */
public class Refresh_Scala_Script extends RefreshScripts {

	IMain imain = null;
	
    public void run(String arg) {
        setLanguageProperties(".scala", "Scala");
        setVerbose(false);
        super.run(arg);
    }

	/* 
	 * Runs .scala script in file system
	 */
	@Override
	public void runScript(String path) {
        try {
            if (!path.endsWith(".scala") || !new File(path).exists()) {
                IJ.log("Not a scala script or not found: " + path);
                return;
            }        	
            runScript(new BufferedInputStream(new FileInputStream(new File(path))));
        } catch (Throwable t) {
        	IJ.log("Refresh_Scala_Script: Failed loading" + path);
            printError(t);
        }
	}
	
	@Override
	public void runScript(InputStream arg0) {		
		String line;
		BufferedReader br= new BufferedReader(new InputStreamReader(arg0));
		StringBuilder sb = new StringBuilder();
		try {
			while ((line = br.readLine()) != null)
				sb.append(line + ";") ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		runScriptString(sb.toString());
		
	}
	/** Takes Scala script as a String argument and then interprets it. 
	 * 
	 * @param script
	 */
	public void runScriptString(String script){
		Thread.currentThread().setContextClassLoader(IJ.getClassLoader());
		Settings settings = new Settings();
		List<String> param = List.make(1, "true");
		settings.usejavacp().tryToSet(param);
		PrintWriter stream = new PrintWriter(System.out);
		imain = new IMain(settings, stream);
		imain.interpret(script);
	}


	/**
	 * Debugging main. 
	 * @param args
	 */
	public static void main(String[] args) {
		Refresh_Scala_Script refresh = new Refresh_Scala_Script();
		//refresh.run("");
		String path = "/Users/miura/Dropbox/codes/mavenscala/ijscalascript/scripts/helloscript.scala";
		refresh.runScript(path);
	}

}
