package com.micro.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.im.entity.User;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Mr.zxb
 * @since 2020-06-10
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询数量
     * @return
     */
    Integer selectCount();

}
