package model;

import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "SSHSettingsState",
        storages = @Storage("SSHSettingsPlugin.xml")
)
public class SSHSettingsState implements PersistentStateComponent<SSHSettingsState> {

    // 主机名或IP地址
    public String host="";
    // 端口号
    public String port="22";
    // 用户名
    public String username="";
    // 登录密码
    public String password="";
    // 远程路径
    public String remotePath="/tmp/";

    public static SSHSettingsState getInstance() {
        return ServiceManager.getService(SSHSettingsState.class);
    }

    @Nullable
    @Override
    public SSHSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SSHSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}