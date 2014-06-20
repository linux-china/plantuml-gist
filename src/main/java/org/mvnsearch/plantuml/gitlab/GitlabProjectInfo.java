package org.mvnsearch.plantuml.gitlab;

/**
 * gitlab project
 *
 * @author linux_china
 */
public class GitlabProjectInfo {
    /**
     * id
     */
    private Integer id;
    /**
     * name
     */
    private String name;
    /**
     * path without "/" prefix
     */
    private String path;

    public GitlabProjectInfo() {

    }

    public GitlabProjectInfo(Integer id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
