package org.example.ffmpeg;

import java.io.File;

public class FFMPEGCleanup {
    private Runnable cleanupTmpFiles(final String tmpFilePath) {
        return () -> {
            File tmpFile = new File(tmpFilePath);
            File tmpFileList = new File(tmpFilePath.replace(".opus", ".list"));
            System.out.println("Cleaning up " + tmpFilePath + ", status: " + tmpFile.delete());
            System.out.println("Cleaning up " + tmpFileList + ", status: " + tmpFileList.delete());
        };
    }
}
