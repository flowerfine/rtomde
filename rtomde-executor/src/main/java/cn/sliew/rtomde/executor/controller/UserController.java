package cn.sliew.rtomde.executor.controller;

import cn.sliew.rtomde.executor.mapper.SysUser;
import cn.sliew.rtomde.executor.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/user/{id}")
    public SysUser getUser(@PathVariable Long id) {
        return sysUserMapper.selectByPrimaryKey(id);
    }
}