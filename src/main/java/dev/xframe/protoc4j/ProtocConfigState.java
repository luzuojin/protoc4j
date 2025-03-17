package dev.xframe.protoc4j;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.Strings;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@State(
        name = "dev.xframe.protoc4j.ProtocConfigState",
        storages = @Storage("Protoc4jPlugin.xml")
)
public class ProtocConfigState implements PersistentStateComponent<ProtocConfigState> {

    static final String[] DefaultOutDirs = new String[]{"src/main/proto", "src/main/java", "src"};

    public String protodir;

    public String outdir;

    public String protoc;

    public List<String> options = Arrays.asList("--experimental_allow_proto3_optional");

    public boolean showCmd;

    public List<String> getPossibleOutDirs() {
        if(Strings.isEmptyOrSpaces(outdir)) {
            return Arrays.asList(DefaultOutDirs);
        }
        return Stream.concat(Stream.of(outdir), Stream.of(DefaultOutDirs)).distinct().collect(Collectors.toList());
    }

    public List<String> getOptions() {
        return options;
    }

    public static ProtocConfigState getInstance(Project project) {
        return project.getService(ProtocConfigState.class);
    }

    @Nullable
    @Override
    public ProtocConfigState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ProtocConfigState state) {
        XmlSerializerUtil.copyBean(this, state);
    }

}
