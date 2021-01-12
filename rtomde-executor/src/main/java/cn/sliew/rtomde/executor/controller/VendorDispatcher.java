package cn.sliew.rtomde.executor.controller;

import javassist.ClassPool;
import javassist.CtClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Slf4j
@Component
public class VendorDispatcher {


    @Autowired
    private RequestMappingHandlerMapping mappingRegistry;


    public void register() {

    }

    public void registerHandler() {
    }

}
