package jsconsole.script;

import java.io.PrintStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jsconsole.util.Callback;

public class Script {
	private final ScriptEngine engine;
	
	public Script() {
		engine = new ScriptEngineManager().getEngineByName("nashorn");
		if(engine == null) {
			throw new RuntimeException("Can't create JavaScript engine");
		}
		
		eval("function print() { System.out.println([].slice.call(arguments).join(', ')) }");
	}
	
	public void addVariable(String name, Object value) {
		engine.put(name, value);
	}
	
	public String eval(String command) {
		Callback<String> nullCallback = new Callback<String>() {
			public void onCallback(String t) {
			}
		};
		
		try {
			return eval(command, nullCallback, nullCallback);
		} catch(ScriptException e) {
			throw new RuntimeException("Error evaluating command", e);
		}
	}
	
	public String eval(String command, Callback<String> outputCallback, Callback<String> errorCallback) throws ScriptException {
        PrintStream out = System.out;
        PrintStream err = System.err;
        try {
        	LineReader outputReader = new LineReader(outputCallback);
        	LineReader errorReader = new LineReader(errorCallback);
        	
	        System.setOut(new PrintStream(outputReader.getOutputStream()));
	        System.setErr(new PrintStream(errorReader.getOutputStream()));
	        
			Object output = engine.eval("with(new JavaImporter(java.util, java.lang)) { " + command + "}");
			String value = String.valueOf(output);

			System.out.close();
			System.err.close();
			
			outputReader.waitUntilDone();
			errorReader.waitUntilDone();
			
			return value;
        } finally {
        	System.setOut(out);
        	System.setErr(err);
        }
	}
}
