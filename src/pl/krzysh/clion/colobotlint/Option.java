package pl.krzysh.clion.colobotlint;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.panels.VerticalLayout;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Option implements Configurable {
    private boolean modified = false;
    private JTextField binaryPath;
    private JTextField buildPath;
    private OptionModifiedListener listener = new OptionModifiedListener(this);

    public static final String OPTION_KEY_BINARY_PATH = "colobotLintBinaryPath";
    public static final String OPTION_KEY_BUILD_PATH = "colobotLintBuildPath";

    @Nls
    @Override
    public String getDisplayName() {
        return "colobot-lint";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JPanel jPanel = new JPanel();

        VerticalLayout verticalLayout = new VerticalLayout(1, SwingConstants.LEFT);
        jPanel.setLayout(verticalLayout);

        binaryPath = new JTextField(50);
        buildPath = new JTextField(50);

        reset();

        binaryPath.getDocument().addDocumentListener(listener);
        buildPath.getDocument().addDocumentListener(listener);

        jPanel.add(new JLabel("colobot-lint path:"));
        jPanel.add(binaryPath);
        jPanel.add(new JLabel("Build path:"));
        jPanel.add(buildPath);

        return jPanel;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        Settings.set(OPTION_KEY_BINARY_PATH, binaryPath.getText());
        Settings.set(OPTION_KEY_BUILD_PATH, buildPath.getText());
        modified = false;
    }

    @Override
    public void reset() {
        binaryPath.setText(Settings.get(OPTION_KEY_BINARY_PATH));
        buildPath.setText(Settings.get(OPTION_KEY_BUILD_PATH));
        modified = false;
    }

    @Override
    public void disposeUIResources() {
        binaryPath.getDocument().removeDocumentListener(listener);
        buildPath.getDocument().removeDocumentListener(listener);
    }

    private static class OptionModifiedListener implements DocumentListener {
        private final Option option;

        public OptionModifiedListener(Option option) {
            this.option = option;
        }

        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            option.setModified(true);
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            option.setModified(true);
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
            option.setModified(true);
        }
    }
}
