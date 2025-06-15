package site.arookieofc.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import site.arookieofc.pojo.DO.Clazz;

/**
 * 班级视图对象 - 用于Controller层与前端交互
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClazzVO {
    private String id;
    private String name;
    private String teacherId;
    private String teacherName;
    private Integer studentCount;
    
    /**
     * 转换为DO
     */
    public Clazz toDO() {
        Clazz clazz = new Clazz();
        clazz.setId(this.id);
        clazz.setName(this.name);
        clazz.setTeacherId(this.teacherId);
        clazz.setStudentCount(this.studentCount);
        return clazz;
    }
    
    /**
     * 从DO创建VO
     */
    public static ClazzVO fromDO(Clazz clazz) {
        if (clazz == null) {
            return null;
        }
        ClazzVO vo = new ClazzVO();
        vo.setId(clazz.getId());
        vo.setName(clazz.getName());
        vo.setTeacherId(clazz.getTeacherId());
        vo.setStudentCount(clazz.getStudentCount());
        return vo;
    }
}