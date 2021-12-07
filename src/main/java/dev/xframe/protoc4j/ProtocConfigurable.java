package dev.xframe.protoc4j;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.text.Strings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ProtocConfigurable implements Configurable {

    private ProtocConfigComponent component;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Protoc settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.outdir;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        component = new ProtocConfigComponent();
        return component.panel;
    }

    @Override
    public boolean isModified() {
        ProtocConfigState state = ProtocConfigState.getInstance();
        return modified(state.outdir, component.outdir.getText()) ||
                modified(String.join(";", state.options), component.options.getText()) ||
                modified(state.protoc, component.protoc.getText());
    }

    public boolean modified(String origin, String current) {
        return !(Strings.isEmptyOrSpaces(origin) ? Strings.isEmptyOrSpaces(current) : origin.equals(current));
    }

    @Override
    public void apply() {
        String text;
        ProtocConfigState state = ProtocConfigState.getInstance();
        if(!Strings.isEmptyOrSpaces(text = component.outdir.getText()))
            state.outdir = text.trim();
        if(!Strings.isEmptyOrSpaces(text = component.options.getText()))
            state.options = Arrays.asList(text.trim().split(";")).stream().map(String::trim).collect(Collectors.toList());
        if(!Strings.isEmptyOrSpaces(text = component.protoc.getText()))
            state.protoc = text.trim();
    }

    @Override
    public void reset() {
        ProtocConfigState state = ProtocConfigState.getInstance();
        component.outdir.setText(state.outdir);
        component.options.setText(String.join(";", state.options));
        component.protoc.setText(state.protoc);
    }

    @Override
    public void disposeUIResources() {
        component = null;
    }

}
