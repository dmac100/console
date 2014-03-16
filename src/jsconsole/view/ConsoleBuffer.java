package jsconsole.view;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import jsconsole.util.SwingUtil;
import jsconsole.util.ThrottledRunnable;

/**
 * Buffer output and error for the ConsoleView so that multiple updates close together
 * are grouped as a single update.
 */
public class ConsoleBuffer {
	private final ConsoleView consoleView;
	
	private final Deque<String> outputLines = new ConcurrentLinkedDeque<String>();
	private final Deque<String> errorLines = new ConcurrentLinkedDeque<String>();

	private final Runnable throttledRunnable = new ThrottledRunnable(new Runnable() {
		public void run() {
			flush();
		}
	}, 100);

	public ConsoleBuffer(ConsoleView consoleView) {
		this.consoleView = consoleView;
	}

	public void appendOutput(String line) {
		outputLines.add(line);
		throttledRunnable.run();
	}
	
	public void appendError(String line) {
		errorLines.add(line);
		throttledRunnable.run();
	}
	
	public void flush() {
		SwingUtil.invoke(new Runnable() {
			public void run() {
				if(!outputLines.isEmpty()) {
					consoleView.appendOutput(join(outputLines));
				}
				if(!errorLines.isEmpty()) {
					consoleView.appendError(join(errorLines));
				}
			}
		});
	}
	
	private static String join(Deque<String> buffer) {
		StringBuilder s = new StringBuilder();
		String line;
		while((line = buffer.pollFirst()) != null) {
			if(s.length() > 0) s.append("\n");
			s.append(line);
		}
		return s.toString();
	}
}
