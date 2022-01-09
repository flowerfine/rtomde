package cn.rtomde.template.mapping.template;

import cn.rtomde.template.mapping.SqlTemplate;

public interface TemplateEngine {

    SqlTemplate create(TemplateContext context, String template);
}
