package eu.essi_lab.turtle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TurtleCleaner {

	public TurtleCleaner() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		TurtleCleaner cleaner = new TurtleCleaner();
		cleaner.cleanMultiLineDescription("/home/boldrini/turtle/turtle-2.ttl", "/home/boldrini/turtle/turtle-b.ttl");
	}

	private void cleanMultiLineDescription(String filePathSource, String filePathTarget) {
		File source = new File(filePathSource);
		File target = new File(filePathTarget);
		try (BufferedReader br = new BufferedReader(new FileReader(source));
				BufferedWriter bw = new BufferedWriter(new FileWriter(target))) {
			String line;
			boolean inMultilineDescription = false;
			String descriptionLine = "";
			while ((line = br.readLine()) != null) {
				if (line.startsWith("dct:description") && !line.endsWith("\";")) {
					descriptionLine = line.trim();
					inMultilineDescription = true;
					continue;
				}
				if (inMultilineDescription) {
					descriptionLine += line.trim();
					if (line.endsWith("\";")) {
						inMultilineDescription = false;
						line = descriptionLine;
					} else {
						continue;
					}
				}

				bw.write(line);
				bw.newLine();
			}
			System.out.println("File cleaned successful!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
