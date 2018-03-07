package org.null0.edi.debatcher.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.null0.edi.debatcher.Config;
import org.null0.pojo.MetadataJson;

public class TestMetadataJsonLogger {

	// TODO: why are we hardcoding here?
	String fileName = "/home/developer/lnxshare/test.json";

	@Before
	public void setup() throws IOException {
		String baseDir = Config.getSharedBaseDirPath();
		if (baseDir != null) {
			fileName = baseDir + "test.json";
		}
	}

	@Test
	public void testJavaToJson() throws Exception {

		File statText = new File(fileName);
		FileOutputStream os = new FileOutputStream(statText);
		OutputStreamWriter osw = new OutputStreamWriter(os);
		Writer w = new BufferedWriter(osw);

		MetadataJson meta = new MetadataJson();
		meta.setTabName("ISA");
		meta.setOperation("INSERT");
		Map<String, String> data = new HashMap<String, String>();
		data.put("ISASegment", "232:232:232:232");
		data.put("ISA13", "isa13value");
		meta.setData(data);

		ObjectMapper metaObjMapper = new ObjectMapper();
		String str = metaObjMapper.writeValueAsString(meta);
		w.write(str);

		meta = new MetadataJson();
		meta.setTabName("ISA");
		meta.setOperation("UPDATE");
		data = new HashMap<String, String>();
		data.put("ISASegment", "IAMISASEGMENT");
		data.put("ISA13", "isa13valueyes");
		meta.setData(data);
		int i = 1;
		while (i < 100) {
			str = metaObjMapper.writeValueAsString(meta);
			w.write(System.getProperty("line.separator"));
			w.write(str);
			i++;
		}
		w.close();
	}

	@Test
	public void testJsonToJava() throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		FileInputStream fstream = new FileInputStream(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;

		while ((strLine = br.readLine()) != null) {

			MetadataJson meta = mapper.readValue(strLine, MetadataJson.class);
			System.out.println(meta);
		}
		br.close();

	}

}
