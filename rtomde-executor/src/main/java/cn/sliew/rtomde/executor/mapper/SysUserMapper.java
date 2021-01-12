package cn.sliew.rtomde.executor.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserMapper {

    SysUser selectByPrimaryKey(@Param("id") Long id);
}