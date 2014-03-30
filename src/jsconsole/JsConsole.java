package jsconsole;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.*;

import jsconsole.script.Script;
import jsconsole.util.Callback;
import jsconsole.util.SwingUtil;
import jsconsole.view.ConsoleBuffer;
import jsconsole.view.ConsoleView;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class JsConsole {
	private static ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());
	
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
	
	public JsConsole addVariable(String name, Object value) {
		script.addVariable(name, value);

		return this;
	}
	
	public JsConsole execute(final String command) {
		executor.execute(new Runnable() {
			public void run() {
				if(command.equals("clear")) {
					SwingUtil.invoke(new Runnable() {
						public void run() {
							consoleView.clear();
						}
					});
					return;
				}
				
				try {
					final ConsoleBuffer consoleBuffer = new ConsoleBuffer(consoleView);
					
					Callback<String> outputCallback = new Callback<String>() {
						public void onCallback(final String line) {
							consoleBuffer.appendOutput(line);
						}
					};
					
					Callback<String> errorCallback = new Callback<String>() {
						public void onCallback(final String line) {
							consoleBuffer.appendError(line);
						}
					};
					
					final String result = script.eval(command, outputCallback, errorCallback);
					
					consoleBuffer.flush();
					
					SwingUtil.invoke(new Runnable() {
						public void run() {
							consoleView.appendOutput("=> " + result);
						}
					});
				} catch(final Exception e) {
					SwingUtil.invoke(new Runnable() {
						public void run() {
							e.printStackTrace();
							consoleView.appendError(e.getMessage());
						}
					});
				}
			}
		});

		return this;
	}
	
	public String eval(final String command) {
		return SwingUtil.invoke(new Callable<String>() {
			public String call() throws Exception {
				return script.eval(command);
			}
		});
	}
	
	public JsConsole show() {
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

		return this;
	}
	
	public JsConsole waitForExit() {
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

		return this;
	}
	
	public static void main(String[] args) {
		new JsConsole().waitForExit();
	}
}
