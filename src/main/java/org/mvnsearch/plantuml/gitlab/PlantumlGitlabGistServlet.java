package org.mvnsearch.plantuml.gitlab;

import net.sf.ehcache.Element;
import org.apache.commons.io.IOUtils;
import org.mvnsearch.plantuml.PlantUmlBaseServlet;
import org.mvnsearch.plantuml.gist.HttpClientUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Plantuml gitlab gist servlet
 * <p/>
 * demo url: http://localhost:8080/gitlabgist/488
 *
 * @author linux_china
 */
public class PlantumlGitlabGistServlet extends PlantUmlBaseServlet {
    /**
     * image cache
     */
    private byte[] gistNotFound = null;
    private String gitlabUrl;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            gistNotFound = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/gist_not_found.png"));
            this.gitlabUrl = config.getInitParameter("gitlabUrl");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(imageContentType);
        ServletOutputStream output = response.getOutputStream();
        byte[] imageContent = renderError;
        String requestURI = request.getRequestURI();
        String gistId = requestURI.replace("/snippet/", "").replace("/alipaysnippet/", "");
        if (gistId.contains("?")) {
            gistId = gistId.substring(0, gistId.indexOf("?"));
        }
        if (gistId.contains("/")) {
            gistId = gistId.substring(0, gistId.indexOf("/"));
        }
        Element element = imageCache.get(gistId);
        if (element != null && !element.isExpired()) {  //cache
            imageContent = (byte[]) element.getObjectValue();
        } else {
            try {
                String source = getGistContent(gistId);
                imageContent = renderSource(gistId,source,response);
            } catch (Exception ignore) {

            }
        }
        if (imageContent == null) {
            imageContent = gistNotFound;
            response.setContentType("image/png");
        }
        output.write(imageContent);
        output.flush();
    }

    @SuppressWarnings("unchecked")
    public String getGistContent(String id) throws IOException {
        String httpUrl = gitlabUrl + "/snippets/" + id + "/raw";
        try {
            String content = HttpClientUtils.getResponseBody(httpUrl);
            fileCache.put(new Element(id, content));
            return content;
        } catch (Exception e) {
            Element element = fileCache.get(id);
            if (element != null && !element.isExpired()) {
                return (String) element.getObjectValue();
            }
        }
        return "no puml found";
    }

}
