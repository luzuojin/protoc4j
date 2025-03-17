package dev.xframe.protoc4j;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.Strings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class ProtocConfigurable implements Configurable {

    private final Project project;

    private ProtocConfigComponent component;

    public ProtocConfigurable(Project project) {
        this.project = project;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Protoc Config";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.outdir;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return (component = new ProtocConfigComponent()).panel;
    }

    @Override
    public boolean isModified() {
        ProtocConfigState state = ProtocConfigState.getInstance(project);
        return state.showCmd != component.showCmd.isSelected()
            || modified(state.protodir, component.protodir.getText())
            || modified(state.outdir, component.outdir.getText())
            || modified(state.protoc, component.protoc.getText())
            || modified(String.join(";", state.options), component.options.getText());
    }

    public boolean modified(String origin, String current) {
        return !(Strings.isEmptyOrSpaces(origin) ? Strings.isEmptyOrSpaces(current) : origin.equals(current));
    }

    @Override
    public void apply() {
        String text;
        ProtocConfigState state = ProtocConfigState.getInstance(project);
        state.protoc = Strings.isEmptyOrSpaces(text = component.protoc.getText()) ? null : text.trim();
        state.protodir = Strings.isEmptyOrSpaces(text = component.protodir.getText()) ? null : text.trim();
        state.outdir = Strings.isEmptyOrSpaces(text = component.outdir.getText()) ? null : text.trim();
        state.options = Strings.isEmptyOrSpaces(text = component.options.getText()) ? Collections.emptyList() : Arrays.stream(text.trim().split(";")).map(String::trim).collect(Collectors.toList());
        state.showCmd = component.showCmd.isSelected();
    }

    @Override
    public void reset() {
        ProtocConfigState state = ProtocConfigState.getInstance(project);
        component.protoc.setText(state.protoc);
        component.protodir.setText(state.protodir);
        component.outdir.setText(state.outdir);
        component.options.setText(String.join(";", state.options));
        component.showCmd.setSelected(state.showCmd);
    }

    @Override
    public void disposeUIResources() {
        component = null;
    }

}
