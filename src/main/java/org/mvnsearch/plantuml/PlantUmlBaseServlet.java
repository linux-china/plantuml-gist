package org.mvnsearch.plantuml;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * plantuml base servlet
 *
 * @author linux_china
 */
public class PlantUmlBaseServlet extends HttpServlet {
    protected Cache imageCache = CacheManager.getInstance().getCache("plantUmlImages");
    protected Cache uniqueImagesCache = CacheManager.getInstance().getCache("uniqueImages");
    protected Cache fileCache = CacheManager.getInstance().getCache("plantUmlFiles");
    protected byte[] noPumlFound = null;
    protected byte[] renderError = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            noPumlFound = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/no_puml_found.png"));
            renderError = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/render_error.png"));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected byte[] renderPuml(String filePath, String source) throws Exception {
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

    protected byte[] renderPuml(String source) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        SourceStringReader reader = new SourceStringReader(source);
        String desc = reader.generateImage(bos);
        if (desc == null || !"(Error)".equals(desc)) {
            return bos.toByteArray();
        } else {
            return null;
        }
    }
}
