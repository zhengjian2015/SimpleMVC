package com.zhengj.learnjava.framework;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GetDispather {
    Object instance;             //Controller实例
    Method method;               //Controller方法
    String[] paramterNames;      //方法参数名称
    Class<?>[] parameterClasses; //方法参数类型

    public GetDispather(Object instance, Method method, String[] parameterNames, Class<?>[] parameterClasses) {
        super();
        this.instance = instance;
        this.method = method;
        this.paramterNames = parameterNames;
        this.parameterClasses = parameterClasses;
    }

    //通过构造某个方法需要的所有参数列表，使用反射调用该方法后返回结果
    public ModelAndView invoke(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException {
        Object[] arguments = new Object[parameterClasses.length];
        for (int i = 0; i < parameterClasses.length; i++) {
            //方法参数对应一个参数名称 共用一个变量i
            String parameterName = paramterNames[i];
            Class<?> parameterClass = parameterClasses[i];
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx33");
            System.out.println(parameterClass);
            if (parameterClass == HttpServletRequest.class) {
                arguments[i] = request;
            } else if (parameterClass == HttpServletResponse.class) {
                arguments[i] = response;
            } else if (parameterClass == HttpSession.class) {
                arguments[i] = request.getSession();
            } else if (parameterClass == int.class) {
                arguments[i] = Integer.valueOf(getOrDefault(request, parameterName, "0"));
            } else if (parameterClass == boolean.class) {
                arguments[i] = Boolean.valueOf(getOrDefault(request, parameterName, "false"));
            } else if (parameterClass == String.class) {
                arguments[i] = getOrDefault(request, parameterName, "");
            } else {
                throw new RuntimeException("Missing hadler for type" + parameterClass);
            }

        }
        return (ModelAndView) this.method.invoke(this.instance,arguments);
    }


    private String getOrDefault(HttpServletRequest request, String name, String defaultValue) {
        String s = request.getParameter(name);
        return s == null ? defaultValue : s;
    }
}
