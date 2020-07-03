/**
 * index.js
 */
layui.config({
    base: 'js/'
});
// 0本机，1线上
var server = 0;
var profile = ['ws://127.0.0.1:8080/im', 'ws://104.155.195.129/im'];
var host = profile[server];
let token = $.cookie("token");
var socket;
//避免重复连接
var lockReconnect = false;
layui.use(['layim', 'contextMenu', 'jquery'], function (layim) {
    let $ = layui.jquery;

    createWebSocket(host);

    let cachedata;

    // 获取基础信息
    layim.on('ready', function (options) {
        // 存储用户数据到本地
        window.localStorage.setItem("mine", JSON.stringify(options));
        // 查看消息盒子离线消息
        msgBox(options.mine.id);

        cachedata = layui.layim.cache();

        // 初始化右键菜单
        contextMenu();
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
            , information: 'getInformation.html' //好友群资料页面
            , isAudio: true //开启聊天工具栏音频
            , isVideo: true //开启聊天工具栏视频
            , notice: true //是否开启桌面消息提醒，默认false
            , systemNotice: true
            , voice: 'default.mp3'
        });
    }

    //定义右键操作
    var contextMenu = function () {
        var my_spread = $('.layim-list-friend >li');
        my_spread.mousedown(function (e) {
            var data = {
                contextItem: "context-friend", // 添加class
                target: function (ele) { // 当前元素
                    $(".context-friend").attr("data-id", ele[0].id.replace(/[^0-9]/ig, "")).attr("data-name", ele.find("span").html());
                    $(".context-friend").attr("data-img", ele.find("img").attr('src')).attr("data-type", 'friend');
                },
                menu: []
            };
            data.menu.push(menuChat());
            data.menu.push(menuInfo());
            data.menu.push(menuChatLog());
            data.menu.push(menuNickName());
            var currentGroupidx = $(this).find('h5').data('groupidx');//当前分组id
            if (my_spread.length >= 2) { //当至少有两个分组时
                var html = '<ul>';
                for (var i = 0; i < my_spread.length; i++) {
                    var groupidx = my_spread.eq(i).find('h5').data('groupidx');
                    if (currentGroupidx != groupidx) {
                        var groupName = my_spread.eq(i).find('h5 span').html();
                        html += '<li class="ui-move-menu-item" data-groupidx="' + groupidx + '" data-groupName="' + groupName + '"><a href="javascript:void(0);"><span>' + groupName + '</span></a></li>'
                    }

                }
                html += '</ul>';
                data.menu.push(menuMove(html));
            }
            data.menu.push(menuRemove());
            $(".layim-list-friend >li > ul > li").contextMenu(data);//好友右键事件
        });

        $(".layim-list-friend >li > h5").mousedown(function (e) {
            var data = {
                contextItem: "context-mygroup", // 添加class
                target: function (ele) { // 当前元素
                    console.log(ele);
                    $(".context-mygroup").attr("data-id", ele.data('groupidx')).attr("data-name", ele.find("span").html());
                },
                menu: []
            };
            data.menu.push(menuAddMyGroup());
            data.menu.push(menuRename());
            if ($(this).parent().find('ul li').data('index') !== 0) {
                data.menu.push(menuDelMyGroup());
            }

            $(this).contextMenu(data);  //好友分组右键事件
        });

        $(".layim-list-group > li").mousedown(function (e) {
            var data = {
                contextItem: "context-group", // 添加class
                target: function (ele) { // 当前元素
                    $(".context-group").attr("data-id", ele[0].id.replace(/[^0-9]/ig, "")).attr("data-name", ele.find("span").html())
                        .attr("data-img", ele.find("img").attr('src')).attr("data-type", 'group')
                },
                menu: []
            };
            data.menu.push(menuChat());
            data.menu.push(menuInfo());
            data.menu.push(menuChatLog());
            data.menu.push(menuLeaveGroupBySelf());

            $(this).contextMenu(data);  //面板群组右键事件
        });


        $('.groupMembers > li').mousedown(function (e) {//聊天页面群组右键事件
            var data = {
                contextItem: "context-group-member", // 添加class
                isfriend: $(".context-group-member").data("isfriend"), // 添加class
                target: function (ele) { // 当前元素
                    $(".context-group-member").attr("data-id", ele[0].id.replace(/[^0-9]/ig, ""));
                    $(".context-group-member").attr("data-img", ele.find("img").attr('src'));
                    $(".context-group-member").attr("data-name", ele.find("span").html());
                    $(".context-group-member").attr("data-isfriend", ele.attr('isfriend'));
                    $(".context-group-member").attr("data-manager", ele.attr('manager'));
                    $(".context-group-member").attr("data-groupidx", ele.parent().data('groupidx'));
                    $(".context-group-member").attr("data-type", 'friend');
                },
                menu: []
            };
            var _this = $(this);
            var groupInfo = layim.thisChat().data;
            var _time = (new Date()).valueOf();//当前时间
            var _gagTime = parseInt(_this.attr('gagTime'));//当前禁言时间
            if (cachedata.mine.id !== _this.attr('id')) {
                data.menu.push(menuChat());
                data.menu.push(menuInfo());
                if (3 == e.which && $(this).attr('isfriend') == 0) { //点击右键并且不是好友
                    data.menu.push(menuAddFriend())
                }
            } else {
                data.menu.push(menuEditGroupNickName());
            }
            if (groupInfo.manager == 1 && cachedata.mine.id !== _this.attr('id')) {//是群主且操作的对象不是自己
                if (_this.attr('manager') == 2) {
                    data.menu.push(menuRemoveAdmin());
                } else if (_this.attr('manager') == 3) {
                    data.menu.push(menuSetAdmin());
                }
                data.menu.push(menuEditGroupNickName());
                data.menu.push(menuLeaveGroup());
                if (_gagTime < _time) {
                    data.menu.push(menuGroupMemberGag());
                } else {
                    data.menu.push(menuLiftGroupMemberGag());
                }
            }//群主管理

            layui.each(cachedata.group, function (index, item) {
                if (item.id == _this.parent().data('groupidx') && item.manager == 2 && _this.attr('manager') == 3 && cachedata.mine.id !== _this.attr('id')) {//管理员且操作的是群员
                    data.menu.push(menuEditGroupNickName());
                    data.menu.push(menuLeaveGroup());
                    if (_gagTime < _time) {
                        data.menu.push(menuGroupMemberGag());
                    } else {
                        data.menu.push(menuLiftGroupMemberGag());
                    }
                }//管理员管理
            })
            $(".groupMembers > li").contextMenu(data);
        })
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

    var menuChat = function () {
        return data = {
            text: "发送消息",
            icon: "&#xe63a;",
            callback: function (ele) {
                var othis = ele.parent(), type = othis.data('type'),
                    name = othis.data('name'), avatar = othis.data('img'),
                    id = othis.data('id');
                // id = (new RegExp(substr).test('layim')?substr.replace(/[^0-9]/ig,""):substr);
                layim.chat({
                    name: name
                    , type: type
                    , avatar: avatar
                    , id: id
                });
            }
        }
    }

    var menuInfo = function () {
        return data = {
            text: "查看资料",
            icon: "&#xe62a;",
            callback: function (ele) {
                var othis = ele.parent(), type = othis.data('type'), id = othis.data('id');
                // id = (new RegExp(substr).test('layim')?substr.replace(/[^0-9]/ig,""):substr);
                getInformation({
                    id: id,
                    type: type
                });
            }
        }
    }

    var getInformation = function (data) {
        var id = data.id || {}, type = data.type || {};
        var index = layer.open({
            type: 2
            , title: type == 'friend' ? (cachedata.mine.id == id ? '我的资料' : '好友资料') : '群资料'
            , shade: false
            , maxmin: false
            // ,closeBtn: 0
            , area: ['400px', '670px']
            , skin: 'layui-box layui-layer-border'
            , resize: true
            , content: cachedata.base.Information + '?id=' + id + '&type=' + type
        });
    }

    var menuChatLog = function () {
        return data = {
            text: "聊天记录",
            icon: "&#xe60e;",
            callback: function (ele) {
                var othis = ele.parent(), type = othis.data('type'), name = othis.data('name'),
                    id = othis.data('id');
                getChatLog({
                    name: name,
                    id: id,
                    type: type
                });
            }
        }
    }

    var getChatLog = function (data) {
        if (!cachedata.base.chatLog) {
            return layer.msg('未开启更多聊天记录');
        }
        var index = layer.open({
            type: 2
            , maxmin: true
            , title: '与 ' + data.name + ' 的聊天记录'
            , area: ['450px', '600px']
            , shade: false
            , skin: 'layui-box'
            , anim: 2
            , id: 'layui-layim-chatlog'
            , content: cachedata.base.chatLog + '?id=' + data.id + '&type=' + data.type
        });
    }

    var menuLeaveGroupBySelf = function () {
        return data = {
            text: "退出该群",
            icon: "&#xe613;",
            callback: function (ele) {
                var othis = ele.parent(),
                    group_id = othis.data('id'),
                    groupname = othis.data('name');
                avatar = othis.data('img');
                layer.confirm('您真的要退出该群吗？退出后你将不会再接收此群的会话消息。<div class="layui-layim-list"><li layim-event="chat" data-type="friend" data-index="0"><img src="' + avatar + '"><span>' + groupname + '</span></li></div>', {
                    btn: ['确定', '取消'], //按钮
                    title: ['提示', 'background:#b4bdb8'],
                    shade: 0
                }, function () {
                    var user = cachedata.mine.id;
                    var username = cachedata.mine.username;
                    leaveGroupBySelf(user, username, group_id);
                }, function () {
                    var index = layer.open();
                    layer.close(index);
                });
            }
        }
    }

    var menuNickName = function () {
        return data = {
            text: "修改好友备注",
            icon: "&#xe6b2;",
            callback: function (ele) {
                var othis = ele.parent(), friend_id = othis.data('id'), friend_name = othis.data('name');
                layer.prompt({title: '修改备注姓名', formType: 0, value: friend_name}, function (nickName, index) {
                    $.get('class/doAction.php?action=editNickName', {
                        nickName: nickName,
                        friend_id: friend_id
                    }, function (data) {
                        if (data.code === 0) {
                            var friendName = $("#layim-friend" + friend_id).find('span');
                            friendName.html(data.data);
                            layer.close(index);
                        }
                        layer.msg(data.msg);
                    });
                });

            }
        }
    }

    var menuMove = function (html) {
        return data = {
            text: "移动联系人",
            icon: "&#xe630;",
            nav: "move",//子导航的样式
            navIcon: "&#xe602;",//子导航的图标
            navBody: html,//子导航html
            callback: function (ele) {
                var friend_id = ele.parent().data('id');//要移动的好友id
                friend_name = ele.parent().data('name');
                var avatar = '../uploads/person/' + friend_id + '.jpg';
                var default_avatar = './uploads/person/empty2.jpg';
                var signature = $('.layim-list-friend').find('#layim-friend' + friend_id).find('p').html();//获取签名
                var item = ele.find("ul li");
                item.hover(function () {
                    var _this = item.index(this);
                    var groupidx = item.eq(_this).data('groupidx');//将好友移动到分组的id
                    $.get('class/doAction.php?action=moveFriend', {
                        friend_id: friend_id,
                        groupidx: groupidx
                    }, function (data) {
                        if (data.code === 0) {
                            layim.removeList({//将好友从之前分组除去
                                type: 'friend'
                                , id: friend_id //好友ID
                            });
                            layim.addList({//将好友移动到新分组
                                type: 'friend'
                                , avatar: im['IsExist'].call(this, avatar) ? avatar : default_avatar //好友头像
                                , username: friend_name //好友昵称
                                , groupid: groupidx //所在的分组id
                                , id: friend_id //好友ID
                                , sign: signature //好友签名
                            });
                        }
                        layer.msg(data.msg);
                    });
                });
            }
        }
    }

    var menuRemove = function () {
        return data = {
            text: "删除好友",
            icon: "&#xe640;",
            events: "removeFriends",
            callback: function (ele) {
                var othis = ele.parent(), friend_id = othis.data('id'), username, sign;
                layui.each(cachedata.friend, function (index1, item1) {
                    layui.each(item1.list, function (index, item) {
                        if (item.id === friend_id) {
                            username = item.username;
                            sign = item.sign;
                        }
                    });
                });
                layer.confirm('删除后对方将从你的好友列表消失，且以后不会再接收此人的会话消息。<div class="layui-layim-list"><li layim-event="chat" data-type="friend" data-index="0"><img src="./uploads/person/' + friend_id + '.jpg"><span>' + username + '</span><p>' + sign + '</p></li></div>', {
                    btn: ['确定', '取消'], //按钮
                    title: ['删除好友', 'background:#b4bdb8'],
                    shade: 0
                }, function () {
                    removeFriends(friend_id);
                }, function () {
                    var index = layer.open();
                    layer.close(index);
                });
            }
        }
    }

    var removeFriends = function (username) {
        conn.removeRoster({
            to: username,
            success: function () {  // 删除成功
                $.get('class/doAction.php?action=removeFriends', {friend_id: username}, function (data) {
                    if (data.code === 0) {
                        var index = layer.open();
                        layer.close(index);
                        layim.removeList({//从我的列表删除
                            type: 'friend' //或者group
                            , id: username //好友或者群组ID
                        });
                        removeHistory({//从我的历史列表删除
                            type: 'friend' //或者group
                            , id: username //好友或者群组ID
                        });
                        parent.location.reload();
                    } else {
                        layer.msg(data.msg);
                    }
                });


            },
            error: function () {
                console.log('removeFriends faild');
                // 删除失败
            }
        });
    }

    var removeHistory = function (data) {//删除好友或退出群后清除历史记录
        var history = cachedata.local.history;
        delete history[data.type + data.id];
        cachedata.local.history = history;
        layui.data('layim', {
            key: cachedata.mine.id
            , value: cachedata.local
        });
        $('#layim-history' + data.id).remove();
        var hisElem = $('.layui-layim').find('.layim-list-history');
        var none = '<li class="layim-null">暂无历史会话</li>'
        if (hisElem.find('li').length === 0) {
            hisElem.html(none);
        }
    }

    var menuAddMyGroup = function () {
        return data = {
            text: "添加分组",
            icon: "&#xe654;",
            callback: function (ele) {
                addMyGroup();
            }
        }
    }

    var menuRename = function () {
        return data = {
            text: "重命名",
            icon: "&#xe642;",
            callback: function (ele) {
                var othis = ele.parent(), mygroupIdx = othis.data('id'), groupName = othis.data('name');
                debugger;
                layer.prompt({title: '请输入分组名，并确认', formType: 0, value: groupName}, function (mygroupName, index) {
                    if (mygroupName) {
                        $.ajax({
                            url: 'editGroupName.do',
                            type:"POST",
                            data: JSON.stringify({
                                groupName: mygroupName,
                                groupId: mygroupIdx
                            }),
                            contentType:"application/json; charset=utf-8",
                            dataType:"json",
                            success: function(data) {
                                if (data.code === 0) {
                                    var friend_group = $(".layim-list-friend li");
                                    for (var j = 0; j < friend_group.length; j++) {
                                        var groupIdx = friend_group.eq(j).find('h5').data('groupidx');
                                        if (groupIdx == mygroupIdx) {//当前选择的分组
                                            friend_group.eq(j).find('h5').find('span').html(mygroupName);
                                        }
                                    }
                                    contextMenu();
                                    layer.close(index);
                                    layer.msg('修改成功');
                                }
                            }
                        })
                    }
                });
            }
        }
    }

    var menuDelMyGroup = function () {
        return data = {
            text: "删除该组",
            icon: "&#x1006;",
            callback: function (ele) {
                var othis = ele.parent(), mygroupIdx = othis.data('id');
                layer.confirm('<div style="float: left;width: 17%;margin-top: 14px;"><i class="layui-icon" style="font-size: 48px;color:#cc4a4a">&#xe607;</i></div><div style="width: 83%;float: left;"> 选定的分组将被删除，组内联系人将会移至默认分组。</div>', {
                    btn: ['确定', '取消'], //按钮
                    title: ['删除分组', 'background:#b4bdb8'],
                    shade: 0
                }, function () {
                    delMyGroup(mygroupIdx);
                }, function () {
                    var index = layer.open();
                    layer.close(index);
                });
            }
        }
    }

    //新增分组
    var addMyGroup = function () {
        layer.prompt({title: '新增分组', formType: 0, value: '', maxlength: 10}, function (name, index) {
            $.ajax({
                url: 'addMyGroup.do',
                type:"POST",
                data: JSON.stringify({
                    userId : cachedata.mine.id,
                    groupName : name
                }),
                contentType:"application/json; charset=utf-8",
                dataType:"json",
                success: function(data) {
                    if (data.code === 0) {
                        $('.layim-list-friend').append('<li><h5 layim-event="spread" lay-type="false" data-id="' + data.data.id + '"><i class="layui-icon">&#xe602;</i><span>' + data.data.name + '</span><em>(<cite class="layim-count"> 0</cite>)</em></h5><ul class="layui-layim-list"><span class="layim-null">该分组下暂无好友</span></ul></li>');
                        contextMenu();
                        location.reload();
                    } else {
                        layer.msg(data.msg);
                    }
                }
            })
        });
    }

    var delMyGroup = function (groupidx) {//删除分组
        $.get('class/doAction.php?action=delMyGroup', {mygroupIdx: groupidx}, function (data) {
            if (data.code == 0) {
                var group = $('.layim-list-friend li') || [];
                for (var j = 0; j < group.length; j++) {//遍历每一个分组
                    groupList = group.eq(j).find('h5').data('groupidx');
                    if (groupList === groupidx) {//要删除的分组
                        if (group.eq(j).find('ul span').hasClass('layim-null')) {//删除的分组下没有好友
                            group.eq(j).remove();
                        } else {
                            // var html = group.eq(j).find('ul').html();//被删除分组的好友
                            var friend = group.eq(j).find('ul li');
                            var number = friend.length;//被删除分组的好友个数
                            for (var i = 0; i < number; i++) {
                                var friend_id = friend.eq(i).attr('id').replace(/^layim-friend/g, '');//好友id
                                var friend_name = friend.eq(i).find('span').html();//好友id
                                var signature = friend.eq(i).find('p').html();//好友id
                                var avatar = '../uploads/person/' + friend_id + '.jpg';
                                var default_avatar = './uploads/person/empty2.jpg';
                                conf.layim.removeList({//将好友从之前分组除去
                                    type: 'friend'
                                    , id: friend_id //好友ID
                                });
                                conf.layim.addList({//将好友移动到新分组
                                    type: 'friend'
                                    , avatar: im['IsExist'].call(this, avatar) ? avatar : default_avatar //好友头像
                                    , username: friend_name //好友昵称
                                    , groupid: data.data //将好友添加到默认分组
                                    , id: friend_id //好友ID
                                    , sign: signature //好友签名
                                });
                            }
                            ;
                        }

                    }
                }
                contextMenu();
                layer.close(layer.index);
            } else {
                layer.msg(data.msg);
            }
        });
    }

    var leaveGroupBySelf = function (to, username, roomId) {
        $.get('class/doAction.php?action=leaveGroup', {groupIdx: roomId, memberIdx: to}, function (data) {
            if (data.code == 0) {
                var option = {
                    to: to,
                    roomId: roomId,
                    success: function (res) {
                        sendMsg({//系统消息
                            mine: {
                                content: username + ' 已退出该群',
                                timestamp: new Date().getTime()
                            },
                            to: {
                                id: roomId,
                                type: 'group',
                                cmd: {
                                    cmdName: 'leaveGroup',
                                    cmdValue: username
                                }
                            }
                        });
                        layim.removeList({
                            type: 'group' //或者group
                            , id: roomId //好友或者群组ID
                        });
                        removeHistory({//从我的历史列表删除
                            type: 'group' //或者group
                            , id: roomId //好友或者群组ID
                        });
                        var index = layer.open();
                        layer.close(index);
                        parent.location.reload();
                    },
                    error: function (res) {
                        console.log('Leave room faild');
                    }
                };
                leaveGroupBySelf(option);
            } else {
                layer.msg(data.msg);
            }
        });
    }

    var menuAddFriend = function () {
        return data = {
            text: "添加好友",
            icon: "&#xe654;",
            callback: function (ele) {
                var othis = ele;
                addFriendGroup(othis, 'friend');
            }
        }
    }

    var addFriendGroup = function (othis, type) {
        var li = othis.parents('li') || othis.parent()
            , uid = li.data('uid') || li.data('id')
            , approval = li.data('approval')
            , name = li.data('name');
        if (uid == 'undifine' || !uid) {
            var uid = othis.parent().data('id'), name = othis.parent().data('name');
        }
        var avatar = './uploads/person/' + uid + '.jpg';
        var isAdd = false;
        if (type == 'friend') {
            var default_avatar = './uploads/person/empty2.jpg';
            if (cachedata.mine.id == uid) {//添加的是自己
                layer.msg('不能添加自己');
                return false;
            }
            layui.each(cachedata.friend, function (index1, item1) {
                layui.each(item1.list, function (index, item) {
                    if (item.id == uid) {
                        isAdd = true;
                    }//是否已经是好友
                });
            });
        } else {
            var default_avatar = './uploads/person/empty1.jpg';
            for (i in cachedata.group)//是否已经加群
            {
                if (cachedata.group[i].id == uid) {
                    isAdd = true;
                    break;
                }
            }
        }
        parent.layui.layim.add({//弹出添加好友对话框
            isAdd: isAdd
            , approval: approval
            , username: name || []
            , uid: uid
            , avatar: im['IsExist'].call(this, avatar) ? avatar : default_avatar
            , group: cachedata.friend || []
            , type: type
            , submit: function (group, remark, index) {//确认发送添加请求
                if (type == 'friend') {
                    $.get('class/doAction.php?action=add_msg', {
                        to: uid,
                        msgType: 1,
                        remark: remark,
                        mygroupIdx: group
                    }, function (data) {
                        if (data.code == 0) {
                            conn.subscribe({
                                to: uid,
                                message: remark
                            });
                            layer.msg('你申请添加' + name + '为好友的消息已发送。请等待对方确认');
                        } else {
                            layer.msg('你申请添加' + name + '为好友的消息发送失败。请刷新浏览器后重试');
                        }
                    });
                } else {
                    var options = {
                        groupId: uid,
                        success: function (resp) {
                            if (approval == '1') {
                                $.get('class/doAction.php?action=add_msg', {
                                    to: uid,
                                    msgType: 3,
                                    remark: remark
                                }, function (data) {
                                    if (data.code == 0) {
                                        layer.msg('你申请加入' + name + '的消息已发送。请等待管理员确认');
                                    } else {
                                        layer.msg('你申请加入' + name + '的消息发送失败。请刷新浏览器后重试');
                                    }
                                });

                            } else {
                                layer.msg('你已加入 ' + name + ' 群');
                            }
                        },
                        error: function (e) {
                            if (e.type == 17) {
                                layer.msg('您已经在这个群组里了');
                            }
                        }
                    };
                    conn.joinGroup(options);
                }
            }, function() {
                layer.close(index);
            }
        });

    }

    var menuEditGroupNickName = function () {
        return data = {
            text: "修改群名片",
            icon: "&#xe60a;",
            callback: function (ele) {
                var othis = ele.parent();
                editGroupNickName(othis);
            }
        }
    }

    var editGroupNickName = function (othis) {
        var memberIdx = othis.data('id'), name = othis.data('name').split('('), groupIdx = othis.data('groupidx');
        layer.prompt({title: '请输入群名片，并确认', formType: 0, value: name[0]}, function (nickName, index) {
            $.get('class/doAction.php?action=editGroupNickName', {
                nickName: nickName,
                memberIdx: memberIdx,
                groupIdx: groupIdx
            }, function (data) {
                if (data.code === 0) {
                    $("ul[data-groupidx=" + groupIdx + "] #" + memberIdx).find('span').html(nickName + '(' + memberIdx + ')');
                    layer.close(index);
                }
                layer.msg(data.msg);
            });
        });
    }

    var menuRemoveAdmin = function () {
        return data = {
            text: "取消管理员",
            icon: "&#xe612;",
            callback: function (ele) {
                var othis = ele.parent();
                removeAdmin(othis);
            }
        }
    }

    var menuSetAdmin = function () {
        return data = {
            text: "设置为管理员",
            icon: "&#xe612;",
            callback: function (ele) {
                var othis = ele.parent(), user = othis.data('id');
                setAdmin(othis);
            }
        }
    }

    var menuLeaveGroup = function () {
        return data = {
            text: "踢出本群",
            icon: "&#x1006;",
            callback: function (ele) {
                var othis = ele.parent();
                var friend_id = ele.parent().data('id');//要禁言的id
                var username = ele.parent().data('name');
                var groupIdx = ele.parent().data('groupidx');
                var list = new Array();
                list[0] = friend_id;
                leaveGroup(groupIdx, list, username)
            }
        }
    }

    var leaveGroup = function (groupIdx, list, username) {//list为数组
        $.get('class/doAction.php?action=leaveGroup', {list: list, groupIdx: groupIdx}, function (data) {
            if (data.code === 0) {
                var options = {
                    roomId: groupIdx,
                    list: list,
                    success: function (resp) {
                        console.log(resp);
                    },
                    error: function (e) {
                        console.log(e);
                    }
                };
                // conn.leaveGroup(options);
                $("ul[data-groupidx=" + groupIdx + "] #" + data.data).remove();
                im.sendMsg({//系统消息
                    mine: {
                        content: username + ' 已被移出该群',
                        timestamp: new Date().getTime()
                    },
                    to: {
                        id: groupIdx,
                        type: 'group',
                        cmd: {
                            cmdName: 'leaveGroup',
                            cmdValue: username
                        }
                    }
                });
                var index = layer.open();
                layer.close(index);
            }
            layer.msg(data.msg);
        });
    }

    var menuGroupMemberGag = function () {
        return data = {
            text: "禁言",
            icon: "&#xe60f;",
            nav: "gag",//子导航的样式
            navIcon: "&#xe602;",//子导航的图标
            navBody: '<ul><li class="ui-gag-menu-item" data-gag="10m"><a href="javascript:void(0);"><span>禁言10分钟</span></a></li><li class="ui-gag-menu-item" data-gag="1h"><a href="javascript:void(0);"><span>禁言1小时</span></a></li><li class="ui-gag-menu-item" data-gag="6h"><a href="javascript:void(0);"><span>禁言6小时</span></a></li><li class="ui-gag-menu-item" data-gag="12h"><a href="javascript:void(0);"><span>禁言12小时</span></a></li><li class="ui-gag-menu-item" data-gag="1d"><a href="javascript:void(0);"><span>禁言1天</span></a></li></ul>',//子导航html
            callback: function (ele) {
                var friend_id = ele.parent().data('id');//要禁言的id
                friend_name = ele.parent().data('name');
                groupidx = ele.parent().data('groupidx');
                var item = ele.find("ul li");
                item.hover(function () {
                    var _index = item.index(this), gagTime = item.eq(_index).data('gag');//禁言时间
                    $.get('class/doAction.php?action=groupMemberGag', {
                        gagTime: gagTime,
                        groupidx: groupidx,
                        friend_id: friend_id
                    }, function (data) {
                        if (data.code === 0) {
                            var gagTime = data.data.gagTime;
                            var res = {
                                mine: {
                                    content: gagTime + '',
                                    timestamp: data.data.time
                                },
                                to: {
                                    type: 'group',
                                    id: groupidx + "",
                                    cmd: {
                                        id: friend_id,
                                        cmdName: 'gag',
                                        cmdValue: data.data.value
                                    }
                                }
                            }
                            im.sendMsg(res);
                            $("ul[data-groupidx=" + groupidx + "] #" + friend_id).attr('gagtime', gagTime);
                        }
                        layer.msg(data.msg);
                    });
                });
            }
        }
    }

    var menuLiftGroupMemberGag = function () {
        return data = {
            text: "取消禁言",
            icon: "&#xe60f;",
            callback: function (ele) {
                var friend_id = ele.parent().data('id');//要禁言的id
                friend_name = ele.parent().data('name');
                groupidx = ele.parent().data('groupidx');
                $.get('class/doAction.php?action=liftGroupMemberGag', {
                    groupidx: groupidx,
                    friend_id: friend_id
                }, function (data) {
                    if (data.code === 0) {
                        var res = {
                            mine: {
                                content: '0',
                                timestamp: data.data.time
                            },
                            to: {
                                type: 'group',
                                id: groupidx + "",
                                cmd: {
                                    id: friend_id,
                                    cmdName: 'liftGag',
                                    cmdValue: data.data.value
                                }
                            }
                        }
                        im.sendMsg(res);
                        $("ul[data-groupidx=" + groupidx + "] #" + friend_id).attr('gagtime', 0);
                    }
                    layer.msg(data.msg);
                });
            }
        }
    }

    var removeAdmin = function (othis) {
        var username = othis.data('id'), friend_avatar = othis.data('img'),
            isfriend = othis.data('isfriend'), name = othis.data('name').split('<'),
            gagTime = othis.data('gagtime'), groupidx = othis.data('groupidx');
        var options = {
            groupId: groupidx,
            username: username,
            success: function (resp) {
                $.get('class/doAction.php?action=setAdmin', {
                    groupidx: groupidx,
                    memberIdx: username,
                    type: 3
                }, function (admin) {
                    if (admin.code === 0) {
                        $("ul[data-groupidx=" + groupidx + "] #" + username).remove();
                        var html = '<li id="' + username + '" isfriend="' + isfriend + '" manager="3" gagTime="' + gagTime + '"><img src="' + friend_avatar + '"><span>' + name[0] + '</span></li>'
                        $("ul[data-groupidx=" + groupidx + "]").append(html);
                        im.contextMenu();
                    }
                    layer.msg(admin.msg);
                });
            },
            error: function (e) {
            }
        };
        conn.removeAdmin(options);
    }

    var setAdmin = function (othis) {
        var username = othis.data('id'), friend_avatar = othis.data('img'),
            isfriend = othis.data('isfriend'), name = othis.data('name'),
            gagTime = othis.data('gagtime'), groupidx = othis.data('groupidx');
        var options = {
            groupId: groupidx,
            username: username,
            success: function (resp) {
                $.get('class/doAction.php?action=setAdmin', {
                    groupidx: groupidx,
                    memberIdx: username,
                    type: 2
                }, function (admin) {
                    if (admin.code === 0) {
                        $("ul[data-groupidx=" + groupidx + "] #" + username).remove();
                        var html = '<li id="' + username + '" isfriend="' + isfriend + '" manager="2" gagTime="' + gagTime + '"><img src="' + friend_avatar + '"><span style="color:#de6039">' + name + '</span><i class="layui-icon" style="color:#eaa48e"></i></li>'
                        $("ul[data-groupidx=" + groupidx + "]").find('li').eq(0).after(html);
                        contextMenu();
                    }
                    layer.msg(admin.msg);
                });
            },
            error: function (e) {
            }
        };
        // 请求后端接口
        // conn.setAdmin(options);
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
    function receivedMessage(res) {
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
        if (lockReconnect) return;
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
        socket.onerror = function (err) {
            console.error("服务器错误: " + err);
            // reconnect(host);
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
        reset: function () {
            clearTimeout(this.timeoutObj);
            clearTimeout(this.serverTimeoutObj);
            return this;
        },
        start: function () {
            var self = this;
            this.timeoutObj = setTimeout(function () {
                //这里发送一个心跳，后端收到后，返回一个心跳消息，
                //onmessage拿到返回的心跳就说明连接正常
                socket.send(JSON.stringify({
                    "type": "heartBeat"
                }));
                self.serverTimeoutObj = setTimeout(function () {//如果超过一定时间还没重置，说明后端主动断开了
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
            var status = res.data.status;
            var onlineMsg = status === 'online' ? '在线' : '离线';
            layim.setChatStatus('<span style="color:#FF5722;">' + onlineMsg + '</span>')
        } else {
            // layim.getMessage({
            //     system: true //系统消息
            //     , id: 111111111
            //     , type: "group"
            //     , content: '加入群聊'
            // });
        }
    });

});

