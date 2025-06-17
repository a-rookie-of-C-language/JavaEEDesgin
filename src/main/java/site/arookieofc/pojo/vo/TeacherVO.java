package site.arookieofc.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import site.arookieofc.pojo.DO.Teacher;

/**
 * 教师视图对象 - 用于Controller层与前端交互
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeacherVO {
    private String id;
    private String name;
    
    /**
     * 转换为DO
     */
    public Teacher toDO() {
        Teacher teacher = new Teacher();
        teacher.setId(this.id);
        teacher.setName(this.name);
        return teacher;
    }
    
    /**
     * 从DO创建VO
     */
    public static TeacherVO fromDO(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        TeacherVO vo = new TeacherVO();
        vo.setId(teacher.getId());
        vo.setName(teacher.getName());
        return vo;
    }
}