layui.use(['form', 'layer'], function () {
    var form = layui.form,
        layer = layui.layer,
        $ = layui.jquery;

    form.on('submit(Add-filter)', function (data) {
        $.ajax({
            url: web.rootPath() + "visitinfo/save",
            type: "post",
            contentType: "application/json",
            data: JSON.stringify(data.field),
            dataType: 'json',
            traditional: true,
            success: function (data) {
                layer.msg("操作成功", {
                    icon: 1,
                    success: function () {
                        reloadTb("Save-frame", "#SearchBtn");
                    }
                });
            },
            error: function (e) {
                layer.msg(e.responseJSON.message, {icon: 2});
            }

        });
        return false;
    });


    /**
     * 选择所属企业 加载的选择过滤器 【绑定事件】
     */
    form.on('select(customerSelect)', function(data){
        $.ajax({
            url: web.rootPath() + "custlink/listByCustomerId?custId="+data.value,
            type: "post",
            contentType: "application/json",
            dataType: 'json',
            traditional: true,
            success: function (data) {
                //清空原来的数据
                $("#linkman").empty();
                //迭代后台返回过来的数据
                var optionHtml = '<option value="">---请选择---</option>';
                if(data.data.length>0){
                    data.data.forEach(item=>{
                        optionHtml+=`<option value="${item.id}">${item.linkman}</option>`
                    })
                }
                //设置选择信息
                $("#linkman").html(optionHtml);
                //渲染表单  关键一步别忘记了
                form.render('select','component-form-element')

            },
            error: function (e) {
                layer.msg(e.responseJSON.message, {icon: 2});
            }

        });
    });

});
