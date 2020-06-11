/*******************************************************************************
 * Copyright (c) 2020-06-11 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/

import com.alibaba.fastjson.JSON;
import com.iff.docker.modules.app.vo.dashboard.DockerImagesRepositoryVO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * NexusTest
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-11
 */
public class NexusTest {
    public static void main(String[] args) {
        String url = "http://47.107.102.110:8081/service/rest/v1/search/assets?repository=test";
        String content = get(url);
        DockerImagesRepositoryVO vo = JSON.parseObject(content, DockerImagesRepositoryVO.class);
        for (DockerImagesRepositoryVO.Image image : vo.getItems()) {
            String name = StringUtils.replace(image.getPath(), "v2/", "172.18.222.82:8082/");
            name = StringUtils.replace(name, "/manifests/", ":");
            System.out.println("docker pull " + name);
        }
    }

    public static String get(String requestUrl) {
        BufferedReader reader = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(requestUrl);
            {
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:44.0) Gecko/20100101 Firefox/44.0");
                connection.setRequestProperty("charset", "UTF-8");
                connection.setRequestProperty("accept", "*/*");
                connection.setConnectTimeout(30 * 1000);
                connection.setReadTimeout(60 * 1000);
                connection.connect();
            }
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder(2048);
            for (String line; (line = reader.readLine()) != null; ) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(reader);
            try {
                connection.disconnect();
            } catch (Exception e) {
            }
        }
        return null;
    }
}
