package org.null0.edi.debatcher;

import java.io.IOException;
import java.util.Properties;

public class FileProperties {
	private char dataElementSeparator;
	private char dataRepetitionSeparator;
	private char componentElementSeparator;
	private char segmentTerminator;

	private boolean overridefilesize;
	private String isaieafoldername;
	private String stsefoldername;
	private String gsgefoldername;
	private String parentfoldername;

	/** 
	 * 
	 * 
	 * TODO This class does not appear to be used, except in a unit test. 
	 * Need to come up with a cleaner solution.
	*/
	public FileProperties() {
		try {
			Properties prop = new Properties();
			prop.load(FileProperties.class.getClassLoader().getResourceAsStream("edi-parameters.properties"));	
			
			dataElementSeparator = prop.getProperty("dataElementSeparator").charAt(0);
			dataRepetitionSeparator =  prop.getProperty("dataRepetitionSeparator").charAt(0);
			componentElementSeparator = prop.getProperty("componentElementSeparator").charAt(0);
			segmentTerminator = prop.getProperty("segmentTerminator").charAt(0);
		} 
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public char getDataElementSeparator() {
		return dataElementSeparator;
	}

	public void setDataElementSeparator(char dataElementSeparator) {
		this.dataElementSeparator = dataElementSeparator;
	}

	public char getDataRepetitionSeparator() {
		return dataRepetitionSeparator;
	}

	public void setDataRepetitionSeparator(char dataRepetitionSeparator) {
		this.dataRepetitionSeparator = dataRepetitionSeparator;
	}

	public char getComponentElementSeparator() {
		return componentElementSeparator;
	}

	public void setComponentElementSeparator(char componentElementSeparator) {
		this.componentElementSeparator = componentElementSeparator;
	}

	public String getISAIEAFolderName() {
		return isaieafoldername;
	}

	public void setISAIEAFolderName(String isaieafoldername) {
		this.isaieafoldername = isaieafoldername;
	}

	public String getParentFolderName() {
		return parentfoldername;
	}

	public void setParentFolderName(String parentfoldername) {
		this.parentfoldername = parentfoldername;
	}

	public String getGSGEFolderName() {
		return gsgefoldername;
	}

	public void setGSGEFolderName(String gsgefoldername) {
		this.gsgefoldername = gsgefoldername;
	}

	public String getSTSEFolderName() {
		return stsefoldername;
	}

	public void setSTSEFolderName(String stsefoldername) {
		this.stsefoldername = stsefoldername;
	}

	public char getSegmentTerminator() {
		return segmentTerminator;
	}

	public void setSegmentTerminator(char segmentTerminator) {
		this.segmentTerminator = segmentTerminator;
	}

	public boolean getOverrideFileSize() {
		return this.overridefilesize;
	}

	public void setOverrideFileSize(boolean overridefilesize) {
		this.overridefilesize = overridefilesize;
	}
}
