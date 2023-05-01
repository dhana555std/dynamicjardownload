package org.dhana.dbs;

import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.joinWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class JDBCDynamicJarDownloadAndConnectToDB {
    private static final String repositoryUrl = "https://repo1.maven.org/maven2";


    public static Connection init(JDBCDetails jdbcDetails, String version) {
        Connection conn = null;
        try {
            String dbType = jdbcDetails.name().toLowerCase();
            String groupId = jdbcDetails.getGroupId();
            String artifactId = jdbcDetails.getArtifactId();
            String className = jdbcDetails.getClassName();

            String dbVersion = version.equals("*") ? getVersionDetails(groupId, artifactId) : version;
            downloadJar(groupId, artifactId, dbVersion);
            conn = connectToDB(dbType, className, join(artifactId, "-", dbVersion, ".jar"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    private static String getVersionDetails(String groupId, String artifactId)
            throws IOException, XmlPullParserException {
        String mavenUrl = joinWith("/", repositoryUrl, groupId.replace(".", "/"), artifactId,
                                   "maven-metadata.xml");
        URL metadataUrl = new URL(mavenUrl);

        MetadataXpp3Reader reader = new MetadataXpp3Reader();
        Metadata metadata = reader.read(metadataUrl.openStream());
        String latestVersion = metadata.getVersioning().getLatest();

        List<String> versions = metadata.getVersioning().getVersions().stream().sorted(Comparator.reverseOrder())
                                        .collect(Collectors.toList());

        System.out.println("The following are the versions of " + groupId + " " + artifactId + " downloaded from " +
                           repositoryUrl + ":-");
        versions.forEach(System.out::println);

        System.out.println("The latest version of the dependency is " + latestVersion);
        return latestVersion;
    }

    private static void downloadJar(String groupId, String artifactId, String version)
            throws IOException {
        String jarUrl = joinWith("/", repositoryUrl, groupId.replace(".", "/"), artifactId, version,
                                 join(artifactId, "-", version, ".jar"));
        System.out.println("The download url is " + jarUrl);

        // Create the directory in the user's home directory where we want to download the JAR file
        File directory = new File(System.getProperty("user.home"), ".boomis");

        // Download the JAR file and save it to the subdirectory in the user's home directory
        URL url = new URL(jarUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        InputStream in = connection.getInputStream();
        FileOutputStream out = new FileOutputStream(new File(directory, artifactId + "-" + version + ".jar"));
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }

        out.close();
        in.close();

        System.out.println("JAR file downloaded to " + directory.getAbsolutePath());
    }

    public static Connection connectToDB(String dbType, String className, String dbJarName) {
        Connection conn = null;
        try {
            String sqlJar = joinWith(File.separator, System.getProperty("user.home"), ".boomis",
                                     dbJarName);
            File dbJar = new File(sqlJar);
            URLClassLoader classLoader = new URLClassLoader(new URL[]{dbJar.toURI().toURL()},
                                                            ClassLoader.getSystemClassLoader());

            Driver driver = (Driver) classLoader.loadClass(className).getDeclaredConstructor()
                                                .newInstance();

            Properties dbProperties = getDBProperties(dbType);
            String url = (String) dbProperties.get("url");
            dbProperties.remove("url");

            conn = driver.connect(url, dbProperties);
            System.out.println("Connected to " + dbType + " database.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    private static Properties getDBProperties(String dbType) throws IOException {
        Properties props = new Properties();
        String dbProperties = join(dbType, ".properties");
        InputStream is = JDBCDynamicJarDownloadAndConnectToDB.class
                .getResourceAsStream("/" + dbProperties);
        props.load(is);
        return props;
    }
}
