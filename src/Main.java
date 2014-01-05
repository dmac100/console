import javax.script.ScriptException;
import javax.swing.SwingUtilities;

import script.Script;
import script.ScriptResult;
import util.Callback;
import view.ConsoleView;

public class Main {
	public static void main(String[] args) throws ScriptException {
		final Script ruby = new Script();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final ConsoleView consoleView = new ConsoleView();
				consoleView.show();
				
				ruby.addVariable("consoleView", consoleView);
				
				consoleView.setCommandCallback(new Callback<String>() {
					public void onCallback(String command) {
						if(command.equals("clear")) {
							consoleView.clear();
							return;
						}
						
						try {
							ScriptResult result = ruby.eval(command);
							if(result.getOutput().length() > 0) {
								consoleView.appendOutput(result.getOutput().trim());
							}
							if(result.getError().length() > 0) {
								consoleView.appendError(result.getError().trim());
							}
							consoleView.appendOutput("=> " + result.getValue());
						} catch(Exception e) {
							e.printStackTrace();
							consoleView.appendError(e.getMessage());
						}
					}
				});
			}
		});
	}
}
