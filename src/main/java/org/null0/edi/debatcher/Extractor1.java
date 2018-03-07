// Test Trigger (TODO: remove this comment)
package org.null0.edi.debatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.null0.edi.debatcher.FileProperties;

public class Extractor1 {

	private URL edifilepath;
	private static final Logger Log = LoggerFactory.getLogger(Extractor1.class);
	private long filesize; // Value set greater than 1 GB for testing
	private String edilines;
	private String currentdatetime;

	private File datetimefolder;
	// private File senderfolder;
	private File isafolder;
	private File gsfolder;
	private File stfolder;
	private File clmfolder;
	private Properties edifileproperties = null;
	private String propertiesfilename = "src/test/resources/edi-parameters.properties";
	private FileProperties fileproperties;

	// private Senders senderinfo;
	// private Sender sender;
	public Extractor1(URL edifilepath) {
		this.edifilepath = edifilepath;
		try {
			// setSenderId("TX");
			// senderinfo = Senders.load();

			if (edifileproperties == null) {
				ReadProperties();
			} else {
				Log.error(String.format("Properties file was not found at location %s", propertiesfilename));
				throw new FileNotFoundException("The properties file was not found");
			}
			Log.debug("Checking the file size..");
			setFileSize(new File(edifilepath.getFile()).length());
		} catch (Exception ex) {
			Log.error(ex.getMessage());
		}
	}

	private void ReadProperties() throws Exception {
		Log.debug("Reading the properties file..");
		edifileproperties = new Properties();
		try (InputStream in = new FileInputStream(propertiesfilename)) {
			edifileproperties.load(in);
			fileproperties = new FileProperties();
			// Set the file delimiters which will be used for splitting the lines
			fileproperties.setDataElementSeparator(edifileproperties.getProperty("dataElementSeperator").charAt(0));
			fileproperties.setSegmentTerminator(edifileproperties.getProperty("segmentTerminator").charAt(0));
			fileproperties.setISAIEAFolderName(edifileproperties.getProperty("isaieafoldername"));
			fileproperties.setGSGEFolderName(edifileproperties.getProperty("gsgefoldername"));
			fileproperties.setSTSEFolderName(edifileproperties.getProperty("stsefoldername"));
			fileproperties.setParentFolderName(edifileproperties.getProperty("rootfolderpath"));
			fileproperties.setOverrideFileSize(Boolean.parseBoolean(edifileproperties.getProperty("overridefilesize")));
			Log.debug("Finished the properties file..");
		} catch (Exception e) {
			Log.error("IO Error in ReadProperties", e.getMessage());
			throw e;
		}
	}

	// Delete the temp. directories that were created; we don't need those any more
	private boolean DeleteTempDirectories(boolean delete) {
		if (delete) {
			File tempdirectory = new File(isafolder.getPath());
			return tempdirectory.exists() ? tempdirectory.delete() : false;
		} else {
			return false;
		}
	}

	private boolean CreateFolders(String rootfoldername, String currentdatetime) {
		return CreateDateTimeFolder(rootfoldername, currentdatetime);
	}

	private boolean CreateDateTimeFolder(String rootfoldername, String currentdatetime) {
		datetimefolder = new File(rootfoldername + currentdatetime);
		return !datetimefolder.exists() ? datetimefolder.mkdir() : false;
	}

	private boolean CreateInterchangeControlFolder(String rootfolder, String isaisefolderpath) {
		isafolder = new File(rootfolder + "/" + isaisefolderpath);
		return !isafolder.exists() ? isafolder.mkdir() : false;
	}

	private boolean CreateFunctionalGroupFolders(String isaieafolderpath, String gsgefolderpath) {
		gsfolder = new File(isaieafolderpath + "/" + gsgefolderpath);
		return !gsfolder.exists() ? gsfolder.mkdir() : false;
	}

	private boolean CreateTransactionalSetFolders(String gsgefolderpath, String stsefolderpath) {
		stfolder = new File(gsgefolderpath + "/" + stsefolderpath);
		return !stfolder.exists() ? stfolder.mkdir() : false;
	}

