package org.mvnsearch.plantuml.gravizo;

import org.apache.commons.lang.StringUtils;
import org.mvnsearch.plantuml.PlantUmlBaseServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * gravizo servlet
 * <p/>
 * demo url: http://puml.alibaba.net/g
 *
 * @author linux_china
 */
public class GravizoServlet extends PlantUmlBaseServlet {

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(imageContentType);
        ServletOutputStream output = response.getOutputStream();
        String content = null;
        String query = request.getQueryString();
        if (StringUtils.isNotEmpty(query)) {
            content = URLDecoder.decode(query, "utf-8").trim();
        }
        byte[] imageContent = renderPuml(content);
        if (imageContent != null) {
            output.write(imageContent);
        } else {
            response.setContentType("image/png");
            output.write(renderError);
        }
        output.flush();
    }

}
