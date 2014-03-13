package jsconsole.script;

import java.io.*;
import java.util.concurrent.*;

import jsconsole.util.Callback;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Creates an OutputStream that calls a callback for each line that's written to it.
 */
public class LineReader {
	private static ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());
	
	private PipedOutputStream outputStream;
	private Future<?> future;
	
	public LineReader(final Callback<String> callback) {
		try {
			final PipedInputStream pipedInputStream = new PipedInputStream();
			final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
			  
			future = executor.submit(new Runnable() {
				public void run() {
					try {
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pipedInputStream));
						String line;
						while((line = bufferedReader.readLine()) != null) {
							callback.onCallback(line);
						}
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			});
			
			this.outputStream = pipedOutputStream;
		} catch(IOException e) {
			throw new RuntimeException("Error creating outputstream", e);
		}
	}
	
	/**
	 * Returns the OutputStream to write to, which will call the main callback for each line.
	 */
	public PipedOutputStream getOutputStream() {
		return outputStream;
	}

	/**
	 * Waits until there are no more lines left - the OutputStream is closed and all lines have been
	 * passed to the callback.
	 */
	public void waitUntilDone() {
		try {
			future.get();
		} catch(InterruptedException | ExecutionException e) {
			throw new RuntimeException("Error waiting for stream", e);
		}
	}
} 