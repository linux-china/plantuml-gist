package org.mvnsearch.plantuml.gitlab;

import java.util.*;

/**
 * gitlab info
 *
 * @author linux_china
 */
public class GitlabInfo {
    private String username;
    private Map<String, GitlabProjectInfo> projects = new HashMap<String, GitlabProjectInfo>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, GitlabProjectInfo> getProjects() {
        return projects;
    }

    public void setProjects(Map<String, GitlabProjectInfo> projects) {
        this.projects = projects;
    }

    public void addProject(GitlabProjectInfo project) {
        projects.put(project.getPath(), project);
    }

    public GitlabProjectInfo findProject(String path) {
        for (GitlabProjectInfo projectInfo : projects.values()) {
            if (projectInfo.getPath().equalsIgnoreCase(path.substring(1))) {
                return projectInfo;
            }
        }
        return null;
    }
}
