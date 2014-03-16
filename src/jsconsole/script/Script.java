package jsconsole.script;

import java.io.PrintStream;

import jsconsole.util.Callback;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;

public class Script {
	private ImporterTopLevel scope;

	public Script() {
		Context context = Context.enter();
		try {
			this.scope = new ImporterTopLevel(context);
		} finally {
			Context.exit();
		}
		
		eval("importPackage(java.lang)");
		eval("importPackage(java.util)");
		
		eval("function print() { System.out.println([].slice.call(arguments).join(', ')) }");
	}
	
	public void addVariable(String name, Object value) {
		scope.defineProperty(name, value, 0);
	}
	
	public String eval(String command) {
		Callback<String> nullCallback = new Callback<String>() {
			public void onCallback(String t) {
			}
		};
		
		return eval(command, nullCallback, nullCallback);
	}
	
	public String eval(String command, Callback<String> outputCallback, Callback<String> errorCallback) {
		Context context = Context.enter();
		try {
			context.setOptimizationLevel(-1);
	        context.setLanguageVersion(Context.VERSION_1_8);
	        
	        PrintStream out = System.out;
	        PrintStream err = System.err;
	        try {
	        	LineReader outputReader = new LineReader(outputCallback);
	        	LineReader errorReader = new LineReader(errorCallback);
	        	
		        System.setOut(new PrintStream(outputReader.getOutputStream()));
		        System.setErr(new PrintStream(errorReader.getOutputStream()));
			
				Object output = context.evaluateString(scope, command, "<js>", 1, null);
				String value = Context.toString(output);

				System.out.close();
				System.err.close();
				
				outputReader.waitUntilDone();
				errorReader.waitUntilDone();
				
				return value;
	        } finally {
	        	System.setOut(out);
	        	System.setErr(err);
	        }
		} finally {
			Context.exit();
		}
	}
}
