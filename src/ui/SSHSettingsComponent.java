package ui;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import util.SSHUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SSHSettingsComponent {
    private final JPanel mainPanel;
    private final JBTextField hostTextField = new JBTextField("主机名或 IP 地址...");
    private final JBTextField portTextField = new JBTextField("远程主机端口号...");
    private final JBTextField usernameTextField = new JBTextField("用户名...");
    private final JBPasswordField passwordField = new JBPasswordField();
    private final JBTextField remotePathField = new JBTextField();
    private final JButton testButton = new JButton("Test Connection");

    public SSHSettingsComponent() {
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Host: "), hostTextField, 1, false)
                .addLabeledComponent(new JBLabel("Port: "), portTextField, 1, false)
                .addLabeledComponent(new JBLabel("Username: "), usernameTextField, 1, false)
                .addLabeledComponent(new JBLabel("Passwd: "), passwordField, 1, false)
                .addComponent(testButton)
                .addSeparator()
                .addLabeledComponent(new JBLabel("Remote path: "), remotePathField, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String host = getHostText();
                String port = getPortText();
                String username = getUsernameText();
                String password = getPasswordText();
                if (host == null || "".equals(host.trim())) {
                    Messages.showMessageDialog("Host 不能为空！", "连接测试", Messages.getWarningIcon());
                    return;
                }
                if (port == null || "".equals(port.trim())) {
                    Messages.showMessageDialog("Port 不能为空！", "连接测试", Messages.getWarningIcon());
                    return;
                }
                if (username == null || "".equals(username.trim())) {
                    Messages.showMessageDialog("Username 不能为空！", "连接测试", Messages.getWarningIcon());
                    return;
                }
                if (password == null || "".equals(password.trim())) {
                    Messages.showMessageDialog("Host 不能为空！", "连接测试", Messages.getWarningIcon());
                    return;
                }
                boolean connect = SSHUtil.connect(host, Integer.parseInt(port), username, password);
                if (connect) {
                    Messages.showMessageDialog(username + " 连接成功！", "连接测试", Messages.getInformationIcon());
                } else {
                    Messages.showMessageDialog(username + " 连接失败！", "连接测试", Messages.getErrorIcon());
                }
                SSHUtil.closeConnect();
            }
        });
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public String getHostText() {
        return hostTextField.getText();
    }

    public void setHostText(String newText) {
        hostTextField.setText(newText);
    }

    public String getPortText() {
        return portTextField.getText();
    }

    public void setPortText(String newText) {
        portTextField.setText(newText);
    }

    public String getUsernameText() {
        return usernameTextField.getText();
    }

    public void setUsernameText(String newText) {
        usernameTextField.setText(newText);
    }

    public String getPasswordText() {
        return new String(passwordField.getPassword());
    }

    public void setPasswordText(String newText) {
        passwordField.setText(newText);
    }

    public String getRemotePathText(){
        return remotePathField.getText();
    }

    public void setRemotePathField(String newText){
        remotePathField.setText(newText);
    }
}
