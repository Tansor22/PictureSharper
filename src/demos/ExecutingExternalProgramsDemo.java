package demos;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ExecutingExternalProgramsDemo {
    public static void main(String[] args) throws Exception {
       ProcessBuilder builder = new ProcessBuilder("java.exe",
                "-jar", "D:\\java_sdk_8.0_161\\mpj-v0_44\\lib\\starter.jar", "-np", "4", "demos.PiComputationMPJ");
        builder.environment().put("MPJ_HOME", "D:\\java_sdk_8.0_161\\mpj-v0_44" );

        builder.redirectErrorStream( true );
        Process process = builder.start();
        try ( BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream())) ) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }

        // waiting
        process.waitFor();
    }
}
