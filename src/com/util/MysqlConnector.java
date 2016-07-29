package com.util;

import java.sql.*;
import java.util.List;

/**
 * Created by Administrator on 2016/7/28.
 */
public class MysqlConnector {
    private Connection conn = null;
    PreparedStatement statement = null;
    PreparedStatement pst = null;
    PreparedStatement ps = null;
    String db_host = "60.206.107.184";
    String db_user = "root";
    String db_pwd = "root123";

    // connect to MySQL
    public void connSQL() {
        String url = "jdbc:mysql://"+ db_host + ":3306/weather?autoReconnect=true&useUnicode=TRUE&characterEncoding=UTF8";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, db_user, db_pwd);
        }
        //捕获加载驱动程序异常
        catch (ClassNotFoundException cnfex) {
            System.err.println("装载 JDBC/ODBC 驱动程序失败。");
            cnfex.printStackTrace();
        }
        //捕获连接数据库异常
        catch (SQLException sqlex) {
            System.err.println("无法连接数据库");
            sqlex.printStackTrace();
        }
    }

    // disconnect to MySQL
    public void disconnSQL() {
        try {
            if (conn != null)
                conn.close();
        } catch (Exception e) {
            System.out.println("关闭数据库问题 ：");
            e.printStackTrace();
        }
    }

    public ResultSet query(String sql){
        try {
            //forward only read only也是mysql 驱动的默认值，所以不指定也是可以的 比如： PreparedStatement ps = connection.prepareStatement("select .. from ..");
            ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE); //也可以修改jdbc url通过defaultFetchSize参数来设置，这样默认所以的返回结果都是通过流方式读取.
            ResultSet rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void close_query(){
        try {
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // execute selection language
    public ResultSet selectSQL(String sql) {
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
    public boolean insertSQL(String sql) {
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

    public void ready_insert(){
        // 设置事务为非自动提交
        try {
            conn.setAutoCommit(false);
            // Statement st = conn.createStatement();
            // 比起st，pst会更好些
            pst = conn.prepareStatement("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean insert_SQLS(String prefix,List<String> values) {

        int size = 0;
//        String prefix = "INSERT INTO tb_big_data (count, create_time, random) VALUES ";
        try {
            // 保存sql后缀
            StringBuffer suffix = new StringBuffer();
            // 外层循环，总提交事务次数
            size = values.size();
            for (int j = 0; j <size; j++) {
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("insert size : " + size);
        return true;
    }
    public void close_insert(){
        try {
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //execute delete language
    public boolean deleteSQL(String sql) {
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
    public boolean updateSQL(String sql) {
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

    // show data of town_location
    public void showResultSet(ResultSet rs) {
        System.out.println("-----------------");
        System.out.println("执行结果如下所示:");
        System.out.println("-----------------");

        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                for(int i=1;i<columnCount+1;i++){
                    Object tmp = rs.getObject(i);
                    if(tmp!=null)System.out.print(tmp.toString() + " ");
                }
                System.out.println("");
            }
        } catch (Exception e) {
            System.out.println("显示出错。");
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();

        String s = "select * from town_location";
        String insert = "INSERT INTO motorway(name, road_id, latitude,longitude) VALUES ('S20-彭湖高速公路','0',116.68726746,29.9301079052)";
//        String update = "update ju_users set ju_userPWD =123 where ju_userName= 'mm'";
//        String delete = "delete from ju_users where ju_userName= 'mm'";

//       if (mysqlConnector.insertSQL(insert) == true) {
//            System.out.println("insert successfully");
            ResultSet resultSet = mysqlConnector.selectSQL("select * from motorway");
            mysqlConnector.showResultSet(resultSet);
//        }
//        if (mysqlConnector.updateSQL(update) == true) {
//            System.out.println("update successfully");
//            ResultSet resultSet = mysqlConnector.selectSQL(s);
//            mysqlConnector.layoutStyle2(resultSet);
//        }
//        if (mysqlConnector.insertSQL(delete) == true) {
//            System.out.println("delete successfully");
//            ResultSet resultSet = mysqlConnector.selectSQL(s);
//            mysqlConnector.layoutStyle2(resultSet);
//        }
//        ResultSet resultSet = mysqlConnector.selectSQL(s);
//        mysqlConnector.showResultSet(resultSet);
        mysqlConnector.disconnSQL();
    }
}



