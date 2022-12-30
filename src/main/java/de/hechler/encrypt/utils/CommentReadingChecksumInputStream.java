package de.hechler.encrypt.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CommentReadingChecksumInputStream extends ChecksumInputStream {
	
	private int maxHeaderSize;
	private byte[] header;
	private int headerSize;
	
	private final static int DEFAULT_MAX_HEADER_SIZE = 4096;

	public CommentReadingChecksumInputStream(String algorithm, InputStream delegte) {
		this(algorithm, delegte, DEFAULT_MAX_HEADER_SIZE);
	}

	public CommentReadingChecksumInputStream(String algorithm, InputStream delegte, int maxHeaderSize) {
		super(algorithm, delegte);
		this.maxHeaderSize = maxHeaderSize;
		this.header = new byte[maxHeaderSize];
		this.headerSize = 0;
	}

	public int read() throws IOException {
		int result = super.read();
		if (headerSize<maxHeaderSize) {
			if (result != -1) {
				header[headerSize] = (byte) result;
				headerSize++;
			}
		}
		return result;
	}

	public int read(byte[] b) throws IOException {
		int result = super.read(b);
		if (headerSize<maxHeaderSize) {
			if (result > 0) {
				int cnt = Math.min(maxHeaderSize - headerSize, result);
				System.arraycopy(b, 0, header, headerSize, cnt);
				headerSize += cnt;
			}
		}
		return result;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int result = super.read(b, off, len);
		if (headerSize<maxHeaderSize) {
			if (result > 0) {
				int cnt = Math.min(maxHeaderSize - headerSize, result);
				System.arraycopy(b, off, header, headerSize, cnt);
				headerSize += cnt;
			}
		}
		return result;
	}

	public List<String> getComments() {
		List<String> result = new ArrayList<>();
		int lineStart = 0;
		int lineEnd = findInHeader(lineStart, (byte) '\n');
		while (lineEnd != -1) {
			String line = new String(header, lineStart, lineEnd-lineStart);
			line = line.replace("\r", "");
			if (line.length()==0) {
				break;
			}
			if (line.startsWith("Comment: ")) {
				result.add(line.substring(9).trim());
			}
			lineStart = lineEnd+1;
			lineEnd = findInHeader(lineStart, (byte) '\n');
		}
		return result;
	}

	private int findInHeader(int fromIndex, byte searchByte) {
		for (int i=fromIndex; i<maxHeaderSize; i++) {
			if (header[i] == searchByte) {
				return i;
			}
		}
		return -1;
	}
}
