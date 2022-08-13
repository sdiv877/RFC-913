package server;

import utils.Utils;

/**
 * Represents a file that is to be written following a STOR and SIZE call
 */
public class PendingStorFile {
    private String filePath;
    private String bytesToWrite;
    private int maxBytes;
    private String writeMode;
    private String transferType;

    public PendingStorFile(String filePath, String writeMode) {
        this.filePath = filePath;
        this.writeMode = writeMode;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setBytesToWrite(String s) {
        this.bytesToWrite = s;
    }

    public String getClampedBytesToWrite() {
        return Utils.safeSubstring(bytesToWrite, 0, maxBytes);
    }

    public void setMaxBytes(int maxBytes) {
        this.maxBytes = maxBytes;
    }

    public String getWriteMode() {
        return this.writeMode;
    }

    public String getTransferType() {
        return this.transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }
}
