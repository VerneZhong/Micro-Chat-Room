layui.use(['form', 'layer'], function () {
    var form = layui.form;
    // var $ = layui.jquery;
    var layer = layui.layer;
    //监听提交
    form.on('submit(formDemo)', function (data) {
        var index = layer.load();
        $.ajax({
            type: "post",
            data: JSON.stringify(data.field),
            url: "login.do",
            dataType : 'JSON',
            contentType: "application/json",
            success: function (data) {
                layer.close(index);
                if (data.code !== 0) {
                    layer.msg("用户名或密码有误!");
                    $(".layui-input").val('');
                } else {
                    var token = data.data;
                    $.cookie('token', token);
                    window.location.href = "/index";
                }
            }
        });
        return false;
    });
});