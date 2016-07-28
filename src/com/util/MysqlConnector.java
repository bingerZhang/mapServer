package com.util;

import java.sql.*;
import java.util.List;

/**
 * Created by Administrator on 2016/7/28.
 */
public class MysqlConnector {
    private Connection conn = null;
    PreparedStatement statement = null;
    String db_host = "localhost";
    String db_user = "root";
    String db_pwd = "root";

    // connect to MySQL
    void connSQL() {
        String url = "jdbc:mysql://"+ db_host + ":3306/hello?characterEncoding=UTF-8";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, db_user, db_pwd);
        }
        //捕获加载驱动程序异常
        catch (ClassNotFoundException cnfex) {
            System.err.println(
                    "装载 JDBC/ODBC 驱动程序失败。");
            cnfex.printStackTrace();
        }
        //捕获连接数据库异常
        catch (SQLException sqlex) {
            System.err.println("无法连接数据库");
            sqlex.printStackTrace();
        }
    }

    // disconnect to MySQL
    void deconnSQL() {
        try {
            if (conn != null)
                conn.close();
        } catch (Exception e) {
            System.out.println("关闭数据库问题 ：");
            e.printStackTrace();
        }
    }

    // execute selection language
    ResultSet selectSQL(String sql) {
        ResultSet rs = null;
        try {
            statement = conn.prepareStatement(sql);
            rs = statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    // execute insertion language
    boolean insertSQL(String sql) {
        try {
            statement = conn.prepareStatement(sql);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("插入数据库时出错：");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("插入时出错：");
            e.printStackTrace();
        }
        return false;
    }


    public boolean insert_SQLS(String prefix,List<String> values) {

        int size = 0;
        // sql前缀
//        String prefix = "INSERT INTO tb_big_data (count, create_time, random) VALUES ";
        try {
            // 保存sql后缀
            StringBuffer suffix = new StringBuffer();
            // 设置事务为非自动提交
            conn.setAutoCommit(false);
            // Statement st = conn.createStatement();
            // 比起st，pst会更好些
            PreparedStatement pst = conn.prepareStatement("");
            // 外层循环，总提交事务次数
            size = values.size();

            for (int j = 0; j <= size; j++) {
                // 构建sql后缀
                suffix.append("(" + values.get(j) + "),");
            }
            // 构建完整sql
            String sql = prefix + suffix.substring(0, suffix.length() - 1);
            // 添加执行sql
            pst.addBatch(sql);
            // 执行操作
            pst.executeBatch();
            // 提交事务
            conn.commit();

            pst.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("insert size : " + size);
        return true;
    }

    //execute delete language
    boolean deleteSQL(String sql) {
        try {
            statement = conn.prepareStatement(sql);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("插入数据库时出错：");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("插入时出错：");
            e.printStackTrace();
        }
        return false;
    }

    //execute update language
    boolean updateSQL(String sql) {
        try {
            statement = conn.prepareStatement(sql);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("插入数据库时出错：");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("插入时出错：");
            e.printStackTrace();
        }
        return false;
    }

    // show data in ju_users
    void layoutStyle2(ResultSet rs) {
        System.out.println("-----------------");
        System.out.println("执行结果如下所示:");
        System.out.println("-----------------");
        System.out.println(" 用户ID" + "/t/t" + "淘宝ID" + "/t/t" + "用户名" + "/t/t" + "密码");
        System.out.println("-----------------");
        try {
            while (rs.next()) {
                System.out.println(rs.getInt("ju_userID") + "/t/t"
                        + rs.getString("taobaoID") + "/t/t"
                        + rs.getString("ju_userName")
                        + "/t/t" + rs.getString("ju_userPWD"));
            }
        } catch (SQLException e) {
            System.out.println("显示时数据库出错。");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("显示出错。");
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();

        String s = "select * from ju_users";
        String insert = "insert into ju_users(ju_userID,TaobaoID,ju_userName,ju_userPWD) values(" + 8329 + "," + 34243 + ",'mm','789')";
        String update = "update ju_users set ju_userPWD =123 where ju_userName= 'mm'";
        String delete = "delete from ju_users where ju_userName= 'mm'";

        if (mysqlConnector.insertSQL(insert) == true) {
            System.out.println("insert successfully");
            ResultSet resultSet = mysqlConnector.selectSQL(s);
            mysqlConnector.layoutStyle2(resultSet);
        }
        if (mysqlConnector.updateSQL(update) == true) {
            System.out.println("update successfully");
            ResultSet resultSet = mysqlConnector.selectSQL(s);
            mysqlConnector.layoutStyle2(resultSet);
        }
        if (mysqlConnector.insertSQL(delete) == true) {
            System.out.println("delete successfully");
            ResultSet resultSet = mysqlConnector.selectSQL(s);
            mysqlConnector.layoutStyle2(resultSet);
        }

        mysqlConnector.deconnSQL();
    }
}



