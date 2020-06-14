var token = $.cookie("token");
if (token === null) {
    window.location.href = "/login";
}
//layui绑定扩展
layui.config({
    base: 'js/'
});
layui.use(['layim', 'jquery'], function (layim) {
    var $ = layui.jquery;
    //基础配置
    layim.config({
        //获取主面板列表信息，下文会做进一步介绍
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

    layim.on('ready', function (options) {
        console.log("ready...");
        console.log(options);
        //do something
    });

    // ws 地址
    var socket = new WebSocket('ws://localhost:8080/im');
    // 连接成功时触发
    socket.onopen = function () {
        console.log('连接服务器成功！');
    };

    // Connection opened
    socket.addEventListener('open', function (event) {
        socket.send(JSON.stringify({
            type: "login",
            uid: $.cookie("uid")
        }));
    });

    // 关闭连接
    socket.onclose = function () {
        console.log('服务器关闭！');
    };

    // 出现错误
    socket.onerror = function (error) {
        console.log(error);
    };

    // 监听在线状态切换
    layim.on('online', function (status) {
        $.ajax({
            type: "get",
            headers: {      //请求头
                Accept: "application/json; charset=utf-8",
                token: "" + token  //这是获取的token
            },
            url: "modifyStatus.do?status=" + status,
            contentType: "application/json",
            success: function (data) {

            }
        });
    });

    // 监听修改签名
    layim.on('sign', function (sign) {
        $.ajax({
            type: "post",
            headers: {      //请求头
                Accept: "application/json; charset=utf-8",
                token: "" + token  //这是获取的token
            },
            url: "modifySign.do",
            data: JSON.stringify({sign: sign}),
            contentType: "application/json",
            success: function (data) {

            }
        });
    });

    // 监听更换背景皮肤
    layim.on('setSkin', function (filename, src) {

    });

    // 监听发送消息
    layim.on('sendMessage', function (res) {
        // 发送消息到服务器
        socket.send(JSON.stringify({
            type: 'chatMessage',
            data: res
        }));
    });

    // 接收消息
    socket.onmessage = function (res) {
        console.log(res);
        // 事件名称
        var emit = res.emit;
        var data = res.data;
        console.log(emit);
        console.log(data);
        layim.getMessage(JSON.parse(data));
        if (emit === 'chatMessage') {

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
        // 模拟数据
        // 私聊
        if (type === 'friend') {
            layim.setChatStatus('<span style="color:#FF5722;">在线</span>')
        } else {
            // 模拟系统消息
            // layim.getMessage(res.data);
            layim.getMessage({
                system: true //系统消息
                , id: 111111111
                , type: "group"
                , content: '加入群聊'
            });
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

    // 添加好友/群到主面板

});

