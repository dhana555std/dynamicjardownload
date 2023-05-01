package org.dhana.dbs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBInvocation {

    public static void main(String[] args) {
        invokeDBCall(JDBCDetails.POSTGRES, "select 71 as result", "*");
        invokeDBCall(JDBCDetails.MYSQL, "select 555 as result from dual", "*");
        invokeDBCall(JDBCDetails.ORACLE, "select 1 as result from dual", "*");
        invokeDBCall(JDBCDetails.MSSQL, "select 1 as result", "*");
    }

    private static void invokeDBCall(JDBCDetails dbDetails, String sqlStatement, String version) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCDynamicJarDownloadAndConnectToDB.init(dbDetails, version);
            pstmt = conn.prepareStatement(sqlStatement);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("output " + rs.getInt("result"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
