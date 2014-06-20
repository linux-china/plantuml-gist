package org.mvnsearch.plantuml.gitlab;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.http.GitlabHTTPRequestor;
import org.gitlab.api.models.GitlabProject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Plantuml gitlab servlet
 * <p/>
 * demo url: http://localhost:8080/gitlab/uic/uic-structure/blob/master/uml/uic-deployment.puml
 *
 * @author linux_china
 */
public class PlantumlGitlabServlet extends HttpServlet {
    /**
     * image cache
     */
    private Cache imageCache = CacheManager.getInstance().getCache("plantUmlImages");
    private byte[] noPumlFound = null;
    private byte[] notDeveloper = null;
    private byte[] renderError = null;
    private GitlabAPI gitlabAPI;
    private GitlabInfo gitlabInfo = new GitlabInfo();

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            gitlabAPI = GitlabAPI.connect(config.getInitParameter("gitlabUrl"), config.getInitParameter("userToken"));
            initGitlabInfo();
            noPumlFound = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/no_puml_found.png"));
            renderError = IOUtils.toByteArray(this.getClass().getResourceAsStream("/img/render_error.png"));
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
        response.setContentType("image/png");
        ServletOutputStream output = response.getOutputStream();
        byte[] imageContent = renderError;
        String requestURI = request.getRequestURI();
        String filePath = requestURI.replace("/gitlab/", "/");
        Element element = imageCache.get(filePath);
        if (filePath.endsWith(".puml")) {
            if (element != null && !element.isExpired()) {  //cache
                imageContent = (byte[]) element.getObjectValue();
            } else {
                try {
                    String source = getGitlabFileContent(filePath);
                    if (source == null) {  // puml file not found
                        imageContent = noPumlFound;
                    } else {  //render puml content
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        SourceStringReader reader = new SourceStringReader(source);
                        String desc = reader.generateImage(bos);
                        if (desc == null || !"(Error)".equals(desc)) {
                            imageContent = bos.toByteArray();
                            element = new Element(filePath, imageContent);
                            imageCache.put(element);
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
     * get gitlab file content
     *
     * @param filePath file path
     * @return puml file content
     */
    public String getGitlabFileContent(String filePath) throws Exception {
        String projectPath = filePath.substring(0, filePath.indexOf("/blob/"));
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
            return new String(contents);
        }
        return null;
    }

}
