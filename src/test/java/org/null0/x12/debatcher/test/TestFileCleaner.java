package org.null0.x12.debatcher.test;

import java.io.File;

import org.null0.x12.debatcher.Config;
import org.null0.x12.debatcher.DefaultConfig;

public class TestFileCleaner {
	public static synchronized boolean clean(String endsWith) {
		try {
			Config config = new DefaultConfig();
			
			File dir = new File(config.getOutputDirectory().toString());
			if (!dir.exists()) {
				return false;
			}

			File[] files = dir.listFiles();
			if (files == null) {
				return false;
			}
			
			for (File f : files) {
				if (f.getName().endsWith(endsWith)) {
					f.delete();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
