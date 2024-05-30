package dev.xframe.protoc4j;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;

public class ProtocConfigComponent {

    final JPanel panel;

    final JBTextField outdir = new JBTextField();
    final JBTextField options = new JBTextField();
    final JBCheckBox showCmd = new JBCheckBox();
    final TextFieldWithBrowseButton protoc = new TextFieldWithBrowseButton();

    public ProtocConfigComponent() {
        protoc.addBrowseFolderListener(new TextBrowseFolderListener(new FileChooserDescriptor(true, false, false, false, false, false)));
        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Protoc"), protoc, 1, false)
                .addLabeledComponent(new JBLabel("OutDir"), outdir, 1, false)
                .addLabeledComponent(new JBLabel("Options"), options, 1, false)
                .addLabeledComponent(new JBLabel("ShowCmd"), showCmd, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

}
