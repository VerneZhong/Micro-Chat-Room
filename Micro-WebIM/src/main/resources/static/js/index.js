layui.use('layim', function (layim) {
    var token = $.cookie("token");
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
        , msgbox: layui.cache.dir + 'css/modules/layim/html/msgbox.html'
        //发现页面地址
        , find: layui.cache.dir + 'css/modules/layim/html/find.html'
        //聊天记录页面地址
        , chatLog: layui.cache.dir + 'css/modules/layim/html/chatlog.html'
    });
});