/**
 *  msg box js
 * @author Mr.zxb
 * @date 2020-07-03 10:32
 */
layui.use(['layim', 'flow'], function () {
    var layim = layui.layim
        , layer = layui.layer
        , laytpl = layui.laytpl
        , $ = layui.jquery
        , flow = layui.flow;

    let cache = {}; //用于临时记录请求到的数据

    // 当前用户信息
    let mine = JSON.parse(window.localStorage.getItem("mine"));
    //请求消息
    var renderMsg = function (page, callback) {
        $.ajax({
            type: "post",
            data: JSON.stringify({
                page: page || 1,
                userId: mine.mine.id
            }),
            url: "getMsgBox.do",
            dataType: 'JSON',
            contentType: "application/json",
            success: function (res) {
                if (res.code !== 0) {
                    return layer.msg(res.msg);
                }
                // 记录来源用户信息
                layui.each(res.data, function (index, item) {
                    cache[item.from] = item.fromInfo;
                });
                callback && callback(res, Math.ceil(res.pages / 10));
            }
        });
    };

    //消息信息流
    flow.load({
        elem: '#LAY_view' //流加载容器
        , isAuto: false
        , end: '<li class="layim-msgbox-tips">暂无更多新消息</li>'
        , done: function (page, next) { //加载下一页
            renderMsg(page, function (res, pages) {
                var html = laytpl(LAY_tpl.value).render({
                    data: res
                    , page: page
                });
                next(html, page < pages);
            });
        }
    });

    //打开页面即把消息标记为已读
    // $.ajax({
    //     type: "get",
    //     url: "setMessageRead.do?uid=" + mine.mine.id,
    //     dataType: 'JSON',
    //     contentType: "application/json"
    // });

    //操作
    var active = {
        //同意
        agree: function (othis) {
            var li = othis.parents('li')
                , from = li.data('from')
                , to = li.data('to')
                , from_group = li.data('fromgroupid')
                , messageId = li.data('id')
                , user = cache[from];
            //选择分组
            layim.setFriendGroup({
                type: 'friend'
                , username: user.username
                , avatar: user.avatar
                , group: mine.friend //获取好友分组数据
                , submit: function (group, index) {
                    $.ajax({
                        type: "post",
                        data: JSON.stringify({
                            uid: to // 当前用户ID
                            , fromGroup: from_group // 对方设定的好友分组
                            , group: group // 我设定的好友分组
                            , friend: from
                            , messageId: messageId
                        }),
                        url: "confirmAddFriend.do",
                        dataType: 'JSON',
                        contentType: "application/json",
                        success: function (res) {
                            if (res.code !== 0) {
                                return layer.msg(res.msg);
                            }
                            //将好友追加到主面板
                            var data = {
                                type: 'friend'
                                , avatar: user.avatar //好友头像
                                , username: user.username //好友昵称
                                , groupid: group //所在的分组id
                                , id: from //好友ID
                                , sign: user.sign //好友签名
                            };
                            parent.layui.layim.addList(data);
                            layer.close(index);
                            othis.parent().html('已同意');
                        }
                    });
                }
            });
        }, refuse: function (othis) {
            //拒绝
            var li = othis.parents('li')
                , to = li.data('to')
                , messageId = li.data('id')
                , from = li.data('from');
            layer.confirm('确定拒绝吗？', function (index) {
                $.post('/refuseFriend.do', {
                    to: to, //对方用户ID
                    messageId : messageId,
                    from: from
                }, function (res) {
                    if (res.code !== 0) {
                        return layer.msg(res.msg);
                    }
                    layer.close(index);
                    othis.parent().html('<em>已拒绝</em>');
                });
            });
        }, chat: function(othis){
            //发起好友聊天
            var  uid = othis.data('uid'), avatar = othis.data('avatar');
            parent.layui.layim.chat({
                name: othis.data('name')
                ,type: othis.data('chattype')
                ,avatar: avatar
                ,id: uid
            });
        }
    };

    $('body').on('click', '.layui-btn', function () {
        var othis = $(this), type = othis.data('type');
        active[type] ? active[type].call(this, othis) : '';
    });
});