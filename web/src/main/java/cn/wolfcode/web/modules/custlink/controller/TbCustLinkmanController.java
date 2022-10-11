package cn.wolfcode.web.modules.custlink.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.hutool.core.collection.CollectionUtil;
import cn.wolfcode.web.commons.entity.LayuiPage;
import cn.wolfcode.web.commons.utils.LayuiTools;
import cn.wolfcode.web.commons.utils.PoiExportHelper;
import cn.wolfcode.web.commons.utils.SystemCheckUtils;
import cn.wolfcode.web.modules.BaseController;
import cn.wolfcode.web.modules.custinfo.controller.TbCustomerController;
import cn.wolfcode.web.modules.custinfo.entity.TbCustomer;
import cn.wolfcode.web.modules.custinfo.service.ITbCustomerService;
import cn.wolfcode.web.modules.log.LogModules;
import cn.wolfcode.web.modules.sys.entity.SysUser;
import cn.wolfcode.web.modules.sys.form.LoginForm;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.wolfcode.web.modules.custlink.entity.TbCustLinkman;
import cn.wolfcode.web.modules.custlink.service.ITbCustLinkmanService;

import com.mysql.cj.util.StringUtils;
import link.ahsj.core.annotations.AddGroup;
import link.ahsj.core.annotations.SameUrlData;
import link.ahsj.core.annotations.SysLog;
import link.ahsj.core.annotations.UpdateGroup;
import link.ahsj.core.entitys.ApiModel;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author lailin
 * @since 2022-10-09
 */
@Controller
@RequestMapping("custlink")
public class TbCustLinkmanController extends BaseController {

    @Autowired
    private ITbCustLinkmanService entityService;

    //客户业务类
    @Autowired
    private ITbCustomerService customerService;

    private static final String LogModule = "TbCustLinkman";

    @GetMapping("/list.html")
    public ModelAndView list(ModelAndView mv) {
        //获取所有企业客户信息，返回到页面上
        List<TbCustomer> list = customerService.list();
        mv.addObject("custs",list);

        mv.setViewName("cust/custlink/list");
        return mv;
    }

    @RequestMapping("/add.html")
    @PreAuthorize("hasAuthority('cust:custlink:add')")
    public ModelAndView toAdd(ModelAndView mv) {
        //获取所有企业客户信息，返回到页面上
        List<TbCustomer> list = customerService.list();
        mv.addObject("custs",list);

        mv.setViewName("cust/custlink/add");
        return mv;
    }

    @GetMapping("/{id}.html")
    @PreAuthorize("hasAuthority('cust:custlink:update')")
    public ModelAndView toUpdate(@PathVariable("id") String id, ModelAndView mv) {
        //获取所有企业客户信息，返回到页面上
        List<TbCustomer> list = customerService.list();
        mv.addObject("custs",list);

        mv.setViewName("cust/custlink/update");
        mv.addObject("obj", entityService.getById(id));
        mv.addObject("id", id);
        return mv;
    }

    @RequestMapping("list")
    @PreAuthorize("hasAuthority('cust:custlink:list')")
    public ResponseEntity page(LayuiPage layuiPage,String parameterName,String custId) {
        SystemCheckUtils.getInstance().checkMaxPage(layuiPage);
        IPage page = new Page<>(layuiPage.getPage(), layuiPage.getLimit());
        IPage page1 = entityService.lambdaQuery().
                eq(!StringUtils.isEmptyOrWhitespaceOnly(custId), TbCustLinkman::getCustId,custId)
                .like(!StringUtils.isEmptyOrWhitespaceOnly(parameterName), TbCustLinkman::getLinkman,parameterName)//联系人
                .or()
                .like(!StringUtils.isEmptyOrWhitespaceOnly(parameterName),TbCustLinkman::getPhone,parameterName)//电话
                .page(page);


        //判断联系人集合不为空的情况下
        if(CollectionUtil.isNotEmpty(page.getRecords())) {
            List<TbCustLinkman> records = page.getRecords();//拿到所有的数据集合
            records.forEach(obj -> {
                //得到客户id
                String custId1 = obj.getCustId();
                //根据客户id 查询出 企业客户名称
                TbCustomer customer = customerService.getById(custId1);
                if (Objects.nonNull(customer)) {
                    //并赋值
                    obj.setCustomerName(customer.getCustomerName());
                }

            });
        }

        return ResponseEntity.ok(LayuiTools.toLayuiTableModel(page1));
    }

    @SameUrlData
    @PostMapping("save")
    @SysLog(value = LogModules.SAVE, module =LogModule)
    @PreAuthorize("hasAuthority('cust:custlink:add')")
    public ResponseEntity<ApiModel> save(@Validated({AddGroup.class}) @RequestBody TbCustLinkman entity) {
        entityService.save(entity);
        return ResponseEntity.ok(ApiModel.ok());
    }

    @SameUrlData
    @SysLog(value = LogModules.UPDATE, module = LogModule)
    @PutMapping("update")
    @PreAuthorize("hasAuthority('cust:custlink:update')")
    public ResponseEntity<ApiModel> update(@Validated({UpdateGroup.class}) @RequestBody TbCustLinkman entity) {
        entityService.updateById(entity);
        return ResponseEntity.ok(ApiModel.ok());
    }

    @SysLog(value = LogModules.DELETE, module = LogModule)
    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasAuthority('cust:custlink:delete')")
    public ResponseEntity<ApiModel> delete(@PathVariable("id") String id) {
        entityService.removeById(id);
        return ResponseEntity.ok(ApiModel.ok());
    }

    /**
     * 导出
     * @param parameterName
     * @param custId
     */
    @SysLog(value = LogModules.DELETE, module = LogModule)
    @RequestMapping("export")
    public void export(HttpServletResponse response, String parameterName, String custId){
        System.out.println("导出的参数1"+parameterName);
        System.out.println("导出的参数1"+custId);

        //要把什么数据导出到表格当中
        List<TbCustLinkman> list=entityService.lambdaQuery().
                eq(!StringUtils.isEmptyOrWhitespaceOnly(custId),TbCustLinkman::getCustId, custId) //所属企业
                .like(!StringUtils.isEmptyOrWhitespaceOnly(parameterName),TbCustLinkman::getLinkman, parameterName) //联系人
                .or()
                .like(!StringUtils.isEmptyOrWhitespaceOnly(parameterName),TbCustLinkman::getPhone, parameterName)//电话
                .list();

        //执行文件导出 准备工作
        ExportParams exportParams=new ExportParams();
        /**
         * 参数一： 样式
         * 参数二：导出的实体类的字节码  实际上来解析我们的导出的注释列的
         * 参数三：导出的内容
         */
        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, TbCustLinkman.class, list);
        //导出
        try {
            PoiExportHelper.exportExcel(response,"联系人管理",workbook);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

}
