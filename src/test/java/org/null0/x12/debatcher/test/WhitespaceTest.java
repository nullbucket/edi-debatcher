package org.null0.x12.debatcher.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Test;
import org.null0.x12.debatcher.Config;
import org.null0.x12.debatcher.Debatcher;
import org.null0.x12.debatcher.DefaultMetadata;
import org.null0.x12.debatcher.DefaultValidator;

public class WhitespaceTest {
	private static final boolean doClean = true;
	private Debatcher debatcher;

	@AfterClass
	public static void tearDown() {
		if (doClean) {
			TestFileCleaner.clean(".edi.txt");			
		}
	}

	@Test
	public void testIgnoreWhitespace() throws Exception {
		test("Whitespaces", true);
	}
	
	@Test
	public void testWhitespace() throws Exception {
		test("Whitespaces", false);
		// TODO: verify extra whitespace (lines) in .Whitespaces_00001.edi.txt
	}
	
	private Config createMockConfig(boolean ignoreWhitespaceBetweenSegments) {
		Config mockConfig = mock(Config.class);
		when(mockConfig.ignoreWhitespaceBetweenSegments()).thenReturn(ignoreWhitespaceBetweenSegments);
		when(mockConfig.getBufferSize()).thenReturn(1024);
		when(mockConfig.getOutputDirectory()).thenReturn(Paths.get("./"));
		when(mockConfig.getValidReceivers()).thenReturn(new String[] {});
		when(mockConfig.getValidSenders()).thenReturn(new String[] {});
		when(mockConfig.willRejectOnValidationError()).thenReturn(false); // TODO: if this true then we we get localized 837 HC error ""Functional Group Not Supported"
		when(mockConfig.willUpdateTransactionId()).thenReturn(false);
		return mockConfig;		
	}
	
	private InputStream getInputStream(String name) throws Exception {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream("files/" + name + ".txt");
	}

	private void test(String name, boolean ignoreWhitespaceBetweenSegments) throws Exception {
		Config config = createMockConfig(ignoreWhitespaceBetweenSegments);
		debatcher = new Debatcher(config, new DefaultValidator(config),	new DefaultMetadata());
		debatcher.debatch(name, getInputStream(name));
	}
}

