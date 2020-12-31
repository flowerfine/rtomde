package cn.sliew.rtomde.executor.controller;

import cn.sliew.rtomde.executor.mapper.SysUser;
import cn.sliew.rtomde.executor.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @GetMapping("/user/{id}")
    public SysUser getUser(@PathVariable Long id) {
        return sysUserMapper.selectByPrimaryKey(id);
    }

    @GetMapping("/sqluser/{id}")
    public SysUser getSqlUser(@PathVariable Long id) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        List<Object> objects = sqlSession.selectList("cn.sliew.rtomde.executor.mapper.SysUserMapper.selectByPrimaryKey", id);
        return (SysUser) objects.get(0);
    }
}