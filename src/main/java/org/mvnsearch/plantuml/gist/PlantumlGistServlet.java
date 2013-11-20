package org.mvnsearch.plantuml.gist;

import com.google.gson.Gson;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sourceforge.plantuml.SourceStringReader;

import javax.servlet.ServletException;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String gistId = requestURI.replace("/gist/", "");
        if (gistId.contains("?")) {
            gistId = gistId.substring(0, gistId.indexOf("?"));
        }
        if (gistId.contains("/")) {
            gistId = gistId.substring(0, gistId.indexOf("/"));
        }
        Element element = imageCache.get(gistId);
        if (element != null && !element.isExpired()) {
            response.setContentType("image/png");
            response.getOutputStream().write((byte[]) element.getObjectValue());
            return;
        }
        try {
            String source = getGistContent(gistId);
            if (source == null) {
                response.sendRedirect("/img/gist_not_found.png");
                return;
            }
            if (source.equals("no puml found")) {
                response.sendRedirect("/img/no_puml_found.png");
                return;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            SourceStringReader reader = new SourceStringReader(source);
            String desc = reader.generateImage(bos);
            if ("(Error)".equals(desc)) {
                response.sendRedirect("render_error.png");
            } else {
                response.setContentType("image/png");
                byte[] content = bos.toByteArray();
                element = new Element(gistId, content);
                imageCache.put(element);
                response.getOutputStream().write(content);
            }
        } catch (Exception e) {
            response.sendRedirect("render_error.png");
        }
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
