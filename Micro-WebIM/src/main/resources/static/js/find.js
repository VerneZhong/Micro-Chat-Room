/**
 *
 * @author Mr.zxb
 * @date 2020-06-13 20:27:23
 */
layui.config({
    base: 'js/'
}).extend({
    socket: 'socket'
});
layui.use(['layim', 'laypage', 'form', 'socket'], function () {
    var layim = layui.layim
        , layer = layui.layer
        , laytpl = layui.laytpl
        , form = layui.form
        , $ = layui.jquery
        , laypage = layui.laypage;
    let cache = JSON.parse(window.localStorage.getItem("mine"));
    form.render();
    $(function () {
        getRecommend();
    });

    /**
     * 推荐好友列表
     */
    function getRecommend() {
        $.get('getRecommend.do', {token: $.cookie("token")}, function (res) {
            var html = laytpl(LAY_tpl.value).render({
                data: res.data,
                legend: '推荐好友',
                type: 'friend'
            });
            $('#LAY_view').html(html);
        });
    }

    //添加好友
    $('body').on('click', '.add', function () {
        var othis = $(this), type = othis.data('type');
        var li = othis.parents('li') || othis.parent()
            , uid = li.data('uid') || li.data('id')
            , approval = li.data('approval')
            , avatar = li.data('avatar')
            , name = li.data('name');
        if (uid === 'undefined' || !uid) {
            var uid = othis.parent().data('id'), name = othis.parent().data('name');
        }
        var isAdd = false;
        var mineId = cache.mine.id;
        if (type === 'friend') {
            var default_avatar = 'image/photo/empty2.jpg';
            if (mineId === uid) {
                layer.msg('不能添加自己');
                return false;
            }
            layui.each(cache.friend, function (index1, item1) {
                layui.each(item1.list, function (index, item) {
                    if (item.id === uid) {
                        isAdd = true;
                    }
                });
            });
        } else {
            var default_avatar = 'image/photo/empty1.jpg';
            //是否已经加群
            for (i in cache.group) {
                if (cache.group[i].id == uid) {
                    isAdd = true;
                    break;
                }
            }
        }
        //弹出添加好友对话框
        layim.add({
            isAdd: isAdd
            , approval: approval
            , username: name || []
            , uid: uid
            , avatar: avatar ? avatar : default_avatar
            , group: cache.friend || []
            , type: type
            , submit: function (group, remark) {
                //确认发送添加请求
                if (type === 'friend') {
                    $.ajax({
                        type: "post",
                        data: JSON.stringify({
                            uid: mineId,
                            friend: uid,
                            remark: remark,
                            friendgroup: group
                        }),
                        url: "sendAddFriendReq.do",
                        dataType : 'JSON',
                        contentType: "application/json",
                        success: function (res) {
                            if (res.code === 0) {
                                layer.msg('你申请添加' + name + '为好友的消息已发送。请等待对方确认');
                            } else {
                                layer.msg('你申请添加' + name + '为好友的消息发送失败。请刷新浏览器后重试');
                            }
                        }
                    });
                } else {
                    var options = {
                        groupId: uid,
                        success: function (resp) {
                            if (approval === '1') {
                                $.post('addGroup.do', {
                                    to: uid,
                                    msgType: 3,
                                    remark: remark
                                }, function (res) {
                                    var data = eval('(' + res + ')');
                                    if (data.code === 0) {
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
                            if (e.type === 17) {
                                layer.msg('您已经在这个群组里了');
                            }
                        }
                    };
                }
            }, function() {
                layer.close(index);
            }
        });
    });
    //创建群
    $('body').on('click', '.createGroup', function () {
        var othis = $(this);
        // im.createGroup(othis);
    });
    //返回推荐好友
    $('body').on('click', '.back', function () {
        getRecommend();
        $("#LAY_page").css("display", "none");
    });

    $("body").keydown(function (event) {
        if (event.keyCode == 13) {
            $(".find").click();
        }
    });
    $('body').on('click', '.find', function () {
        $("#LAY_page").css("display", "block");
        var othis = $(this), input = othis.parents('.layui-col-space3').find('input').val();
        var addType = $('input:radio:checked').val();
        if (input) {
            $.get('findFriendTotal.do', {value: input}, function (res) {
                if (res.code != 0) {
                    return layer.msg(res.msg);
                }
                laypage.render({
                    elem: 'LAY_page'
                    , count: res.data.count
                    , limit: res.data.limit
                    , prev: '<i class="layui-icon">&#58970;</i>'
                    , next: '<i class="layui-icon">&#xe65b;</i>'
                    , layout: ['prev', 'next']
                    , curr: res.data.limit
                    , jump: function (obj, first) {
                        //obj包含了当前分页的所有参数，比如：
                        //首次不执行
                        if (first) {
                            var page = res.data.limit;
                        } else {
                            var page = obj.curr;
                        }
                        $.get('findFriend.do', {value: input, page: obj.curr || 1}, function (data) {
                            var html = laytpl(LAY_tpl.value).render({
                                data: data.data,
                                legend: '<a class="back"><i class="layui-icon">&#xe65c;/>返回</a> 查找结果',
                                type: addType
                            });
                            $('#LAY_view').html(html);
                        });
                    }
                });
            });
        }
    });
});