/**
 * index.js
 */
layui.config({
    base: 'js/'
});
var host = 'ws://127.0.0.1:8080/im';
let token = $.cookie("token");
var socket;
//避免重复连接
var lockReconnect = false;
layui.use(['layim', 'jquery'], function (layim) {
    let $ = layui.jquery;

    createWebSocket(host);

    // 获取基础信息
    layim.on('ready', function (options) {
        // 存储用户数据到本地
        window.localStorage.setItem("mine", JSON.stringify(options));
        // 查看消息盒子离线消息
        msgBox(options.mine.id);
    });

    function initLayIM() {
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
                token: token
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
            type: 'chat',
            data: res
        }));
    });

    // 接收消息
    function receivedMessage (res) {
        let data = JSON.parse(res.data);
        console.log(data);
        let emit = data.type;
        let obj = data.data;
        if (emit === 'chat') {
            layim.getMessage(obj);
        } else if (emit === 'system') {
            layim.getMessage(obj);
        } else if (emit === 'addFriend' || emit === 'refuseFriend') {
            // 刷新消息盒子数量
            msgBox(obj);
        } else if (emit === 'confirmAddFriend') {
            // 确认添加好友
            layim.addList(obj);
        } else if (emit === 'setFriendStatus') {
            // 监听好友是否在线
            layim.setFriendStatus(obj.id, obj.status);
        }
    }

    function reconnect(url) {
        console.log("重连服务器");
        if(lockReconnect) return;
        lockReconnect = true;
        //没连接上会一直重连，设置延迟避免请求过多
        setTimeout(function () {
            createWebSocket(url);
            lockReconnect = false;
        }, 2000);
    }

    function createWebSocket(url) {
        try {
            socket = new WebSocket(url);
            initEventHandle();
        } catch (e) {
            reconnect(url);
        }
    }

    function initEventHandle() {
        socket.onclose = function () {
            console.log("服务器关闭");
            reconnect(host);
        };
        socket.onerror = function () {
            console.log("服务器错误");
            reconnect(host);
        };
        socket.onopen = function () {
            console.log('连接服务器成功！');
            socket.send(JSON.stringify({
                type: "login",
                data: token
            }));
            initLayIM();
            //心跳检测重置
            heartCheck.reset().start();
        };
        socket.onmessage = function (event) {
            heartCheck.reset().start();
            //如果获取到消息，心跳检测重置
            //拿到任何消息都说明当前连接是正常的
            receivedMessage(event);
        }
    }

    //心跳检测
    var heartCheck = {
        timeout: 60000,//60秒
        timeoutObj: null,
        serverTimeoutObj: null,
        reset: function(){
            clearTimeout(this.timeoutObj);
            clearTimeout(this.serverTimeoutObj);
            return this;
        },
        start: function(){
            var self = this;
            this.timeoutObj = setTimeout(function(){
                //这里发送一个心跳，后端收到后，返回一个心跳消息，
                //onmessage拿到返回的心跳就说明连接正常
                socket.send("HeartBeat");
                self.serverTimeoutObj = setTimeout(function(){//如果超过一定时间还没重置，说明后端主动断开了
                    socket.close();//如果onclose会执行reconnect，我们执行ws.close()就行了.如果直接执行reconnect 会触发onclose导致重连两次
                }, self.timeout)
            }, this.timeout)
        }
    }

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

