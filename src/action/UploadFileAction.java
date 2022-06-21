package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import model.SSHSettingsState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import util.SSHUtil;

public class UploadFileAction extends AnAction {
    private final static Log LOG = LogFactory.getLog(UploadFileAction.class);

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        try {

            // 获取被选中的文件或目录
            VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());
            String localPath = file.getPath();

            // 获取连接参数配置
            SSHSettingsState state = SSHSettingsState.getInstance();
            String host = state.host;
            String port = state.port;
            String username = state.username;
            String password = state.password;
            String remotePath = state.remotePath;
            // 建立 SSH 连接
            boolean connect = SSHUtil.connect(host, Integer.parseInt(port), username, password);
            // 如果连接失败则给出提示
            if (!connect) {
                Messages.showMessageDialog(project, "远程连接失败，请检查配置信息！", "错误", Messages.getErrorIcon());
            } else {
                // 如果连接成功则上传文件
                SSHUtil.uploadByScp(localPath, remotePath);
                Messages.showMessageDialog(project, "上传文件成功！", "信息", Messages.getInformationIcon());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            Messages.showMessageDialog(project, "上传文件失败！！", "错误", Messages.getErrorIcon());
        }
    }
}
