package org.mvnsearch.plantuml.github;

import net.sf.ehcache.Element;
import org.mvnsearch.plantuml.PlantUmlBaseServlet;
import org.mvnsearch.plantuml.gist.HttpClientUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Plantuml github servlet
 * <p/>
 * demo url: http://localhost:8080/github/linux-china/plantuml-gist/blob/master/src/main/uml/plantuml_gist.puml
 *
 * @author linux_china
 */
public class PlantumlGithubServlet extends PlantUmlBaseServlet {

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(imageContentType);
        ServletOutputStream output = response.getOutputStream();
        byte[] imageContent = renderError;
        String requestURI = request.getRequestURI();
        //remove mapping
        String filePath = requestURI.substring(requestURI.indexOf("/", 1));
        if (filePath.endsWith(".puml")) {
            Element element = imageCache.get(filePath);
            if (element != null && !element.isExpired()) {  //cache
                imageContent = (byte[]) element.getObjectValue();
            } else {
                try {
                    String source = getGithubFileContent(filePath);
                    imageContent = renderSource(filePath,source,response);
                } catch (Exception ignore) {
                }
            }
        } else {
            imageContent = noPumlFound;
            response.setContentType("image/png");
        }
        output.write(imageContent);
        output.flush();
    }

    /**
     * get github file content
     *
     * @param filePath file path
     * @return puml file content
     */
    public String getGithubFileContent(String filePath) throws Exception {
        if (filePath.contains("/blob/")) {
            filePath = filePath.replace("/blob/", "/");
        }
        //https://raw.githubusercontent.com/linux-china/plantuml-gist/master/deploy.sh
        String httpUrl = "https://raw.githubusercontent.com" + filePath;
        try {
            return HttpClientUtils.getResponseBody(httpUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
