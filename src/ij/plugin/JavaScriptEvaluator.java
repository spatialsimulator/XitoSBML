/*******************************************************************************
 * Copyright 2015 Kaito Ii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ij.plugin;
import ij.IJ;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

// TODO: Auto-generated Javadoc
/** Implements the macro editor's Macros/Evaluate JavaScript command 
    on systems running Java 1.6 or later. The JavaScript plugin at
    <http://rsb.info.nih.gov/ij/plugins/download/misc/JavaScript.java>
    is used to evaluate JavaScript on systems running versions
    of Java earlier than 1.6. */
public class JavaScriptEvaluator implements PlugIn, Runnable  {
	
	/** The thread. */
	private Thread thread;
	
	/** The script. */
	private String script;
	
	/** The result. */
	private Object result;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	// run script in separate thread
	public void run(String script) {
		if (script.equals("")) return;
		if (!IJ.isJava16()) {
			IJ.error("Java 1.6 or later required");
			return;
		}
		this.script = script;
		thread = new Thread(this, "JavaScript"); 
		thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
		thread.start();
	}

	/**
	 * Run.
	 *
	 * @param script the script
	 * @param arg the arg
	 * @return the string
	 */
	// run script in current thread
	public String run(String script, String arg) {
		this.script = script;
		run();
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		result = null;
		Thread.currentThread().setContextClassLoader(IJ.getClassLoader());
		try {
			ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
			ScriptEngine engine = scriptEngineManager.getEngineByName("ECMAScript");
			if (engine == null)
				{IJ.error("Could not find JavaScript engine"); return;}
			engine.eval("function load(path) {\n"
				+ "  importClass(Packages.sun.org.mozilla.javascript.internal.Context);\n"
				+ "  importClass(Packages.java.io.FileReader);\n"
				+ "  var cx = Context.getCurrentContext();\n"
				+ "  cx.evaluateReader(this, new FileReader(path), path, 1, null);\n"
				+ "}");
			result = engine.eval(script);
		} catch(Throwable e) {
			String msg = e.getMessage();
			if (msg.startsWith("sun.org.mozilla.javascript.internal.EcmaError: "))
				msg = msg.substring(47, msg.length());
			if (msg.startsWith("sun.org.mozilla.javascript.internal.EvaluatorException"))
				msg = "Error"+msg.substring(54, msg.length());
			if (!msg.contains("Macro canceled"))
				IJ.log(msg);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return result!=null?""+result:"";
	}

}
