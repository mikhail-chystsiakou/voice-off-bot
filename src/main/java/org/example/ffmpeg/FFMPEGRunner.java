package org.example.ffmpeg;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class FFMPEGRunner {
    private static final String COMMAND_PATTERN = "ffmpeg -safe 0 -f concat -i <({0}) -codec copy {1}";
    private static final String FILE_PATTERN = "echo $''file \\''{0}\\''''";

    public static void main(String[] args) throws IOException {
        // ffmpeg -safe 0 -f concat -i <(echo $'file \'/home/bewired/ffmpeg/f1.ogg\'' && echo $'file \'/home/bewired/ffmpeg/f2.ogg\'') -codec copy full.ogg
// Runtime.getRuntime().exec(command);
    }

    private void onWindows() {
        StringJoiner sj = new StringJoiner(" && ");
        sj.add(MessageFormat.format(FILE_PATTERN, "D:/workspace/bewired/voices/test/v1.oga"));
        sj.add(MessageFormat.format(FILE_PATTERN, "D:/workspace/bewired/voices/test/v2.oga"));
        String outputFile = "D:/workspace/bewired/voices/test/full.oga";

        String command = MessageFormat.format(COMMAND_PATTERN, sj.toString(), outputFile);
        System.out.println(command);
    }

    private void onLinux() {

    }
}
