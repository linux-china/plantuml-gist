package org.mvnsearch.plantuml.gist;

import com.google.gson.Gson;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.mvnsearch.plantuml.PlantUmlBaseServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Plantuml gist servlet
 *
 * @author linux_china
 */
public class PlantumlGistServlet extends PlantUmlBaseServlet {
    /**
     * image cache
     */
    private byte[] gistNotFound = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            gistNotFound = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/gist_not_found.png"));
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
        String gistId = requestURI.replace("/gist/", "");
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
                if (source == null) {  // gist not found
                    imageContent = gistNotFound;
                    response.setContentType("image/png");
                } else if (source.equalsIgnoreCase("no puml found")) {  // no puml file found
                    imageContent = noPumlFound;
                    response.setContentType("image/png");
                } else {  //render puml content
                    imageContent = renderPuml(gistId, source);
                }
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
        Gson gson = new Gson();
        String httpUrl = "https://api.github.com/gists/" + id;
        try {
            String responseBody = HttpClientUtils.getResponseBody(httpUrl);
            Map json = gson.fromJson(responseBody, Map.class);
            if (json.containsKey("message") && "Not Found".equalsIgnoreCase((String) json.get("message"))) {
                return null;
            }
            Map<String, Map<String, Object>> files = (Map<String, Map<String, Object>>) json.get("files");
            for (Map.Entry<String, Map<String, Object>> entry : files.entrySet()) {
                String fileName = entry.getKey();
                if (fileName.endsWith(".puml")) {
                    Map<String, Object> file = entry.getValue();
                    Object content = file.get("content");
                    fileCache.put(new Element(id, (String) content));
                    return (String) content;
                }
            }
        } catch (Exception e) {
            Element element = fileCache.get(id);
            if (element != null && !element.isExpired()) {
                return (String) element.getObjectValue();
            }
        }
        return "no puml found";
    }

}
