package org.mvnsearch.plantuml.github;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.mvnsearch.plantuml.gist.HttpClientUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Plantuml github servlet
 * <p/>
 * demo url: http://localhost:8080/github/linux-china/plantuml-gist/blob/master/src/main/uml/plantuml_gist.puml
 *
 * @author linux_china
 */
public class PlantumlGithubServlet extends HttpServlet {
    /**
     * image cache
     */
    private Cache imageCache = CacheManager.getInstance().getCache("plantUmlImages");
    private Cache uniqueImagesCache = CacheManager.getInstance().getCache("uniqueImages");
    private byte[] noPumlFound = null;
    private byte[] notDeveloper = null;
    private byte[] renderError = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            noPumlFound = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/no_puml_found.png"));
            renderError = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/render_error.png"));
            notDeveloper = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/gitlab_not_developer.png"));
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
        String filePath = requestURI.replace("/github/", "/");
        if (filePath.endsWith(".puml")) {
            Element element = imageCache.get(filePath);
            if (element != null && !element.isExpired()) {  //cache
                imageContent = (byte[]) element.getObjectValue();
            } else {
                try {
                    String source = getGithubFileContent(filePath);
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
        } catch (Exception ignore) {
           ignore.printStackTrace();
        }
        return null;
    }

    public byte[] renderPuml(String filePath, String source) throws Exception {
        String md5Key = DigestUtils.md5Hex(source);
        Element element = uniqueImagesCache.get(md5Key);
        if (element != null && !element.isExpired()) {
            Object content = element.getObjectValue();
            imageCache.put(new Element(filePath, content));
            return (byte[]) content;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        SourceStringReader reader = new SourceStringReader(source);
        String desc = reader.generateImage(bos);
        if (desc == null || !"(Error)".equals(desc)) {
            byte[] imageContent = bos.toByteArray();
            imageCache.put(new Element(filePath, imageContent));
            uniqueImagesCache.put(new Element(md5Key, imageContent));
            return imageContent;
        }
        return null;
    }

}
