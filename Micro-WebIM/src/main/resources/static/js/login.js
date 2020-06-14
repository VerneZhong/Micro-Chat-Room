layui.use(['form', 'layer'], function () {
    var form = layui.form;
    // var $ = layui.jquery;
    var layer = layui.layer;
    //监听提交
    form.on('submit(formDemo)', function (data) {
        var index;
        $.ajax({
            type: "post",
            data: JSON.stringify(data.field),
            url: "login.do",
            dataType : 'JSON',
            contentType: "application/json",
            beforeSend: function () {
               index = layer.load(0, {shade: 0.1});
            },
            success: function (res) {
                layer.close(index);
                if (res.code !== 0) {
                    layer.msg("用户名或密码有误!");
                    $(".layui-input").val('');
                } else {
                    var data = res.data;
                    console.log(data);
                    let token = data.token;
                    $.cookie('token', token);
                    let user = data.user;
                    $.cookie('uid', user.id);
                    $.cookie('userAvatar', user.avatar);
                    window.location.href = "/index";
                }
            }
        });
        return false;
    });
});