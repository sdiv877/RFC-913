package server;

import utils.Utils;

public class PendingStorFile {
    private String filePath;
    private String bytesToWrite;
    private int maxBytes;
    private String writeMode;

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
}
