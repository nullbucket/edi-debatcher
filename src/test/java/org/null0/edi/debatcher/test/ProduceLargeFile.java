package org.null0.edi.debatcher.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProduceLargeFile {

	private static final Logger logger = LoggerFactory.getLogger(ProduceLargeFile.class);
	private static String outputFileAndLocation;
	private static int repeat;

	public static void main(String[] args) {

		if (args.length < 2) {
			logger.info("Usage-> ProduceLargeFile outputFileAndLocation repeatInputNumberOfTimes");
			return;
		}
		outputFileAndLocation = args[0];
		repeat = Integer.parseInt(args[1]);
		new ProduceLargeFile().buildLargeFile();

	}

	private void buildLargeFile() {

		logger.info("Building large file...");
		try {
			byte[] dataChunk = new byte[1024];
			String data;
			int bytesRead = 0;

			// File statText = new File("/home/developer/lnxshare/LargeP.txt");
			File statText = new File(outputFileAndLocation);
			FileOutputStream os = new FileOutputStream(statText);
			OutputStreamWriter osw = new OutputStreamWriter(os);
			Writer w = new BufferedWriter(osw);

			int temp = 1;

			while (temp <= repeat) {
				logger.info("REPEAT:------------->" + temp);
				InputStream inputStream = getClass().getClassLoader().getResourceAsStream("files/837PFile_Large.txt");

				while ((bytesRead = inputStream.read(dataChunk)) != -1) {
					data = new String(dataChunk, 0, bytesRead);
					logger.info(data);
					w.write(data);
				}
				temp++;
			}

			w.close();
			logger.info("DONE");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
