package util;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.TransferListener;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @desc sshj 包的工具类
 */
public class SSHUtil {
    private final static Log LOG = LogFactory.getLog(SSHUtil.class);
    private static SSHClient ssh = null;
    private static Session session = null;

    /**
     * 建立 SSH 连接
     *
     * @param host     主机名或 IP 地址
     * @param port     端口号，通常是 22
     * @param username 登录主机的用户名
     * @param password 登录主机的密码
     *
     * @return 如果连接成功则返回 true，否则返回 false
     */
    public static boolean connect(String host, int port, String username, String password) {
        // 参数校验
        if (host == null || "".equals(host.trim())) {
            throw new IllegalArgumentException("host: 不能为空！");
        }
        if (username == null || "".equals(username.trim())) {
            throw new IllegalArgumentException("username: 不能为空！");
        }
        if (password == null || "".equals(password.trim())) {
            throw new IllegalArgumentException("password: 不能为空！");
        }
        // 尝试建立连接
        try {
            ssh = new SSHClient();
            try {
                ssh.connect(host, port);
            } catch (TransportException e) {
                // 如果连接失败则判断是否是key验证失败，如果是则从输出的异常信息中提取 key 然后设置后重新请求，否则连接失败
                if (e.getDisconnectReason() == DisconnectReason.HOST_KEY_NOT_VERIFIABLE) {
                    ssh = new SSHClient();
                    String msg = e.getMessage();
                    Matcher matcher = Pattern.compile("`((\\w{2}:?){16})`").matcher(msg);
                    if (matcher.find()) {
                        String vc = matcher.group(1);
                        ssh.addHostKeyVerifier(vc);
                        ssh.connect(host, port);
                    }
                } else {
                    LOG.error(e.getMessage(), e);
                    return false;
                }
            }
            ssh.authPassword(username, password);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
        return ssh.getConnection() != null;
    }

    public static void main(String[] args) throws Exception {
//        connect("192.168.3.5","root","root");
        connect("124.70.35.119", 22, "lichunlin", "lichunlin");
//        System.out.println(exec("ls ~"));
//        uploadByScp("C:\\ips\\ipsEcCrawler-locate\\pom.xml", "/home/lichunlin/");
//        uploadByScp("C:\\ips\\ipsEcCrawler-locate\\ips-ec-demo\\src\\test\\java\\com\\demo", "/home/lichunlin/");
//        System.out.println(connect("1.116.40.90", 22, "root", "Tencent.Cloud2021"));
        downloadByScp("/home/lichunlin/", "abcdef");
    }

    /**
     * 建立连接之后，开启会话
     *
     * @throws Exception 如果未建立连接，则抛出该异常
     */
    public static void startSession() throws Exception {
        if (ssh == null) {
            throw new Exception("ssh 未开启连接！");
        }
        if (session == null) {
            // 开启会话
            session = ssh.startSession();
        }
    }

    /**
     * 执行 shell 命令
     *
     * @param command shell 命令
     *
     * @return 命令的执行结果
     *
     * @throws Exception 如果未开启会话则抛出该异常
     */
    public static String exec(String command) throws Exception {
        // 参数校验
        if (command == null || "".equals(command.trim())) {
            throw new IllegalArgumentException("command 不能为空！");
        }
        // 开启会话
        startSession();
        if (session == null) {
            throw new Exception("开启 session 失败！");
        }
        // 执行命令
        final Session.Command cmd = session.exec(command);
        // 获取命令的执行结果
        String response = IOUtils.readFully(cmd.getInputStream()).toString();
        // 关闭会话
        closeSession();
        // 返回命令的执行结果
        return response;
    }

    /**
     * 通过 SCP 方式上传文件或目录
     *
     * @param localPath  本地文件或目录路径
     * @param remotePath 远程路径，通常是一个目录
     *
     * @throws Exception 如果上传失败则抛出该异常
     */
    public static void uploadByScp(String localPath, String remotePath) throws Exception {
        // 参数校验
        if (localPath == null || "".equals(localPath.trim())) {
            throw new IllegalArgumentException("localPath: 不能为空！");
        }
        if (remotePath == null || "".equals(remotePath.trim())) {
            throw new IllegalArgumentException("remotePath: 不能为空！");
        }
        // 如果本地文件不存在，则抛出参数异常
        File file = new File(localPath);
        boolean isExist = file.exists();
        if (!isExist) {
            throw new IllegalArgumentException("localPath: 该文件在本地不存在！");
        }
        // 开启会话
        startSession();
        ssh.useCompression();
        SCPFileTransfer scpFileTransfer = ssh.newSCPFileTransfer();
        // 设置传输监听器
        scpFileTransfer.setTransferListener(new TransferListener() {
            @Override
            public TransferListener directory(String s) {
                // 第一个参数是目录名
                LOG.info("正在上传目录：" + s);
                // 必须返回，否则抛出空指针异常
                return this;
            }

            @Override
            public StreamCopier.Listener file(String s, long l) {
                // 第一个参数是文件名；第二个参数是文件大小
                LOG.info("正在上传文件：" + s + "，该文件的大小为：" + l);
                return null;
            }
        });
        // 上传文件
        scpFileTransfer.upload(new FileSystemFile(localPath), remotePath);
        // 关闭会话
        closeSession();
    }

    /**
     * 下载文件
     *
     * @param remotePath 远程文件路径
     * @param localPath  如果不指定目录，则会下载到当前项目所在目录
     *
     * @throws Exception 如果下载失败则抛出异常
     */
    public static void downloadByScp(String remotePath, String localPath) throws Exception {
        if (localPath == null || "".equals(localPath.trim())) {
            throw new IllegalArgumentException("localPath: 不能为空！");
        }
        if (remotePath == null || "".equals(remotePath.trim())) {
            throw new IllegalArgumentException("remotePath: 不能为空！");
        }
        startSession();
        SCPFileTransfer scpFileTransfer = ssh.newSCPFileTransfer();
        scpFileTransfer.setTransferListener(new TransferListener() {
            @Override
            public TransferListener directory(String s) {
                // 第一个参数是目录名
                LOG.info("正在下载目录：" + s);
                // 必须返回，否则抛出空指针异常
                return this;
            }

            @Override
            public StreamCopier.Listener file(String s, long l) {
                // 第一个参数是文件名；第二个参数是文件大小
                LOG.info("正在下载文件：" + s + "，该文件的大小为：" + l);
                return null;
            }
        });
        scpFileTransfer.download(remotePath, new FileSystemFile(localPath));
        closeSession();
    }

    /**
     * 关闭会话
     */
    public static void closeSession() {
        if (session != null) {
            try {
                session.close();
            } catch (TransportException | ConnectionException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 关闭连接
     */
    public static void closeConnect() {
        if (ssh != null) {
            try {
                ssh.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
