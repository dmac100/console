package jsconsole;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import jsconsole.script.Script;
import jsconsole.script.ScriptResult;
import jsconsole.util.Callback;
import jsconsole.util.SwingUtil;
import jsconsole.view.ConsoleView;

public class JsConsole {
	private final CountDownLatch exitLatch = new CountDownLatch(1);
	
	private History history = new History();
	private Script script = new Script();
	private ConsoleView consoleView;
	
	public JsConsole() {
		SwingUtil.invoke(new Runnable() {
			public void run() {
				consoleView = new ConsoleView();
				consoleView.setCommandCallback(new Callback<String>() {
					public void onCallback(String command) {
						execute(command);
					}
				});
				
				try {
					consoleView.setHistory(history.readHistory());
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void addVariable(String name, Object value) {
		script.addVariable(name, value);
	}
	
	public void execute(final String command) {
		SwingUtil.invoke(new Runnable() {
			public void run() {
				if(command.equals("clear")) {
					consoleView.clear();
					return;
				}
				
				try {
					ScriptResult result = script.eval(command);
					if(result.getOutput().length() > 0) {
						consoleView.appendOutput(result.getOutput());
					}
					if(result.getError().length() > 0) {
						consoleView.appendError(result.getError());
					}
					consoleView.appendOutput("=> " + result.getValue());
				} catch(Exception e) {
					e.printStackTrace();
					consoleView.appendError(e.getMessage());
				}
			}
		});
	}
	
	public ScriptResult eval(final String command) {
		return SwingUtil.invoke(new Callable<ScriptResult>() {
			public ScriptResult call() throws Exception {
				return script.eval(command);
			}
		});
	}
	
	public void show() {
		SwingUtil.invoke(new Runnable() {
			public void run() {
				if(consoleView.isShowing()) return;
				
				consoleView.show();
				consoleView.addWindowListener(new WindowAdapter() {
					public void windowClosed(WindowEvent event) {
						try {
							history.writeHistory(consoleView.getHistory());
						} catch(IOException e) {
							e.printStackTrace();
						}
						
						exitLatch.countDown();
					}
				});
			}
		});
	}
	
	public void waitForExit() {
		SwingUtil.invoke(new Runnable() {
			public void run() {
				if(!consoleView.isShowing()) {
					show();
				}
			}
		});
		
		try {
			exitLatch.await();
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void open() {
		JsConsole jsConsole = new JsConsole();
		jsConsole.show();
		jsConsole.waitForExit();
	}

	public static void main(String[] args) {
		new JsConsole().waitForExit();
	}
}
