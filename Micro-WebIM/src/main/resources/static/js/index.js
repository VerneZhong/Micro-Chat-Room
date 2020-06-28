/**
 * index.js
 */
layui.config({
    base: 'js/'
});
var host = 'ws://127.0.0.1:8080/im';
let token = $.cookie("token");
// 记录当前时间并转成时间戳
// const now = new Date().getTime();
// // 从缓存中获取用户上次退出的时间戳
// const leaveTime = parseInt(localStorage.getItem('leaveTime'), 10);
// // 判断是否为刷新，两次间隔在5s内判定为刷新操作
// const refresh = (now - leaveTime) <= 5000;
layui.use(['layim', 'jquery'], function (layim) {
    let $ = layui.jquery;
    var socket = new WebSocket(host);
    //基础配置
    layim.config({
        init: {
            url: 'getList.do?token=' + token
        }
        //获取群员接口（返回的数据格式见下文）
        , members: {
            url: 'getMembers.do'
            , type: 'get'
            , data: {} //额外参数
        }
        //上传图片接口
        , uploadImage: {
            url: 'uploadImg.do' //接口地址
            , type: 'post' //默认post
        }
        //上传文件接口
        , uploadFile: {
            url: 'uploadFile.do' //接口地址
            , type: 'post' //默认post
        }
        //消息盒子页面地址
        , msgbox: 'msgbox'
        //发现页面地址
        , find: 'find'
        //聊天记录页面地址
        , chatLog: 'chatlog'
        , isAudio: true //开启聊天工具栏音频
        , isVideo: true //开启聊天工具栏视频
        , notice: true //是否开启桌面消息提醒，默认false
        , voice: 'default.mp3'
    });

    // 获取基础信息
    layim.on('ready', function (options) {
        // 存储用户数据到本地
        window.localStorage.setItem("mine", JSON.stringify(options));
        // 查看消息盒子离线消息
        msgBox(options.mine.id);

        // 加载成功时触发
        socket.onopen = function () {
            console.log('连接服务器成功！');
            login(options.mine.id);
        };
    });

    // ws open send login event
    function login(id) {
        let cache = JSON.parse(window.localStorage.getItem("mine"));
        let uid =  id ? id : cache.mine.id;
        socket.send(JSON.stringify({
            type: "login",
            data: uid
        }));
    }

    function msgBox(uid) {
        $.ajax({
            type: "get",
            headers: {
                Accept: "application/json; charset=utf-8",
                token: token
            },
            url: "getMsgBoxCount.do?uid=" + uid,
            contentType: "application/json",
            success: function (res) {
                if (res.code === 0 && res.data > 0) {
                    layim.msgbox(res.data);
                }
            }
        });
    }

    // 连接成功时触发
    socket.onopen = function () {
        console.log('连接服务器成功！');
        login(null);
    };

    // 关闭连接
    socket.onclose = function () {
        console.log('服务器关闭！');
        // 是否重连
        socket = new WebSocket(host);
    };

    // 监听在线状态切换
    layim.on('online', function (status) {
        $.ajax({
            type: "get",
            headers: {      //请求头
                Accept: "application/json; charset=utf-8",
                token: token  //这是获取的token
            },
            url: "modifyStatus.do?status=" + status,
            contentType: "application/json",
            success: function (data) {}
        });
    });

    // 监听修改签名
    layim.on('sign', function (sign) {
        $.ajax({
            type: "post",
            headers: {      //请求头
                Accept: "application/json; charset=utf-8",
                token: token
            },
            url: "modifySign.do",
            data: JSON.stringify({sign: sign}),
            contentType: "application/json",
            success: function (data) {}
        });
    });

    // 监听更换背景皮肤
    layim.on('setSkin', function (filename, src) {

    });

    // 监听发送消息
    layim.on('sendMessage', function (res) {
        // 发送消息到服务器
        socket.send(JSON.stringify({
            type: 'chat',
            data: res
        }));
    });

    // 接收消息
    socket.onmessage = function (res) {
        console.log(res);
        // 事件名称
        let data = JSON.parse(res.data);
        console.log(data);
        debugger;
        let emit = data.type;
        if (emit === 'chat') {
            layim.getMessage(data.data);
        } else if (emit === 'system') {
            layim.getMessage(data.data);
        } else if (emit === 'addFriend') {
            // 刷新消息盒子数量
            msgBox(data.data);
        } else if (emit === 'confirmAddFriend') {
            // 确认添加好友
            // layim.addList(data.data);
        }
    };

    // 监听查看群成员
    // 在群聊面板中查看全部成员时触发，返回获取群员列表的resp信息
    layim.on('members', function (data) {
        console.log(data);
    });

    // 监听聊天窗口的切换
    layim.on('chatChange', function (res) {
        // 更新当前会话状态，用于显示对方输入状态、在线离线状态等
        var type = res.data.type;
        console.log(res);
        // 私聊
        if (type === 'friend') {
            layim.setChatStatus('<span style="color:#FF5722;">在线</span>')
        } else {
            // layim.getMessage({
            //     system: true //系统消息
            //     , id: 111111111
            //     , type: "group"
            //     , content: '加入群聊'
            // });
        }
    });

    // 弹出添加好友面板
    // layim.add({
    //     type: 'friend' //friend：申请加好友、group：申请加群
    //     ,username: 'xxx' //好友昵称，若申请加群，参数为：groupname
    //     ,avatar: 'a.jpg' //头像
    //     ,submit: function(group, remark, index){
    //         // 发送 ws，以通知对方
    //         console.log(group); //获取选择的好友分组ID，若为添加群，则不返回值
    //         console.log(remark); //获取附加信息
    //         layer.close(index); //关闭改面板
    //     }
    // });

    // 好友分组面板
    // layim.setFriendGroup({
    //     type: 'friend'
    //     ,username: 'xxx' //好友昵称，若申请加群，参数为：groupname
    //     ,avatar: 'a.jpg' //头像
    //     ,group: layim.cache().friend //获取好友列表数据
    //     ,submit: function(group, index){
    //         //一般在此执行Ajax和WS，以通知对方已经同意申请
    //         //……
    //
    //         //同意后，将好友追加到主面板
    //         layim.addList(data); //见下文
    //     }
    // });

});

$(function () {
    // window.onunload = function (e) {
        // 将退出时间存于localstorage中
        // localStorage.setItem('leaveTime', new Date().getTime());
        // layer.open({
        //     content: '确定离开聊天室吗？',
        //     yes: function (index, layero) {
        //         layer.close(index);
        //     },
        //     cancel: function (index, layero) {
        //         if (confirm('确定离开聊天室吗？')) { //只有当点击confirm框的确定时，该层才会关闭
        //             layer.close(index)
        //             $.ajax({
        //                 type: "get",
        //                 headers: {      //请求头
        //                     Accept: "application/json; charset=utf-8",
        //                     token: token
        //                 },
        //                 url: "logout.do",
        //                 contentType: "application/json",
        //                 success: function (res) {
        //                     // clean cookie or db
        //                     if (res.code === 0) {
        //                         window.localStorage.removeItem("mine");
        //                         $.cookie('token', null);
        //                     }
        //                 }
        //             });
        //         }
        //         return false;
        //     }
        // });
    // }
});

