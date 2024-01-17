package dev.xframe.protoc4j;

import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.Strings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProtocExecutor {

    public static class ExecResp {
        public final boolean ok;
        public final String msg;
        public ExecResp(boolean ok, String msg) {
            this.ok = ok;
            this.msg = msg;
        }
    }

    /**
     * protoc --proto_path=%s --proto_path=$dependents --experimental_allow_proto3_optional --$options --java_out=%s $.proto
     */
    public static ExecResp exec(ProtocConfigState config, List<String> inputDirs, String outputDir, String protoFile) throws IOException, InterruptedException {
        String protoc = ensureProtoc(config);
        String proto_path = String.join(" ", inputDirs.stream().map(dir->String.format("--proto_path=%s", dir)).collect(Collectors.toList()));
        String proto_options = String.join(" ", config.getOptions());
        String proto_out = String.format("--java_out=%s", outputDir);
        String proto_file = protoFile;
        String proto_cmd = String.join(" ", Arrays.asList(protoc, proto_path, proto_options, proto_out, proto_file));
        return invokeCmd(proto_cmd);
    }

    private static String ensureProtoc(ProtocConfigState config) throws IOException, InterruptedException {
        if(!Strings.isEmpty(config.protoc) && Files.exists(Paths.get(config.protoc))) {
            return config.protoc;
        }
        boolean isWindows = isWindows();
        String protocOriginName = (isWindows ? "protoc.exe" : (isArm() ? "protoc_arm" : "protoc"));
        String protocTargetName = "xframe_" + protocOriginName;
        Path protocPath = Paths.get(FileUtilRt.getTempDirectory(), protocTargetName);
        if(!Files.exists(protocPath)) {//create temporal file
            File temp = FileUtilRt.createTempFile(protocTargetName, null);
            InputStream input = ProtocExecutor.class.getClassLoader().getResourceAsStream(protocOriginName);
            try(FileOutputStream output = new FileOutputStream(temp)) {
                FileUtilRt.copy(input, output);
                output.flush();
            }
            if(!isWindows) {//macos: ensure executable permission
                Runtime.getRuntime().exec(String.format("chmod u+x %s", protocPath.toFile().getPath())).waitFor();
            }
        }
        return protocPath.toFile().getPath();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }
    private static boolean isArm() {
        return Optional.ofNullable(System.getProperty("os.arch")).filter(v -> v.toLowerCase().contains("arm") || v.equalsIgnoreCase("aarch64")).isPresent();
    }


    private static ExecResp invokeCmd(String cmd) throws IOException, InterruptedException {
        Process pr = Runtime.getRuntime().exec(cmd);
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getErrorStream()))) {
            pr.waitFor();
            int status = pr.exitValue();
            StringBuilder sb = new StringBuilder(status).append("\n");
            while(reader.ready())
                sb.append(reader.readLine()).append("\n");
            return status == 0 ? new ExecResp(true, "OK") : new ExecResp(false, sb.toString());
        }
    }

}
