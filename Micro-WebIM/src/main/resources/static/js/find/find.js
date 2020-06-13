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
layui.use(['layim', 'laypage', 'form'], function () {
    var layim = layui.layim
        , layer = layui.layer
        , form = layui.form
        , laytpl = layui.laytpl
        , laypage = layui.laypage;
    form.render();
    $(function () {
        getRecommend();
    });

    /**
     * 推荐好友列表
     */
    function getRecommend() {
        $.get('getRecommend.do', {token: $.cookie("token")}, function (res) {
            console.log(res);
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
        parent.layui.im.addFriendGroup(othis, type);
    });
    //创建群
    $('body').on('click', '.createGroup', function () {
        var othis = $(this);
        parent.layui.im.createGroup(othis);
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