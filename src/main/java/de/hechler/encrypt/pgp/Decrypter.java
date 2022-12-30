package de.hechler.encrypt.pgp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.util.io.Streams;
import org.pgpainless.PGPainless;
import org.pgpainless.decryption_verification.ConsumerOptions;
import org.pgpainless.decryption_verification.DecryptionStream;
import org.pgpainless.key.protection.PasswordBasedSecretKeyRingProtector;
import org.pgpainless.key.protection.SecretKeyRingProtector;
import org.pgpainless.util.Passphrase;

import de.hechler.encrypt.utils.ChecksumOutputStream;
import de.hechler.encrypt.utils.CommentReadingChecksumInputStream;

public class Decrypter {

	private PGPSecretKeyRing privateKey;
	private SecretKeyRingProtector privateKeyPassphrase;
	
	public static class DecryptResult {
		public long sourceFilesize;
		public String sourceSHA256;
		public long targetFilesize;
		public String targetSHA256;
		public List<String> comments;
		public DecryptResult(long sourceFilesize, String sourceSHA256, long targetFilesize, String targetSHA256, List<String> comments) {
			this.sourceFilesize = sourceFilesize;
			this.sourceSHA256 = sourceSHA256;
			this.targetFilesize = targetFilesize;
			this.targetSHA256 = targetSHA256;
			this.comments = comments;
		}
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("{");
			result.append("\"sourceFilesize\":").append(Long.toString(sourceFilesize)).append(",");
			result.append("\"sourceSHA256\":\"").append(sourceSHA256).append("\",");
			result.append("\"targetFilesize\":").append(Long.toString(targetFilesize)).append(",");
			result.append("\"targetSHA256\":\"").append(targetSHA256).append("\",");
				result.append("\"comments\":[");
				String seperator = "";
				for (String comment:comments) {
					result.append(seperator).append("\"").append(comment.replace('"', '\'')).append("\"");
					seperator = ",";
				}
				result.append("]");
			result.append("}");
			return result.toString();
		}
		
	}
	
	public Decrypter(Path privateKeyFilename, String passphrase) { 
		try {
			String privateKeyText = new String(Files.readAllBytes(privateKeyFilename));
			this.privateKey = PGPainless.readKeyRing().secretKeyRing(privateKeyText);
			this.privateKeyPassphrase = PasswordBasedSecretKeyRingProtector.forKey(privateKey, Passphrase.fromPassword(passphrase));
		} catch (IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	public Decrypter(String privateKeyText, String passphrase) {
		try {
			this.privateKey = PGPainless.readKeyRing().secretKeyRing(privateKeyText);
			this.privateKeyPassphrase = PasswordBasedSecretKeyRingProtector.forKey(privateKey, Passphrase.fromPassword(passphrase));
		} catch (IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	public DecryptResult decrypt(Path inputFilename, Path outputFilename) {
		try {
			try (InputStream in = new FileInputStream(inputFilename.toFile())) {
				try (OutputStream out = new FileOutputStream(outputFilename.toFile())) {
					return decrypt(in, out);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e.toString(), e);
		}
		
	}

	
	public DecryptResult decrypt(InputStream encryptedInputStream, OutputStream outputStream) {
		try {
			CommentReadingChecksumInputStream cin = new CommentReadingChecksumInputStream("SHA-256", encryptedInputStream);
			ChecksumOutputStream cout = new ChecksumOutputStream("SHA-256", outputStream);
			
	        DecryptionStream decryptionStream = PGPainless.decryptAndOrVerify()
	                .onInputStream(cin)
	                .withOptions(
	                		new ConsumerOptions()
	                		.addDecryptionKey(privateKey, privateKeyPassphrase)
	                );
	
	        Streams.pipeAll(decryptionStream, cout);
	        decryptionStream.close();
	        long sourceFilesize = cin.getSize();
	        String sourceSHA256 = cin.getMD();
	        long targetFilesize = cout.getSize();
	        String targetSHA256 = cout.getMD();
	        List<String> comments = cin.getComments();
	        return new DecryptResult(sourceFilesize, sourceSHA256, targetFilesize, targetSHA256, comments);
	        // Information about the encryption (algorithms, detached signatures etc.)
//	        EncryptionResult result = encryptionStream.getResult();
//	        System.out.println(result.getEncryptionAlgorithm());
		} catch (IOException | PGPException e) {
			throw new RuntimeException(e.toString(), e);
		}

	}
	
}
