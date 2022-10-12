package cn.wolfcode.web.modules.appdemo.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author haiyang
 * @since 2022-10-09
 */
public class Appdemo implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String APP_NAME="名字";
    public static final String APP_INFO="描述";

    @TableId("ID")
    private String id;

    /**
     * 名称
     */
    @TableField("NAME")
    @Excel(name = APP_NAME)
    private String name;

    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    /**
     * 描述
     */
    @TableField("INFO")
    @Excel(name = APP_INFO)
    private String info;

    @TableField("DESCS")
    private String descs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getDescs() {
        return descs;
    }

    public void setDescs(String descs) {
        this.descs = descs;
    }

    @Override
    public String toString() {
        return "Appdemo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", createTime=" + createTime +
                ", info='" + info + '\'' +
                ", descs='" + descs + '\'' +
                '}';
    }
}
