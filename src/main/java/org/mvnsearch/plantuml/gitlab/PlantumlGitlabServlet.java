package org.mvnsearch.plantuml.gitlab;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import net.sf.ehcache.Element;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.http.GitlabHTTPRequestor;
import org.gitlab.api.models.GitlabProject;
import org.mvnsearch.plantuml.PlantUmlBaseServlet;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Plantuml gitlab servlet
 * <p/>
 * demo url: http://localhost:8080/gitlab/uic/uic-structure/blob/master/uml/uic-deployment.puml
 *
 * @author linux_china
 */
public class PlantumlGitlabServlet extends PlantUmlBaseServlet {
    protected byte[] notDeveloper = null;
    protected GitlabAPI gitlabAPI;
    protected GitlabInfo gitlabInfo = new GitlabInfo();

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            gitlabAPI = GitlabAPI.connect(config.getInitParameter("gitlabUrl"), config.getInitParameter("userToken"));
            initGitlabInfo();
            notDeveloper = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/gitlab_not_developer.png"));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void initGitlabInfo() throws Exception {
        gitlabInfo.setUsername(gitlabAPI.getCurrentSession().getUsername());
        for (GitlabProject gitlabProject : gitlabAPI.getProjects()) {
            gitlabInfo.addProject(new GitlabProjectInfo(gitlabProject.getId(), gitlabProject.getName(), gitlabProject.getPathWithNamespace()));
        }
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
        if (filePath.endsWith(".puml") || filePath.endsWith(".uml")) {
            Element element = imageCache.get(filePath);
            if (element != null && !element.isExpired()) {  //cache
                imageContent = (byte[]) element.getObjectValue();
            } else {
                try {
                    String source = getGitlabFileContent(filePath);
                    imageContent = renderSource(filePath, source, response);
                } catch (FileNotFoundException e) {
                    imageContent = notDeveloper;
                    response.setContentType("image/png");
                } catch (Exception e) {
                    e.printStackTrace();
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
     * get gitlab file content
     *
     * @param filePath file path
     * @return puml file content
     */
    public String getGitlabFileContent(String filePath) throws Exception {
        if (filePath.contains("/raw/")) {
            filePath = filePath.replace("/raw/", "/blob/");
        }
        String projectPath = filePath.substring(0, filePath.indexOf("/blob/"));
        try {
            GitlabProjectInfo project = gitlabInfo.findProject(projectPath);
            //if project not found, try to refresh gitlab info
            if (project == null) {
                initGitlabInfo();
                project = gitlabInfo.findProject(projectPath);
            }
            String path = filePath.substring(filePath.indexOf("/blob/") + 6);
            String branch = path.substring(0, path.indexOf("/"));
            path = path.substring(path.indexOf("/") + 1);
            GitlabHTTPRequestor retrieve = gitlabAPI.retrieve();
            Map<String, Object> content = retrieve.to("/projects/" + project.getId() + "/repository/files?file_path=" + path + "&ref=" + branch, HashMap.class);
            if (content.containsKey("content")) {
                byte[] contents = Base64.decodeBase64((String) content.get("content"));
                String source = new String(contents);
                fileCache.put(new Element(fileCache, source));
                return source;
            }
        } catch (Exception e) {
            Element element = fileCache.get(filePath);
            if (element != null && !element.isExpired()) {
                return (String) element.getObjectValue();
            }
        }
        return null;
    }


    protected void doGetMmd(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("image/png");
        ServletOutputStream output = response.getOutputStream();
        String requestURI = request.getRequestURI();
        byte[] imageContent = notDeveloper;
        //remove mapping
        String filePath = requestURI.substring(requestURI.indexOf("/", 1));
        if (filePath.endsWith(".mmd")) {
            Element element = imageCache.get(filePath);
            if (element != null && !element.isExpired()) {  //cache
                imageContent = (byte[]) element.getObjectValue();
            } else {
                try {
                    String source = getGitlabFileContent(filePath);
                    MindMap model = new MindMap(null, new StringReader(source));
                    MindMapPanelConfig cfg = new MindMapPanelConfig();
                    cfg.setShowGrid(false);
                    cfg.setDrawBackground(false);
                    cfg.setConnectorColor(Color.BLUE);
                    BufferedImage bufferedImage = MindMapPanel.renderMindMapAsImage(model, cfg, true, RenderQuality.QUALITY);
                    ByteArrayOutputStream buff = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "png", buff);
                    imageContent = buff.toByteArray();
                } catch (Exception e) {
                    imageContent = renderError;
                }
            }
        }
        output.write(imageContent);
        output.flush();
    }

}
