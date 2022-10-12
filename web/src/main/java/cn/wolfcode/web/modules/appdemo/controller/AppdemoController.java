package cn.wolfcode.web.modules.appdemo.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import cn.wolfcode.web.commons.entity.CodeMsg;
import cn.wolfcode.web.commons.entity.ExcelExportEntityWrapper;
import cn.wolfcode.web.commons.entity.LayuiPage;
import cn.wolfcode.web.commons.utils.LayuiTools;
import cn.wolfcode.web.commons.utils.PoiExportHelper;
import cn.wolfcode.web.commons.utils.PoiImportHelper;
import cn.wolfcode.web.commons.utils.SystemCheckUtils;
import cn.wolfcode.web.modules.BaseController;
import cn.wolfcode.web.modules.log.LogModules;
import cn.wolfcode.web.modules.sys.entity.SysRoleInfo;
import cn.wolfcode.web.modules.sys.entity.SysUser;
import cn.wolfcode.web.modules.sys.entity.SysUserVerifyEntity;
import cn.wolfcode.web.modules.sys.form.LoginForm;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import cn.wolfcode.web.modules.appdemo.entity.Appdemo;
import cn.wolfcode.web.modules.appdemo.service.IAppdemoService;


import link.ahsj.core.annotations.*;
import link.ahsj.core.entitys.ApiModel;
import link.ahsj.core.exception.ErrorCode;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author haiyang
 * @since 2022-10-09
 */
@Controller
@RequestMapping("appdemo")
public class AppdemoController extends BaseController {

    @Autowired
    private IAppdemoService entityService;

    private static final String LogModule = "Appdemo";

    @GetMapping("/list.html")
    public String list() {
        return "app/appdemo/list";
    }

    @RequestMapping("/add.html")
    @PreAuthorize("hasAuthority('app:appdemo:add')")
    public ModelAndView toAdd(ModelAndView mv) {
        mv.setViewName("app/appdemo/add");
        return mv;
    }

    @GetMapping("/{id}.html")
    @PreAuthorize("hasAuthority('app:appdemo:update')")
    public ModelAndView toUpdate(@PathVariable("id") String id, ModelAndView mv) {
        mv.setViewName("app/appdemo/update");
        mv.addObject("obj", entityService.getById(id));
        mv.addObject("id", id);
        return mv;
    }

    @RequestMapping("list")
    @PreAuthorize("hasAuthority('app:appdemo:list')")
    public ResponseEntity page(LayuiPage layuiPage) {
        SystemCheckUtils.getInstance().checkMaxPage(layuiPage);
        IPage page = new Page<>(layuiPage.getPage(), layuiPage.getLimit());
        return ResponseEntity.ok(LayuiTools.toLayuiTableModel(entityService.page(page)));
    }

    @SameUrlData
    @PostMapping("save")
    @SysLog(value = LogModules.SAVE, module =LogModule)
    @PreAuthorize("hasAuthority('app:appdemo:add')")
    public ResponseEntity<ApiModel> save(@Validated({AddGroup.class}) @RequestBody Appdemo entity) {
        entityService.save(entity);
        return ResponseEntity.ok(ApiModel.ok());
    }

    @SameUrlData
    @SysLog(value = LogModules.UPDATE, module = LogModule)
    @PutMapping("update")
    @PreAuthorize("hasAuthority('app:appdemo:update')")
    public ResponseEntity<ApiModel> update(@Validated({UpdateGroup.class}) @RequestBody Appdemo entity) {
        entityService.updateById(entity);
        return ResponseEntity.ok(ApiModel.ok());
    }

    @SysLog(value = LogModules.DELETE, module = LogModule)
    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasAuthority('app:appdemo:delete')")
    public ResponseEntity<ApiModel> delete(@PathVariable("id") String id) {
        entityService.removeById(id);
        return ResponseEntity.ok(ApiModel.ok());
    }

    @GetMapping("import.html")
    public ModelAndView toImport(ModelAndView mv) {

        mv.setViewName("app/appdemo/importAppdemo");
        return mv;
    }

    @SysLog(value = "用户模板", module = "用户管理")
    @GetMapping("template")

    public void template(HttpServletResponse response) throws UnsupportedEncodingException {
        ExcelExportEntityWrapper wrapper = new ExcelExportEntityWrapper();
        wrapper.entity(Appdemo.APP_NAME, "name", 20)
                .entity(Appdemo.APP_INFO, "info", 20);

        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), wrapper.getResult(), new ArrayList<>());

        PoiExportHelper.exportExcel(response, "AppDemo模板", workbook);
    }

    @SysLog(value = LogModules.IMPORT, module = LogModules.USER)
    @PostMapping("import")
    public ResponseEntity importUser(MultipartFile file) throws Exception {

        ImportParams params = PoiImportHelper.buildImportParams(new String[]
                {
                        Appdemo.APP_NAME,
                        Appdemo.APP_INFO
                }, new Class[]{ImportGroup.class});

        ExcelImportResult result = ExcelImportUtil.importExcelMore(file.getInputStream(), Appdemo.class, params);

        entityService.saveBatch(result.getList());

        return  ResponseEntity.ok(ApiModel.ok());
    }

}
