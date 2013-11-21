package org.mvnsearch.plantuml.gist;

import com.google.gson.Gson;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.io.IOUtils;

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
public class PlantumlGistServlet extends HttpServlet {
    /**
     * image cache
     */
    private Cache imageCache = CacheManager.getInstance().getCache("plantUmlImages");
    private byte[] gistNotFound = null;
    private byte[] noPumlFound = null;
    private byte[] renderError = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            gistNotFound = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/gist_not_found.png"));
            noPumlFound = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/no_puml_found.png"));
            renderError = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/render_error.png"));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("image/png");
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
                } else if (source.equalsIgnoreCase("no puml found")) {  // no puml file found
                    imageContent = noPumlFound;
                } else {  //render puml content
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    SourceStringReader reader = new SourceStringReader(source);
                    String desc = reader.generateImage(bos);
                    if (desc == null || !"(Error)".equals(desc)) {
                        imageContent = bos.toByteArray();
                        element = new Element(gistId, imageContent);
                        imageCache.put(element);
                    }
                }
            } catch (Exception ignore) {

            }
        }
        output.write(imageContent);
        output.flush();
    }

    @SuppressWarnings("unchecked")
    public String getGistContent(String id) throws IOException {
        Gson gson = new Gson();
        String httpUrl = "https://api.github.com/gists/" + id;
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
                return (String) file.get("content");
            }
        }
        return "no puml found";
    }

}
