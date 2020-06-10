package com.micro.cloud.service;

import com.micro.cloud.resp.GetListResp;
import com.micro.cloud.resp.GetMembersResp;

/**
 * interface
 *
 * @author Mr.zxb
 * @date 2020-06-10 12:33
 */
public interface UserService {
    /**
     * 获取用户list
     * @param userId
     * @return
     */
    GetListResp getList(Long userId);

    /**
     * 获取群员列表
     * @param groupId 群组ID
     * @return
     */
    GetMembersResp getMembers(Long groupId);
}
