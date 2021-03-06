package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0xA5
public class NtRenameResponse implements SmbData{

	
	boolean malformed = false;
	byte wordCount; // it must 0x00
	short byteCount; // it must 0x0000
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	@Override
	public boolean isMalformed() {
		// TODO Auto-generated method stub
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	@Override
	public String toString(){
		return String.format("First Level : NtRenameResponse\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s(it must 0x00\n" +
				"byteCount = 0x%s(it must 0x00\n",
				this.malformed,
				this.wordCount,
				this.byteCount);
	}
}
