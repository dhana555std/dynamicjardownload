package org.dhana.dbs;

public enum JDBCDetails {
    MYSQL("com.mysql", "mysql-connector-j", "com.mysql.cj.jdbc.Driver"),
//    MYSQL("mysql", "mysql-connector-java", "com.mysql.jdbc.Driver"),
    POSTGRES("org.postgresql", "postgresql", "org.postgresql.Driver"),
    ORACLE("com.oracle.database.jdbc", "ojdbc10", "oracle.jdbc.driver.OracleDriver"),
    MSSQL("com.microsoft.sqlserver", "mssql-jdbc", "com.microsoft.sqlserver.jdbc.SQLServerDriver");


    private String groupId;
    private String artifactId;
    private String className;

    JDBCDetails(String groupId, String artifactId, String className) {
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.className = className;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getClassName() {
        return className;
    }

    //


}
