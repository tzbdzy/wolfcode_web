package cn.wolfcode.web.modules.custlink.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 客户联系人
 * </p>
 *
 * @author lailin
 * @since 2022-10-09
 */

@Data //lombook
@AllArgsConstructor //有参
@NoArgsConstructor //无参
public class TbCustLinkman implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 客户id
     */
    private String custId;

    /**
     * 联系人名字
     */
    @Excel(name = "联系人的姓名")
    private String linkman;

    /**
     * 性别 1 男 0 女
     */
    @Excel(name = "性别")
    private Integer sex;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 联系人电话
     */
    @Excel(name = "电话")
    private String phone;


    /**
     * 职位
     */
    private String position;

    /**
     * 部门
     */
    private String department;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 录入人
     */
    private String inputUser;

    /**
     * 录入时间
     */
    private LocalDateTime inputTime;

    /**
     * 企业客户名称
     */
    @TableField(exist = false)
    private String customerName;

}
