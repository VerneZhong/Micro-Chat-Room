package com.micro.im.controller;

import com.google.common.collect.Lists;
import com.micro.common.constant.FileType;
import com.micro.common.dto.UserDTO;
import com.micro.common.response.ResultVO;
import com.micro.common.util.TokenUtil;
import com.micro.im.configuration.RedisClient;
import com.micro.im.entity.User;
import com.micro.im.req.AddFriendReq;
import com.micro.im.req.ModifySignReq;
import com.micro.im.req.UserLoginReq;
import com.micro.im.req.UserRegisterReq;
import com.micro.im.resp.*;
import com.micro.im.service.FileService;
import com.micro.im.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.micro.common.code.BusinessCode.*;
import static com.micro.common.response.ResultVO.fail;
import static com.micro.common.response.ResultVO.success;

/**
 * 用户 ctrl
 *
 * @author Mr.zxb
 * @date 2020-06-10 09:22
 */
@RestController
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private FileService fileService;

    /**
     * 获取用户和好友列表信息
     * @param token
     * @return
     */
    @GetMapping("/getList.do")
    public ResultVO<GetListResp> getList(@RequestParam String token) {
        log.info("获取用户列表 req: {}", token);
        if (StringUtils.isBlank(token)) {
            return fail(PARAM_ERROR);
        }
        UserDTO userDto = getUserDto(token);
        if (userDto == null) {
            return fail(NO_LOGIN);
        }
        Long id = userDto.getId();
        GetListResp resp = userService.getList(id);
        return success(resp);
    }

    /**
     * 获取群员列表
     * @param id
     * @return
     */
    @GetMapping("/getMembers.do")
    public ResultVO<GetMembersResp> getMembers(@RequestParam Long id) {
        log.info("获取群员列表，群ID: {}", id);
        GetMembersResp members = userService.getMembers(id);
        return success(members);
    }

    /**
     * 注册用户
     * @param userRegisterReq
     * @return
     */
    @PostMapping("/register.do")
    public ResultVO register(@RequestBody UserRegisterReq userRegisterReq) {
        log.info("注册新用户: {}", userRegisterReq.getNickname());
        userService.register(userRegisterReq);
        return success();
    }

    /**
     * 账号是否已存在
     * @param account
     * @return
     */
    @GetMapping("/accountExists.do")
    public ResultVO accountExists(@RequestParam String account) {
        log.info("查看账户是否存在：{}", account);
        boolean exists = userService.accountExists(account);
        return success(exists);
    }

    /**
     * 用户登录
     * @param req
     * @return
     */
    @PostMapping("/login.do")
    public ResultVO<LoginResp> login(@RequestBody UserLoginReq req){
        log.info("用户登录: {}", req.getAccount());
        User user = userService.login(req.getAccount(), req.getPassword());
        if (user != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setAccount(user.getAccount());
            userDTO.setNickname(user.getNickname());
            userDTO.setAge(user.getAge());
            userDTO.setAvatar(user.getAvatarAddress());
            userDTO.setSign(user.getSign());
            userDTO.setAddress(user.getArea());

            // 生成token
            String token = TokenUtil.getToken();

            // 缓存用户到Redis
            redisClient.set(token, userDTO, 3600);
            redisClient.set(user.getId().toString(), "online");
            LoginResp loginResp = new LoginResp();
            loginResp.setToken(token);
            loginResp.setUser(userDTO);
            return success(loginResp);
        }
        return fail(USER_INVALID);
    }

    /**
     * 登出
     * @return
     */
    @GetMapping("/logout")
    public ResultVO logout() {

        return success();
    }

    /**
     * 上传图片
     * @param file
     * @return
     */
    @PostMapping("/uploadImg.do")
    public ResultVO<UploadImageResp> uploadImg(@RequestParam("file") MultipartFile file) {
        log.info("上传图片：{}", file);
        String filepath = fileService.uploadFile(file, FileType.IMG).getSrc();
        return success(new UploadImageResp(filepath));
    }

    /**
     * 上传文件
     * @param file
     * @return
     */
    @PostMapping("/uploadFile.do")
    public ResultVO<UploadFileResp> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("上传文件：{}", file);
        UploadFileResp fileResp = fileService.uploadFile(file, FileType.FILE);
        return success(fileResp);
    }

    /**
     * 修改在线状态
     * @param request
     * @param status
     * @return
     */
    @GetMapping("/modifyStatus.do")
    public ResultVO modifyStatus(HttpServletRequest request, @RequestParam String status) {
        UserDTO dto = getUserDto(request);
        if (dto != null) {
            log.info("修改用户在线状态：{}:{}", dto.getNickname(), status);
            redisClient.set(dto.getId().toString(), status);
            return success();
        }
        return fail(PARAM_ERROR);
    }

    /**
     * 修改签名
     * @param request
     * @param req
     * @return
     */
    @PostMapping("/modifySign.do")
    public ResultVO modifySign(HttpServletRequest request, @RequestBody ModifySignReq req) {
        UserDTO dto = getUserDto(request);
        if (dto != null) {
            log.info("更改用户签名：{}:{}", dto.getNickname(), req);
            User user = new User();
            user.setId(dto.getId());
            user.setSign(req.getSign());
            userService.updateUser(user);
            return success();
        }
        return fail(NO_LOGIN);
    }

    private UserDTO getUserDto(HttpServletRequest request) {
        return getUserDto(request.getHeader("token"));
    }

    private UserDTO getUserDto(String  token) {
        if (token == null) {
            return null;
        }
        return (UserDTO) redisClient.get(token);
    }

    /**
     * 获取好友推荐
     * @param token
     * @return
     */
    @GetMapping("/getRecommend.do")
    public ResultVO<List<RecommendResp>> getRecommend(@RequestParam String token) {
        log.info("获取好友推荐：{}", token);
        UserDTO userDto = getUserDto(token);
        if (userDto != null) {
            List<User> users = userService.getRecommend(userDto.getId());
            List<RecommendResp> resps = users.stream().map(user -> {
                RecommendResp resp = new RecommendResp();
                resp.setId(user.getId().toString());
                resp.setNickname(user.getNickname());
                resp.setAvatar(user.getAvatarAddress());
                resp.setSign(Optional.ofNullable(user.getSign()).orElse(""));
                return resp;
            }).collect(Collectors.toList());
            return success(resps);
        }
        return success(Lists.newArrayList());
    }

    /**
     * 拒绝好友添加
     * @return
     */
    @PostMapping("/refuseFriend.do")
    public ResultVO refuseFriend() {
        return success();
    }

    /**
     * 根据昵称或账号查找好友总数
     * @return
     */
    @GetMapping("/findFriendTotal.do")
    public ResultVO<FindFriendTotalResp> findFriendTotal(@RequestParam String value) {
        log.info("根据账号或昵称查找好友总数: {}", value);
        List<User> userByAccount = userService.findUserByAccountAndName(value, null);
        return success(new FindFriendTotalResp(userByAccount.size()));
    }

    /**
     * 根据昵称或是账号查找好友列表
     * @return
     */
    @GetMapping("/findFriend.do")
    public ResultVO findFriend(@RequestParam String value, @RequestParam Integer page) {
        log.info("根据账号或昵称查找好友: {}:{}", value, page);
        List<User> users = userService.findUserByAccountAndName(value, page);
        List<RecommendResp> resps = users.stream().map(user -> {
            RecommendResp resp = new RecommendResp();
            resp.setId(user.getId().toString());
            resp.setNickname(user.getNickname());
            resp.setAvatar(user.getAvatarAddress());
            resp.setSign(Optional.ofNullable(user.getSign()).orElse(""));
            return resp;
        }).collect(Collectors.toList());
        return success(resps);
    }

    @PostMapping("/addFriendGroup.do")
    public ResultVO addFriendGroup() {

        return success();
    }

    /**
     * 添加好友
     * @param
     */
    @PostMapping("/addFriend.do")
    public ResultVO addFriend(@RequestBody AddFriendReq req) {
        log.info("添加好友req：{}", req);
        return success();
    }

    /**
     * 添加群聊
     * @param
     */
    @PostMapping("/addGroup.do")
    public ResultVO addGroup() {
        return success();
    }

}
