package de.hechler.decryptdownloader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import de.hechler.encrypt.pgp.Encrypter;
import de.hechler.encrypt.pgp.Encrypter.EncryptResult;

class EncrypterTest {

	private static final String TESTDATA_FOLDER = "./testdata"; 
	
	@Test
	void testEncrypterWithPW2() throws IOException {
		Path inputFile = Paths.get(TESTDATA_FOLDER).resolve("input/testdatei.txt");
		Path publicKey = Paths.get("encrypt-key.pub");
		Path outputFile = Paths.get(TESTDATA_FOLDER).resolve("output/testdatei.txt.pgp");
		Files.createDirectories(outputFile.getParent());
		Encrypter enc = new Encrypter(publicKey);
		EncryptResult hashes = enc.encrypt(inputFile, outputFile, "JUnit Test");
		System.out.println(hashes);
		assertEquals(hashes.sourceFilesize, 131);
		assertEquals(hashes.sourceSHA256, "fe6236b4b89def157fb10042abb93f6162b02a9a0fa1beac85f00b097fb7c974");
		// can targetFilesize differ? targetSHA256 is always different.  
		assertTrue((hashes.targetFilesize > 400) && (hashes.targetFilesize < 600));
	}
	
}
