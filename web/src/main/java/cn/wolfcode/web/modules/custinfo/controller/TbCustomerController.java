package cn.wolfcode.web.modules.custinfo.controller;

import cn.wolfcode.web.commons.entity.LayuiPage;
import cn.wolfcode.web.commons.utils.CityUtils;
import cn.wolfcode.web.commons.utils.LayuiTools;
import cn.wolfcode.web.commons.utils.SystemCheckUtils;
import cn.wolfcode.web.modules.BaseController;
import cn.wolfcode.web.modules.custinfo.entity.TbCustomer;
import cn.wolfcode.web.modules.custinfo.service.ITbCustomerService;
import cn.wolfcode.web.modules.custlink.entity.TbCustLinkman;
import cn.wolfcode.web.modules.custlink.service.ITbCustLinkmanService;
import cn.wolfcode.web.modules.log.LogModules;
import cn.wolfcode.web.modules.sys.entity.SysUser;
import cn.wolfcode.web.modules.sys.form.LoginForm;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysql.cj.util.StringUtils;
import link.ahsj.core.annotations.AddGroup;
import link.ahsj.core.annotations.SameUrlData;
import link.ahsj.core.annotations.SysLog;
import link.ahsj.core.annotations.UpdateGroup;
import link.ahsj.core.entitys.ApiModel;
import link.ahsj.core.exception.AppServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lzg
 * @since 2022-10-09
 */
@Controller
@RequestMapping("custinfo")
public class TbCustomerController extends BaseController {

    @Autowired
    private ITbCustomerService entityService;

    @Autowired
    private ITbCustLinkmanService custLinkmanService;

    private static final String LogModule = "TbCustomer";

    @GetMapping("/list.html")
    public ModelAndView list(ModelAndView mv) {
        mv.addObject("citys", CityUtils.citys);
        mv.setViewName("cust/custinfo/list");
        return mv;
    }

    @RequestMapping("/add.html")
    @PreAuthorize("hasAuthority('cust:custinfo:add')")
    public ModelAndView toAdd(ModelAndView mv) {

        mv.addObject("citys", CityUtils.citys);
        mv.setViewName("cust/custinfo/add");
        return mv;
    }

    @GetMapping("/{id}.html")
    @PreAuthorize("hasAuthority('cust:custinfo:update')")
    public ModelAndView toUpdate(@PathVariable("id") String id, ModelAndView mv) {
        mv.setViewName("cust/custinfo/update");
        mv.addObject("obj", entityService.getById(id));
        mv.addObject("id", id);
        mv.addObject("citys", CityUtils.citys);
        return mv;
    }

    @RequestMapping("list")
    @PreAuthorize("hasAuthority('cust:custinfo:list')")
    public ResponseEntity page(LayuiPage layuiPage,String parameterName,String cityId,String openStatus) {
        SystemCheckUtils.getInstance().checkMaxPage(layuiPage);

        IPage page = new Page<>(layuiPage.getPage(), layuiPage.getLimit());

        IPage page1 = entityService.lambdaQuery()
                .eq(!StringUtils.isEmptyOrWhitespaceOnly(openStatus),TbCustomer::getOpenStatus,openStatus)
                .eq(!StringUtils.isEmptyOrWhitespaceOnly(cityId),TbCustomer::getProvince,cityId)
                .like(!StringUtils.isEmptyOrWhitespaceOnly(parameterName),TbCustomer::getCustomerName,parameterName)
                .or()
                .like(!StringUtils.isEmptyOrWhitespaceOnly(parameterName),TbCustomer::getLegalLeader,parameterName)
                .page(page);

        //??????????????????
        List<TbCustomer> records = page.getRecords();
        for (TbCustomer record:records){
            String cityValue = CityUtils.getCityValue(record.getProvince());
            record.setProvinceName(cityValue);
        }


        return ResponseEntity.ok(LayuiTools.toLayuiTableModel(page1));
    }

    @SameUrlData
    @PostMapping("save")
    @SysLog(value = LogModules.SAVE, module =LogModule)
    @PreAuthorize("hasAuthority('cust:custinfo:add')")
    public ResponseEntity<ApiModel> save(@Validated({AddGroup.class}) @RequestBody TbCustomer entity, HttpServletRequest request) {

        //????????????
        entity.setInputTime(LocalDateTime.now());
        //?????????
        SysUser loginUser = (SysUser)request.getSession().getAttribute(LoginForm.LOGIN_USER_KEY);
        entity.setInputUserId(loginUser.getUserId());

        //????????????????????????????????????
        Integer count = entityService.lambdaQuery()
                .eq(TbCustomer::getCustomerName,entity.getCustomerName())
                .ne(TbCustomer::getId,entity.getId()).count();  //??????????????????
        if (count>0){
            throw new AppServerException("?????????????????????");
        }
        entityService.save(entity);
        return ResponseEntity.ok(ApiModel.ok());
    }

    @SameUrlData
    @SysLog(value = LogModules.UPDATE, module = LogModule)
    @PutMapping("update")
    @PreAuthorize("hasAuthority('cust:custinfo:update')")
    public ResponseEntity<ApiModel> update(@Validated({UpdateGroup.class}) @RequestBody TbCustomer entity) {
        //????????????
        entity.setUpdateTime(LocalDateTime.now());
        entityService.updateById(entity);
        return ResponseEntity.ok(ApiModel.ok());
    }

    @SysLog(value = LogModules.DELETE, module = LogModule)
    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasAuthority('cust:custinfo:delete')")
    public ResponseEntity<ApiModel> delete(@PathVariable("id") String id) {
        entityService.removeById(id);

        //????????????????????????????????????????????????
        custLinkmanService.lambdaUpdate().eq(TbCustLinkman::getCustId, id).remove();
        return ResponseEntity.ok(ApiModel.ok());
    }

}
