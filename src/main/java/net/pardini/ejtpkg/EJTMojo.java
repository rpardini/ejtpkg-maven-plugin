package net.pardini.ejtpkg;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.sonatype.inject.Nullable;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * The Mojo.
 *
 * @goal ejt
 * @requiresProject
 */
public class EJTMojo extends AbstractMojo {
// ------------------------------ FIELDS ------------------------------

    /**
     * Directory the project.
     *
     * @parameter default-value="${project.build.directory}"
     * @required
     */
    protected File projectBuildDir;

    private final Log log = this.getLog();


    /**
     * @component
     * @required
     * @readonly
     */
    private MavenSettingsBuilder mavenSettingsBuilder;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Mojo ---------------------

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            log.info(String.format("Build dir is %s", projectBuildDir));

            Map<String, byte[]> finalFiles = new LinkedHashMap<String, byte[]>();
            final List<String> neededFilesList = getNeededFilesList();

            finalFiles.putAll(filterEntriesByList(readEntriesFromArchive(downloadJREFromOracle("http://download.oracle.com/otn-pub/java/jdk/7u11-b21/jre-7u11-windows-x64.tar.gz"), "jre/"), neededFilesList));
            finalFiles.putAll(filterEntriesByList(readEntriesFromArchive(downloadTomcatFromApache("http://apache.mirror.pop-sc.rnp.br/apache/tomcat/tomcat-7/v7.0.35/bin/apache-tomcat-7.0.35-windows-x64.zip"), "tomcat/"), neededFilesList));

            addResourceFileDirectly(finalFiles, "conf/catalina.policy");
            addResourceFileDirectly(finalFiles, "conf/catalina.properties");
            addResourceFileDirectly(finalFiles, "conf/context.xml");
            addResourceFileDirectly(finalFiles, "conf/logging.properties");
            addResourceFileDirectly(finalFiles, "conf/server.xml");
            addResourceFileDirectly(finalFiles, "conf/web.xml");

            addFilteredCombinedWindowsScriptFile(finalFiles, "installAsService.bat");
            addFilteredCombinedWindowsScriptFile(finalFiles, "removeService.bat");
            addFilteredCombinedWindowsScriptFile(finalFiles, "run.bat");

            log.info(finalFiles.keySet().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addFilteredCombinedWindowsScriptFile(final Map<String, byte[]> finalFiles, final String windowsScriptFile) {
        finalFiles.put(windowsScriptFile, String.format("%s%s", getWindowsCommonFiltered(), getContentsOfResourceFileAsString(String.format("windows/scripts/%s", windowsScriptFile))).getBytes(Charset.defaultCharset()));
    }

    private String getWindowsCommonFiltered() {
        return getContentsOfResourceFileAsString("windows/scripts/common.bat"); // @TODO actually filter
    }

// -------------------------- OTHER METHODS --------------------------

    private void addResourceFileDirectly(final Map<String, byte[]> filteredFiles, final String resourceFile) {
        filteredFiles.put(resourceFile, getContentsOfResourceFileAsByteArray(resourceFile));
    }

    private byte[] getContentsOfResourceFileAsByteArray(String fileToRead) {
        try {
            return IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream(fileToRead));
        } catch (IOException e) {
            throw new RuntimeException(String.format("%s", e.getMessage()), e);
        }
    }

    private File downloadJREFromOracle(String downloadURL) {
        return getFileToTemp(downloadURL);
    }

    private File getFileToTemp(final String downloadURL) {
        URL url = null;
        try {
            url = new URL(downloadURL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("%s", e.getMessage()), e);
        }

        log.info("URL: " + url);

        File tempFile = new File(System.getProperty("java.io.tmpdir"), FilenameUtils.getName(url.getFile()));
        log.info("Temp file: " + tempFile.getAbsolutePath());

        if (tempFile.exists()) return tempFile;

        File downloadedFile = downloadFile(downloadURL, tempFile);
        return downloadedFile;
    }

    private File downloadFile(final String url, final File downloadToFile) {
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
            httpGet.setHeader(new BasicHeader("Cookie", "gpw_e24=http%3A%2F%2Fwww.oracle.com"));
            HttpResponse execute = client.execute(httpGet);
            log.info("Resp code: " + execute.getStatusLine().toString());
            log.info("Downloading " + execute.getFirstHeader("Content-Length").getValue() + " bytes.");

            HttpEntity entity = execute.getEntity();
            if (entity != null) {
                FileOutputStream fos = new FileOutputStream(downloadToFile);
                entity.writeTo(fos);
                fos.close();
            }

            return downloadToFile;
        } catch (Exception e) {
            log.error("IOException", e);
        }
        return null;
    }

    private File downloadTomcatFromApache(final String url) {
        return getFileToTemp(url);
    }

    private Map<String, byte[]> filterEntriesByList(final Map<String, byte[]> allDownloadedFiles, final List<String> list) {
        return Maps.filterKeys(allDownloadedFiles, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable final String entryName) {
                return list.contains(entryName);
            }
        });
    }

    private List<String> getNeededFilesList() throws IOException {
        String fileToRead = getContentsOfResourceFileAsString("windows/neededfiles.txt");
        return Lists.newArrayList(Splitter.on("\n").omitEmptyStrings().trimResults().split(fileToRead));
    }

    private String getContentsOfResourceFileAsString(String fileToRead) {
        try {
            return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(fileToRead), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(String.format("%s: %s", e.getMessage(), fileToRead), e);
        }
    }

    protected String getProxyFromMavenSettings() {
        try {
            Settings settings = mavenSettingsBuilder.buildSettings();
            ProxyInfo proxyInfo = null;
            if (settings != null && settings.getActiveProxy() != null) {
                Proxy settingsProxy = settings.getActiveProxy();
                return settingsProxy.getProtocol() + settingsProxy.getHost() + ":" + settingsProxy.getPort();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, byte[]> readEntriesFromArchive(final File inputFile, final String firstDir) throws Exception {
        HashMap<String, byte[]> stringHashMap = new HashMap<String, byte[]>();
        InputStream is;
        ArchiveInputStream archiveInputStream;
        try {
            is = new BufferedInputStream(new FileInputStream(inputFile));
            archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(is);
        } catch (Exception e) {
            is = new BufferedInputStream(new GZIPInputStream(new FileInputStream((inputFile))));
            archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(is);
        }

        ArchiveEntry entry = null;
        while ((entry = archiveInputStream.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                String fullName = entry.getName();
                String key = removeFirstPartOfPath(fullName);
                if (firstDir != null) key = firstDir + key;
                stringHashMap.put(key, IOUtils.toByteArray(archiveInputStream));
            }
        }
        archiveInputStream.close();
        return stringHashMap;
    }

    private String removeFirstPartOfPath(final String fullName) {
        return fullName.substring(fullName.indexOf("/") + 1);
    }
}
