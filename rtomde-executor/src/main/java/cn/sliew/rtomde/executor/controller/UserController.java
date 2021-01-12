package cn.sliew.rtomde.executor.controller;

import cn.sliew.rtomde.executor.mapper.SysUser;
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
    private SqlSessionFactory sqlSessionFactory;

    @GetMapping("/sqluser/{id}")
    public SysUser getSqlUser(@PathVariable Long id) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        List<Object> objects = sqlSession.selectList("selectByPrimaryKey", id);
        return (SysUser) objects.get(0);
    }
}