	private boolean CreateFinalOutputFolders(String rootfolder) {
		clmfolder = new File(rootfolder + "/" + "Claims");
		return !clmfolder.exists() ? clmfolder.mkdir() : false;
	}

	public void setFileSize(long filesize) {
		this.filesize = filesize;
	}

	public long getFileSize() {
		return this.filesize;
	}

	public String CurrentDateTime() {
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss:SSS");
		Date currentdate = new Date();
		this.currentdatetime = dateformat.format(currentdate);
		return this.currentdatetime;
	}

	public boolean Debatch() throws Exception {
		// Check file size and determine the path; if the filesize is > 1GB
		Log.debug("File size={}", getFileSize());
		String[] splitoutput = null;
		// TODO: Add the flag to check the file sizes
		if (getFileSize() > 1000000000) {
			ReadFileToBuffer(edifilepath, true);

		} else {
			if (!CreateFolders(fileproperties.getParentFolderName(), CurrentDateTime())) {
				Log.error("Unable to create folder's");
				throw new Exception(String.format("Unable to create the folder"));
			}
			ReadFileToBuffer(edifilepath);
			splitoutput = SplitLines(edilines);
			SplitSections(splitoutput);
		}
		return true;
	}

	private void SplitSections(String[] splitoutput) throws Exception {
		SplitISAIEASegment(splitoutput);
		ReadISAIEATempFolder();
		ReadSTSETempFolder();
		ReadClaimandLineLevels();
	}

