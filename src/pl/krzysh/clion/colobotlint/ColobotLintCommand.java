package pl.krzysh.clion.colobotlint;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ColobotLintCommand {
    private static class ReadProcessOutput implements Runnable {
        private final InputStream inputStream;
        private final StringBuffer outputString;

        public ReadProcessOutput(InputStream inputStream, StringBuffer outputString) {
            this.inputStream = inputStream;
            this.outputString = outputString;
        }

        @Override
        public void run() {
            BufferedReader outStream = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line;
                while ((line = outStream.readLine()) != null) {
                    outputString.append(line + "\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public static String execute(Project project, String... arg) throws IOException {
        List<String> args = new ArrayList<String>();

        String colobotLintBinary = Settings.get(Option.OPTION_KEY_BINARY_PATH);

        if (colobotLintBinary == null || colobotLintBinary.isEmpty()) {
            StatusBar.Info.set("colobot-lint binary path is not configured", project);
            return "";
        }

        args.add(colobotLintBinary);
        Collections.addAll(args, arg);
        System.out.println(Arrays.toString(args.toArray(new String[args.size()])));

        File cpplintWorkingDirectory = new File(project.getBaseDir().getCanonicalPath());
        final Process process = Runtime.getRuntime().exec(
                args.toArray(new String[args.size()]), null, cpplintWorkingDirectory);

        final StringBuffer outString = new StringBuffer();
        Thread outThread = new Thread(new ReadProcessOutput(process.getInputStream(), outString));
        //Thread errorThread = new Thread(new ReadProcessOutput(process.getErrorStream(), outString));
        outThread.start();
        //errorThread.start();

        try {
            outThread.join();
            //errorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return outString.toString();
    }
}
