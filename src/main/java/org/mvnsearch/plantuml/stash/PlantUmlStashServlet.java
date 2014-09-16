package org.mvnsearch.plantuml.stash;

import com.google.gson.Gson;
import net.sf.ehcache.Element;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvnsearch.plantuml.PlantUmlBaseServlet;
import org.mvnsearch.plantuml.gist.HttpClientUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * stash plantuml servlet
 *
 * @author linux_china
 */
public class PlantUmlStashServlet extends PlantUmlBaseServlet {
    private String username;
    private String password;
    private String baseUrl;
    private byte[] notDeveloper = null;


    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        username = config.getInitParameter("username");
        password = config.getInitParameter("password");
        baseUrl = config.getInitParameter("baseUrl");
        try {
            notDeveloper = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/gitlab_not_developer.png"));
        } catch (IOException ignore) {
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("image/png");
        ServletOutputStream output = response.getOutputStream();
        byte[] imageContent = renderError;
        String filePath = request.getRequestURI();
        if (filePath.endsWith(".puml")) {
            Element element = imageCache.get(filePath);
            if (element != null && !element.isExpired()) {  //cache
                imageContent = (byte[]) element.getObjectValue();
            } else {
                try {
                    String source = getStashFileContent(filePath);
                    if (source == null) {  // puml file not found
                        imageContent = noPumlFound;
                    } else {  //render puml content
                        imageContent = renderPuml(filePath, source);
                        if (imageContent == null) {
                            imageContent = noPumlFound;
                        }
                    }
                } catch (FileNotFoundException e) {
                    imageContent = notDeveloper;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            imageContent = noPumlFound;
        }
        output.write(imageContent);
        output.flush();
    }


    /**
     * get stash file content
     *
     * @param filePath file path
     * @return puml file content
     */
    @SuppressWarnings("unchecked")
    public String getStashFileContent(String filePath) throws Exception {
        String url = baseUrl + "/rest/api/1.0/" + filePath;
        String body = HttpClientUtils.getResponseBody(username, password, url);
        Gson gson = new Gson();
        Map json = gson.fromJson(body, Map.class);
        List<Map<String, String>> lines = (List<Map<String, String>>) json.get("lines");
        List<String> rawLines = new ArrayList<String>();
        for (Map<String, String> line : lines) {
            rawLines.add(line.get("text"));
        }
        return StringUtils.join(rawLines, "\r\n");
    }
}
