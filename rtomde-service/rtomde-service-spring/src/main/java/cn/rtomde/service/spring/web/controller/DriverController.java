package cn.rtomde.service.spring.web.controller;

import cn.rtomde.service.spring.web.handler.ConcurrentUrlHandlerMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("driver")
@Tag(name = "driver-api", description = "driver 管理 api")
public class DriverController {

    @Autowired
    private ConcurrentUrlHandlerMapping handlerMapping;

    @PutMapping
    @Operation(summary = "注册 driver", description = "注册 driver")
    public boolean register() {
        handlerMapping.registerHandler("/print", new PrintDriver());
        return true;
    }
}
