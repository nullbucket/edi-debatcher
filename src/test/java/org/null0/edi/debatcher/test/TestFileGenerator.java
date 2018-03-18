package org.null0.edi.debatcher.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: All this does is append a fixed file over and over again, the hard way.
// This is something any script or shell could do. Either rewrite this to allow more useful
// templating or remove it.
public class TestFileGenerator {
	private static final Logger logger = LoggerFactory.getLogger(TestFileGenerator.class);

	public static void main(String[] args) {
		if (args.length < 2) {
			logger.info("Usage-> TestFileGenerator outputFileAndLocation repeatInputNumberOfTimes");
			return;
		}
		String outputFileAndLocation = args[0];
		int repeat = Integer.parseInt(args[1]);
		new TestFileGenerator().buildLarge(outputFileAndLocation, repeat);
	}

	void buildLarge(String outputFileAndLocation, int repeat) {
		logger.info("Building large file...");
		try {
			File statText = new File(outputFileAndLocation);
			FileOutputStream os = new FileOutputStream(statText);
			OutputStreamWriter osw = new OutputStreamWriter(os);
			Writer w = new BufferedWriter(osw);
			
			final int BUFFER_SIZE = 1024;
			byte[] dataChunk = new byte[BUFFER_SIZE];
			String data;	
			int bytesRead = 0;
			
			for (int i = 1; i <= repeat; i++) {
				logger.info("REPEAT:------------->{}", i);
				InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("files/837PFile_Large.txt");
                while ((bytesRead = inputStream.read(dataChunk)) != -1) {
					data = new String(dataChunk, 0, bytesRead);
					logger.info(data);
					w.write(data);
				}
			}

			w.close();
			logger.info("DONE");
		} catch (Exception e) {
			logger.error("Exception building large file.", e);
			e.printStackTrace();
		}
	}
}
