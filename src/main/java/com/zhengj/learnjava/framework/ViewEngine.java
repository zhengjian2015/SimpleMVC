package com.zhengj.learnjava.framework;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class ViewEngine {

    //final变量必须及时定义 需要构造方法
    private final PebbleEngine engine;

    public ViewEngine(ServletContext servletContext) {
        // 定义一个ServletLoader用于加载模板:
        ServletLoader loader = new ServletLoader(servletContext);
        // 模板编码:
        loader.setCharset("UTF-8");
        // 模板前缀，这里默认模板必须放在`/WEB-INF/templates`目录:
        loader.setPrefix("/WEB-INF/templates");
        // 模板后缀:
        loader.setSuffix("");
        this.engine = new PebbleEngine.Builder()
                .autoEscaping(true) // 默认打开HTML字符转义，防止XSS攻击
                .cacheActive(false) // 禁用缓存使得每次修改模板可以立刻看到效果
                .loader(loader).build();

    }

    public void render(ModelAndView mv, Writer writer) throws IOException {
        // 查找模板:
        PebbleTemplate template = this.engine.getTemplate(mv.view);
        // 渲染:
        template.evaluate(writer, mv.model);
    }
    /*
    public void render(ModelAndView mv, Writer writer) {
        String view = mv.view;
        Map<String, Object> model = mv.model;
        // 根据view找到模板文件:
        Template template = getTemplateByPath(view);
        // 渲染并写入Writer:
        template.write(writer, model);
    }
    */
}
