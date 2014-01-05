package script;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
	
	public ScriptResult eval(String command) {
		Context context = Context.enter();
		try {
			context.setOptimizationLevel(-1);
	        context.setLanguageVersion(Context.VERSION_1_8);
	        
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
	        
	        PrintStream out = System.out;
	        PrintStream err = System.err;
	        try {
		        System.setOut(new PrintStream(outputStream));
		        System.setErr(new PrintStream(errorStream));
			
				Object output = context.evaluateString(scope, command, "<js>", 1, null);
				String value = Context.toString(output);
				
				return new ScriptResult(value, outputStream.toString(), errorStream.toString());
	        } finally {
	        	System.setOut(out);
	        	System.setErr(err);
	        }
		} finally {
			Context.exit();
		}
	}
}
