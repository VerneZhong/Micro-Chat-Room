layui.use(['form', 'layer'], function () {
    var form = layui.form;
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
                    layer.msg(res.msg);
                    $(".layui-input").val('');
                } else {
                    let data = res.data;
                    $.cookie('token', data);
                    window.location.href = "/index";
                }
            }
        });
        return false;
    });
});