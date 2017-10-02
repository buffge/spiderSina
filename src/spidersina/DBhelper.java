package spidersina;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBhelper {

    private String url = null;
    private String name = "com.mysql.jdbc.Driver";
    private String username = null;
    private String password = null;
    public Connection conn = null;
    public PreparedStatement pst = null;

    /**
     * 初始化jbdc:mysql
     *
     * @param host 主机
     * @param port 端口
     * @param user 用户名
     * @param pwd 密码
     * @param db 数据库名称
     */
    public DBhelper(String host, String port, String user, String pwd, String db) {
        this.url = ("jdbc:mysql://" + host + ":" + port + "/" + db + "?useUnicode=true&characterEncoding=utf8&useSSL=true");
        this.username = user;
        this.password = pwd;
        try {
            Class.forName(this.name);
            this.conn = DriverManager.getConnection(this.url, this.username, this.password);
        } catch (ClassNotFoundException | SQLException localClassNotFoundException) {
        }
    }
}
