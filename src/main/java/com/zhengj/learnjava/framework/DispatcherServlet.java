package com.zhengj.learnjava.framework;

import com.zhengj.learnjava.controller.IndexController;
import com.zhengj.learnjava.controller.UserController;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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


    // TODO: 可指定package并自动扫描:
    private List<Class<?>> controllers = Arrays.asList(IndexController.class, UserController.class);


    private static final Set<Class<?>> supportedGetParameterTypes =
            new HashSet<Class<?>>(){{
                add(int.class);
                add(long.class);
                add(boolean.class);
                add(String.class);
                add(HttpServletRequest.class);
                add(HttpServletResponse.class);
                add(HttpSession.class);
            }};

    /**
     * 当Servlet容器创建当前Servlet实例后，会自动调用init(ServletConfig)方法
     */
    @Override
    public void init() throws ServletException {
        System.out.println("init 开始 "+ getClass().getSimpleName() + "...");
        // 依次处理每个Controller:
        for (Class<?> controllerClass : controllers) {
            try {
                Object controllerInstance = controllerClass.getConstructor().newInstance();
                //处理每个Method
                for(Method method : controllerClass.getMethods()) {
                    if(method.getAnnotation(GetMapping.class) != null) {
                        // 处理@Get:
                        if (method.getReturnType() != ModelAndView.class && method.getReturnType() != void.class) {
                            throw new UnsupportedOperationException(
                                    "Unsupported return type: " + method.getReturnType() + " for method: " + method);
                        }
                        for(Class<?> parameterClass : method.getParameterTypes()) {
                            if (!supportedGetParameterTypes.contains(parameterClass)) {
                                throw new UnsupportedOperationException(
                                        "Unsupported parameter type: " + parameterClass + " for method: " + method);
                            }
                        }
                        String[] parameterNames = Arrays.stream(method.getParameters()).map(p -> p.getName())
                                .toArray(String[]::new);
                        String path = method.getAnnotation(GetMapping.class).value();
                        System.out.printf("Found GET: %s => %s", path, method);
                        System.out.println();
                        this.getMappings.put(path, new GetDispather(controllerInstance, method, parameterNames,
                                method.getParameterTypes()));
                    }
                }
            }catch (ReflectiveOperationException e) {
                throw new ServletException(e);
            }
        }
        // 创建ViewEngine:
        this.viewEngine = new ViewEngine(getServletContext());
    }

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
