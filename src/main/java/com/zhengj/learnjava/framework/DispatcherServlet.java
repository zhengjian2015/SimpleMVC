package com.zhengj.learnjava.framework;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建一个接收所有请求的Servlet，通常我们把它命名为DispatcherServlet，
 * 它总是映射到/，然后，根据不同的Controller的方法定义的@Get或@Post的Path决定调用哪个方法，
 * 最后，获得方法返回的ModelAndView后，渲染模板，写入HttpServletResponse，即完成了整个MVC的处理
 */
@WebServlet(urlPatterns = "/")
public class DispatcherServlet extends HttpServlet {
    //需要存储请求路径到某个具体方法的映射
    private Map<String,GetDispather> getMappings = new HashMap<>();
    private Map<String,PostDispather> postMappings = new HashMap<>();
    private ViewEngine viewEngine;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        String path = req.getRequestURI().substring(req.getContextPath().length());
        //根据路径查找GetDispathcer
        GetDispather dispather = this.getMappings.get(path);
        if(dispather == null) {
            resp.sendError(404);
            return;
        }
        // 调用Controller方法获得返回值:
        ModelAndView mv = null;
        try {
            mv = dispather.invoke(req,resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if(mv == null) {
            return;
        }
        // 允许返回`redirect:`开头的view表示重定向:
        if (mv.view.startsWith("redirect:")) {
            resp.sendRedirect(mv.view.substring(9));
            return;
        }
        // 将模板引擎渲染的内容写入响应:
        PrintWriter pw = resp.getWriter();
        this.viewEngine.render(mv, pw);
        pw.flush();
    }
}
