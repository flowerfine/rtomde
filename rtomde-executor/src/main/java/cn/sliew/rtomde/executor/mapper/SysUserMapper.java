package cn.sliew.rtomde.executor.mapper;

import org.springframework.stereotype.Repository;

@Repository
public interface SysUserMapper {

    SysUser selectByPrimaryKey(Long id);
}