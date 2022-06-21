package config;

import com.intellij.openapi.options.Configurable;
import model.SSHSettingsState;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import ui.SSHSettingsComponent;

import javax.swing.*;

public class SSHSettingsConfigurable implements Configurable {

    private SSHSettingsComponent sshSettingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "SSH 设置";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        sshSettingsComponent = new SSHSettingsComponent();
        return sshSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        SSHSettingsState settings = SSHSettingsState.getInstance();
        boolean modified = !sshSettingsComponent.getUsernameText().equals(settings.username);
        modified |= !sshSettingsComponent.getPortText().equals(settings.port);
        modified |= !sshSettingsComponent.getUsernameText().equals(settings.username);
        modified |= !sshSettingsComponent.getPasswordText().equals(settings.password);
        modified |= !sshSettingsComponent.getRemotePathText().equals(settings.remotePath);
        return modified;
    }

    @Override
    public void apply() {
        SSHSettingsState settings = SSHSettingsState.getInstance();
        settings.host = sshSettingsComponent.getHostText();
        settings.port = sshSettingsComponent.getPortText();
        settings.username = sshSettingsComponent.getUsernameText();
        settings.password = sshSettingsComponent.getPasswordText();
        settings.remotePath = sshSettingsComponent.getRemotePathText();
    }

    @Override
    public void reset() {
        SSHSettingsState settings = SSHSettingsState.getInstance();
        sshSettingsComponent.setHostText(settings.host);
        sshSettingsComponent.setPortText(settings.port);
        sshSettingsComponent.setUsernameText(settings.username);
        sshSettingsComponent.setPasswordText(settings.password);
        sshSettingsComponent.setRemotePathField(settings.remotePath);
    }

    @Override
    public void disposeUIResources() {
        sshSettingsComponent = null;
    }

}