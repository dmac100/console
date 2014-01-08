package jsconsole.util;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;

import com.google.common.util.concurrent.SettableFuture;

public class SwingUtil {
	public static <T> T invoke(final Callable<T> callable) {
		final SettableFuture<T> future = SettableFuture.create();
		
		try {
			invoke(new Runnable() {
				public void run() {
					try {
						future.set(callable.call());
					} catch(Throwable e) {
						future.setException(e);
					}
				}
			});
			
			return future.get();
		} catch(InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void invoke(Runnable runnable) {
		if(SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch(InvocationTargetException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
