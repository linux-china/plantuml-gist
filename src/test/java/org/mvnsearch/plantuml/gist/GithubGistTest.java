package org.mvnsearch.plantuml.gist;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.Map;

/**
 * github gist test
 *
 * @author linux_china
 */
public class GithubGistTest {

    @Test
    public void testGetGist() throws Exception {
        Gson gson = new Gson();
        String httpUrl = "https://api.github.com/gists/7563171";
        String responseBody = HttpClientUtils.getResponseBody(httpUrl);
        Map json = gson.fromJson(responseBody, Map.class);
        Map<String, Map<String, Object>> files = (Map<String, Map<String, Object>>) json.get("files");
        for (Map.Entry<String, Map<String, Object>> entry : files.entrySet()) {
            String fileName = entry.getKey();
            if (fileName.endsWith(".puml")) {
                Map<String, Object> file = entry.getValue();
                System.out.println(file.get("content"));
            }
        }
    }

}
