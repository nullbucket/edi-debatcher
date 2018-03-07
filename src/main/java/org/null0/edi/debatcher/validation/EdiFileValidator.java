package org.null0.edi.debatcher.validation;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdiFileValidator {

	private static final Logger logger = LoggerFactory.getLogger(EdiFileValidator.class);

	public void validateEdi(InputStream is) throws Exception {
		logger.info("Validation started...");
		/*
		 * BufferedReader reader = new BufferedReader(new InputStreamReader(is)); String
		 * line; StringBuffer sb = new StringBuffer(); int lineCnt = 0; if(is != null) {
		 * while ((line = reader.readLine()) != null) { //sb.append(line);
		 * //logger.info(line); lineCnt++; logger.info("Line count: "+lineCnt); }
		 * //validateRules(sb.toString()); }
		 */

		/*
		 * File statText = new File("c:/winshare/test.txt"); FileOutputStream os = new
		 * FileOutputStream(statText); OutputStreamWriter osw = new
		 * OutputStreamWriter(os); Writer w = new BufferedWriter(osw);
		 */

		int bytesRead;
		byte[] chunk = new byte[1024];
		int readCount = 0;
		String strChunk = "";
		while ((bytesRead = is.read(chunk)) != -1) {
			strChunk = new String(chunk, 0, bytesRead);
			logger.info(strChunk);
			readCount++;
			logger.info("Read count: " + readCount);
			// w.write(strChunk, 0, bytesRead);
		}

		is.close();
		// w.close();
	}

	public void validateRules(String ediString) throws Exception {
		if (ediString == null) {
			throw new Exception("No EDI file content");
		}
		if (ediString.indexOf("ISA") != 0) {
			throw new Exception("EDI file should start with an ISA Interchange Segment");
		}

		char fieldDelimiter = ediString.charAt(103);
		logger.info("Field delimiter: " + fieldDelimiter);

		char segmentDelimiter = ediString.charAt(105);
		logger.info("Segment delimiter: " + segmentDelimiter);

		int countOfIsa = StringUtils.countMatches(ediString, segmentDelimiter + "ISA" + fieldDelimiter);
		int countOfIea = StringUtils.countMatches(ediString, segmentDelimiter + "IEA" + fieldDelimiter);
		logger.info("ISA count " + countOfIsa + " IEA count " + countOfIea);

		int countOfGs = StringUtils.countMatches(ediString, segmentDelimiter + "GS" + fieldDelimiter);
		int countOfGe = StringUtils.countMatches(ediString, segmentDelimiter + "GE" + fieldDelimiter);
		logger.info("GS count " + countOfGs + " GE count " + countOfGe);

		int countOfSt = StringUtils.countMatches(ediString, segmentDelimiter + "ST" + fieldDelimiter);
		int countOfSe = StringUtils.countMatches(ediString, segmentDelimiter + "SE" + fieldDelimiter);
		logger.info("ST count " + countOfSt + " SE count " + countOfSe);

		if ((countOfIsa + 1 != countOfIea) || (countOfGs != countOfGe) || (countOfSt != countOfSe)) {
			throw new Exception("EDI file has incorrect number of segments");
		}
	}

}
