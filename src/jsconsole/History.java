package jsconsole;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

import com.google.common.base.Charsets;

public class History {
	private File getFile() {
		String home = System.getProperty("user.home");
		return new File(home, ".consoleHistory");
	}
	
	public void writeHistory(List<String> history) throws IOException {
		try(PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(getFile()), Charsets.UTF_8))) {
			for(String s:history) {
				if(!s.trim().isEmpty()) {
					writer.println(s.trim());
				}
			}
		}
	}
	
	public List<String> readHistory() throws IOException {
		if(!getFile().exists()) {
			return new ArrayList<>();
		}
		return Files.readAllLines(getFile().toPath(), Charsets.UTF_8);
	}
}