	// TODO Read the large files ~1GB
	private void ReadFileToBuffer(URL edifilepath, boolean chunking) throws IOException {
		int CHUNKSIZE = 1024 * 4; // 2MB chunks
		byte[] barray = new byte[CHUNKSIZE];
		try (RandomAccessFile file = new RandomAccessFile(edifilepath.getPath(), "r")) {
			FileChannel channel = file.getChannel();
			long size = channel.size();
			// Log.info("File size={}",size);
			long red = 0L;
			do {
				long read = Math.min(Integer.MAX_VALUE, size - red);

				MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, red, read);
				int nGet;
				while (buffer.hasRemaining()) {
					nGet = Math.min(buffer.remaining(), CHUNKSIZE);
					buffer.get(barray, 0, nGet);
					for (int i = 0; i < nGet; i++) {
						new String(barray);
					}
				}
				red += read;
			} while (red < size);
			channel.close();
			file.close();
		}
	}

	// Take the edi file from the URL path and read into a stringbuilder
	private void ReadFileToBuffer(URL edifilepath) throws IOException {

		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(edifilepath.openStream()))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		}
		edilines = builder.toString();
	}

	private String[] SplitLines(String input) throws IOException {
		String[] splitlines = edilines.split(Character.toString(this.fileproperties.getSegmentTerminator()));
		String[] delimitedlines = new String[splitlines.length];
		for (int i = 0; i < splitlines.length; i++) {
			delimitedlines[i] = splitlines[i] + this.fileproperties.getSegmentTerminator();
		}
		return delimitedlines;
	}

	/*
	 * Copy the range of ISA-IEA section from the String array based on the start
	 * and end index and write to a temp ISA-IEA file Doing this instead of
	 * preserving the data in memory since we are dealing with large file sizes
	 * here.
	 */
	private void CopyISAIEASegment(String[] arr, int[] indices) throws FileNotFoundException, IOException {
		CreateInterchangeControlFolder(datetimefolder.getPath(), fileproperties.getISAIEAFolderName());
		String[] copy = Arrays.copyOfRange(arr, indices[0], indices[1]);
		try (FileOutputStream output = new FileOutputStream(
				String.format(isafolder.getPath() + "/" + "ISA-IEA_%s.txt", indices[2]))) {
			for (int j = 0; j < copy.length; j++) {
				if (copy[j] != null) {
					output.write(copy[j].getBytes());
				}
			}
		}
	}

	/*
	 * Copy the range of GS-GE section from the String array based on the start and
	 * end index and write to a temp GS-GE file Doing this instead of preserving the
	 * data in memory since we are dealing with large file sizes here.
	 */
	private void CopyGSGESegment(String[] arr, int[] indices) throws FileNotFoundException, IOException {
		CreateFunctionalGroupFolders(isafolder.getPath(), this.fileproperties.getGSGEFolderName());
		String[] copy = Arrays.copyOfRange(arr, indices[0], indices[1]);
		try (FileOutputStream output = new FileOutputStream(
				String.format(gsfolder.getPath() + "/" + "GS-GE_%s.txt", indices[2]))) {
			for (int j = 0; j < copy.length; j++) {
				if (copy[j] != null) {
					output.write(copy[j].getBytes());
				}
			}
		}
	}

	/*
	 * Copy the range of ST-SE section from the String array based on the start and
	 * end index and write to a temp ST-SE file Doing this instead of preserving the
	 * data in memory since we are dealing with large file sizes here.
	 */
	private void CopySTSESegment(String[] arr, int[] indices) throws FileNotFoundException, IOException {
		CreateTransactionalSetFolders(gsfolder.getPath(), this.fileproperties.getSTSEFolderName());
		String[] copy = Arrays.copyOfRange(arr, indices[0], indices[1]);
		try (FileOutputStream output = new FileOutputStream(
				String.format(stfolder.getPath() + "/" + "ST-SE_%s.txt", indices[2]))) {
			for (int j = 0; j < copy.length; j++) {
				if (copy[j] != null) {
					output.write(copy[j].getBytes());
				}
			}
		}
	}

	/*
	 * Reads in the array of the ST-SE message and based on the start and end index
	 * values extracts the CLM section and write to a split files
	 */
	private void CopyCLMLineInformation(String[] arr, int[] indices) throws IOException {
		StringBuilder finaloutput = null;

		// Create the folders to output all the individual claims; this is added to the
		// parent directory(datetime)
		CreateFinalOutputFolders(datetimefolder.getPath());
		int i = 0;
		if (indices.length > 0)
			do {
				finaloutput = new StringBuilder();
				// Read and append the header in which contains the ISA-GS-ST segments
				finaloutput.append(ReadHeaderInformation(arr));
				String[] copy = Arrays.copyOfRange(arr, indices[i], indices[i + 1]);
				for (int j = 0; j < copy.length; j++) {
					String[] arr1 = copy[j].split("~");
					for (String str : arr1) {
						finaloutput.append(str);
						finaloutput.append("~");
					}
				}
				// Read and append the final piece of the edi file; i.e. the footer; before
				// beginning to write
				// the file
				finaloutput.append(ReadFooterInformation(arr));
				try (FileOutputStream output = new FileOutputStream(
						clmfolder.getPath() + "/" + String.format("TX_%s.txt", i))) {
					output.write(finaloutput.toString().getBytes());
				}
				finaloutput = null;
				i++;
			} while (i < indices.length - 1);

	}

	/* First read the ISA-IEA segment and write it to a file for next processing */
	private void SplitISAIEASegment(String[] arr) throws Exception {
		int count = 0;
		int startindex = 0;
		int endindex = 0;
		boolean isfoundisa = false;
		boolean isfoundiea = false;

		int[] indices = new int[3];
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].substring(0, 4).equals("ISA*")) {
				startindex = i;
				isfoundisa = true;
				indices[0] = startindex;
			}
			if (arr[i].substring(0, 4).equals("IEA*")) {
				int j = i;
				endindex = j + 1; // Need to include the IEA segment also
				indices[1] = endindex;
				isfoundiea = true;
				indices[2] = count++;
			}
			if (isfoundisa && isfoundiea) {
				CopyISAIEASegment(arr, indices);
				isfoundisa = false;
				isfoundiea = false;
			} else if (!isfoundisa && !isfoundiea) {
				throw new Exception("ISA-IEA segment doesnt not exists");
			}
		}
	}

	/* First read the GS-GE segment and write it to a file for next processing */
	private void ReadFunctionalGroup(String[] arr) throws FileNotFoundException, IOException {
		int count = 0;
		int startindex = 0;
		int endindex = 0;
		boolean isfoundgs = false;
		boolean isfoundge = false;

		int[] indices = new int[3];

		for (int i = 0; i < arr.length; i++) {
			if (arr[i].substring(0, 3).equals("GS*")) {
				int l = i;
				startindex = l - 1;
				isfoundgs = true;
				indices[0] = startindex;

			}
			if (arr[i].substring(0, 3).equals("GE*")) {
				int j = i;
				endindex = j + 2; // Need to include the IEA segment also
				indices[1] = endindex;
				isfoundge = true;

				indices[2] = count++;
			}
			if (isfoundgs && isfoundge) {
				CopyGSGESegment(arr, indices);
				isfoundgs = false;
				isfoundge = false;
			}
		}
	}

	/*
	 * Read the header information from the edi message for reconstruction e.g. from
	 * the beginning of the edi message to the beginning of the first claim(CLM)
	 * segment not including the first claim line e.g ISA-GS-ST-<beginning of the
	 * CLM line>
	 */
	private String ReadHeaderInformation(String[] arr) {
		StringBuilder header = new StringBuilder();
		int i = 0;

		do {
			header.append(arr[i]);
			i++;
		} while (i < arr.length && !arr[i].substring(0, 4).equals("CLM*"));
		header.append(arr[i - 1]);
		return header.toString();
	}

	/*
	 * Read the footer information from the edi message for reconstruction e.g. from
	 * the end of the last CLM segment to the end of the file including
	 * <endofCLM>..SE..GE..IEA
	 */
	private String ReadFooterInformation(String[] arr) {
		StringBuilder footer = new StringBuilder();
		int count = 0;

		for (int i = 0; i < arr.length; i++) {
			if (arr[i].substring(0, 3).equals("SE*")) {
				count = i;
			}
		}
		do {
			footer.append(arr[count]);
			count++;
		} while (count <= arr.length && !arr[count].substring(0, 4).equals("IEA*"));
		footer.append(arr[count]);
		return footer.toString();
	}

	private void ReadClaimandLineLevel(String arr[]) throws FileNotFoundException, IOException {
		int count = 0;

		for (int i = 0; i < arr.length; i++) {
			if (arr[i].substring(0, 4).equals("CLM*")) {
				count++;
			}
		}
		// Read the count of the CLM lines that are found; and add one for the SE
		// segment
		int[] indices = new int[count + 1];
		int j = 0;

		for (int i = 0; i < arr.length; i++) {
			if (arr[i].substring(0, 4).equals("CLM*")) {
				indices[j] = i;
				j++;
			}
			// Read the end segment; the last claim should end with SE
			if (arr[i].substring(0, 3).equals("SE*")) {
				indices[j] = i;
			}
		}
		// Copy the Claim(CLM) segments using the beginning of a section to end of a
		// section into a file
		CopyCLMLineInformation(arr, indices);
		DeleteTempDirectories(true);
	}

	/* First read the GS-GE segment and write it to a file for next processing */
	private void ReadTransactionSet(String[] arr) throws IOException {
		int count = 0;
		int startindex = 0;
		int endindex = 0;
		boolean isfoundst = false;
		boolean isfoundse = false;

		int[] indices = new int[3];

		for (int i = 0; i < arr.length; i++) {
			if (arr[i].substring(0, 3).equals("ST*")) {
				int l = i;
				startindex = l - 2;
				isfoundst = true;
				indices[0] = startindex;

			}
			if (arr[i].substring(0, 3).equals("SE*")) {
				int j = i;
				endindex = j + 3; // Need to include the IEA segment also
				indices[1] = endindex;
				isfoundse = true;

				indices[2] = count++;
			}
			if (isfoundst && isfoundse) {
				CopySTSESegment(arr, indices);
				isfoundst = false;
				isfoundse = false;
			}
		}
	}

	private void ReadISAIEATempFolder() throws Exception {

		StringBuilder gs = new StringBuilder();
		List<String> isafiles = new ArrayList<String>();
		// Check if the file exists
		File isatempfolder = new File(isafolder.getPath());
		if (!isatempfolder.exists()) {
			throw new FileNotFoundException("The ISA-IEA directory and/or file seems to be missing");
		}
		// Read the directory if it exists and then
		File[] lstisatempfiles = isatempfolder.listFiles();

		for (File isatempfile : lstisatempfiles) {
			if (!isatempfile.isFile()) {
				throw new Exception("File found is invalid; exiting program");
			}
			isafiles.add(isatempfile.getName());

			String isafilenamewoextension = null;
			String[] isafilename = null;
			for (String isafile : isafiles) {

				isafilenamewoextension = isafile.substring(0, isafile.lastIndexOf('.'));
				isafilename = isafilenamewoextension.split("_");
			}

			try (FileInputStream input = new FileInputStream(
					String.format(isafolder.getPath() + "/" + "ISA-IEA_%s.txt", isafilename[1]))) {
				int content = 0;
				while ((content = input.read()) != -1) {
					gs.append((char) content);
				}

				String[] gsarr = SplitLines(gs.toString());
				ReadFunctionalGroup(gsarr);
			}
		}
	}

	private void ReadSTSETempFolder() throws Exception {

		StringBuilder st = new StringBuilder();
		List<String> gsfiles = new ArrayList<String>();
		// Check if the file exists
		File gstempfolder = new File(gsfolder.getPath());
		if (!gstempfolder.exists()) {
			throw new FileNotFoundException("The GS-GE directory and/or file seems to be missing");
		}
		// Read the directory if it exists and then
		File[] lstgstempfiles = gstempfolder.listFiles();

		for (File gstempfile : lstgstempfiles) {
			if (!gstempfile.isFile()) {
				throw new Exception("File found is invalid; exiting program");
			}

			gsfiles.add(gstempfile.getName());

			String gsfilenamewoextension = null;
			String[] gsfilename = null;
			for (String gsfile : gsfiles) {
				gsfilenamewoextension = gsfile.substring(0, gsfile.lastIndexOf('.'));
				gsfilename = gsfilenamewoextension.split("_");
			}

			try (FileInputStream input = new FileInputStream(
					String.format(gsfolder.getPath() + "/" + "GS-GE_%s.txt", gsfilename[1]))) {
				int content = 0;
				while ((content = input.read()) != -1) {
					st.append((char) content);
				}

				String[] starr = SplitLines(st.toString());
				ReadTransactionSet(starr);
			}
		}
	}

	private void ReadClaimandLineLevels() throws Exception {

		StringBuilder claims = new StringBuilder();
		List<String> stfiles = new ArrayList<String>();
		// Check if the file exists
		File sttempfolder = new File(stfolder.getPath());
		if (!sttempfolder.exists()) {
			throw new FileNotFoundException("The ST-SE directory and/or file seems to be missing");
		}
		// Read the directory if it exists and then
		File[] lststtempfiles = sttempfolder.listFiles();

		for (File sttempfile : lststtempfiles) {
			if (!sttempfile.isFile()) {
				throw new Exception("File found is invalid; exiting program");
			}

			stfiles.add(sttempfile.getName());

			String stfilenamewoextension = null;
			String[] stfilename = null;
			for (String stfile : stfiles) {
				stfilenamewoextension = stfile.substring(0, stfile.lastIndexOf('.'));
				stfilename = stfilenamewoextension.split("_");
			}

			try (FileInputStream input = new FileInputStream(
					String.format(stfolder.getPath() + "/" + "ST-SE_%s.txt", stfilename[1]))) {
				int content = 0;
				while ((content = input.read()) != -1) {
					claims.append((char) content);
				}

				String[] headerarr = SplitLines(claims.toString());
				ReadClaimandLineLevel(headerarr);
			}
		}
	}
}