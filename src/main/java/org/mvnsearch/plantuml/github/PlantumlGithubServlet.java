package org.mvnsearch.plantuml.github;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import net.sf.ehcache.Element;
import org.mvnsearch.plantuml.PlantUmlBaseServlet;
import org.mvnsearch.plantuml.gist.HttpClientUtils;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

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
        String requestURI = request.getRequestURI();
        if (requestURI.endsWith(".mmd")) {
            doGetMmd(request, response);
            return;
        }
        response.setContentType(imageContentType);
        ServletOutputStream output = response.getOutputStream();
        byte[] imageContent = renderError;
        //remove mapping
        String filePath = requestURI.substring(requestURI.indexOf("/", 1));
        if (filePath.endsWith(".puml")) {
            Element element = imageCache.get(filePath);
            if (element != null && !element.isExpired()) {  //cache
                imageContent = (byte[]) element.getObjectValue();
            } else {
                try {
                    String source = getGithubFileContent(filePath);
                    imageContent = renderSource(filePath, source, response);
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

    protected void doGetMmd(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(imageContentType);
        ServletOutputStream output = response.getOutputStream();
        byte[] imageContent = renderError;
        String requestURI = request.getRequestURI();
        //remove mapping
        String filePath = requestURI.substring(requestURI.indexOf("/", 1));
        if (filePath.endsWith(".mmd")) {
            Element element = imageCache.get(filePath);
            if (element != null && !element.isExpired()) {  //cache
                imageContent = (byte[]) element.getObjectValue();
            } else {
                try {
                    String source = getGithubFileContent(filePath);
                    MindMap model = new MindMap(null, new StringReader(source));
                    MindMapPanelConfig cfg = new MindMapPanelConfig();
                    cfg.setShowGrid(false);
                    cfg.setDrawBackground(false);
                    cfg.setConnectorColor(Color.BLUE);
                    BufferedImage bufferedImage = MindMapPanel.renderMindMapAsImage(model, cfg, true, RenderQuality.QUALITY);
                    ByteArrayOutputStream buff = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "png", buff);
                    imageContent = buff.toByteArray();
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

}
