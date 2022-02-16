package cn.rtomde.service.spring.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("driver")
@Tag(name = "driver-api", description = "driver 管理 api")
public class DriverController {

    @PutMapping
    @Operation(summary = "注入 event", description = "自动注入 event")
    public boolean add() {
        return false;
    }
}
