package site.arookieofc.utils;

import site.arookieofc.pojo.DO.Clazz;
import site.arookieofc.pojo.DO.Student;
import site.arookieofc.pojo.DO.Teacher;
import site.arookieofc.pojo.dto.PageResult;
import site.arookieofc.pojo.dto.StudentDTO;
import site.arookieofc.pojo.vo.ClazzVO;
import site.arookieofc.pojo.vo.StudentVO;
import site.arookieofc.pojo.vo.TeacherVO;
import site.arookieofc.service.ClazzService;
import site.arookieofc.service.TeacherService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 对象转换工具类
 * 统一处理VO、DTO、DO之间的转换逻辑
 */
public class ConversionUtils {
    
    /**
     * 将Student转换为StudentVO，包含关联的教师和班级信息
     */
    public static StudentVO toStudentVO(Student student, TeacherService teacherService, ClazzService clazzService) {
        StudentDTO dto = student.toDTO();
        
        // 查询教师名称
        Teacher teacher = teacherService.getTeacherById(student.getTeacherId());
        dto.setTeacherName(teacher.getName());
        // 查询班级名称
        Clazz clazz = clazzService.getClassById(student.getClazzId());
        dto.setClazzName(clazz.getName());
        return StudentVO.fromDTO(dto);
    }
    
    /**
     * 将Student列表转换为StudentVO列表
     */
    public static List<StudentVO> toStudentVOList(List<Student> students, TeacherService teacherService, ClazzService clazzService) {
        return students.stream()
                .map(student -> toStudentVO(student, teacherService, clazzService))
                .collect(Collectors.toList());
    }
    
    /**
     * 将Student分页结果转换为StudentVO分页结果
     */
    public static PageResult<StudentVO> toStudentVOPageResult(PageResult<Student> pageResult, TeacherService teacherService, ClazzService clazzService) {
        List<StudentVO> studentVOs = toStudentVOList(pageResult.getData(), teacherService, clazzService);
        return new PageResult<>(
                studentVOs,
                pageResult.getTotal(),
                pageResult.getPage(),
                pageResult.getSize()
        );
    }
    
    /**
     * 将StudentVO转换为Student实体
     */
    public static Student toStudentEntity(StudentVO studentVO) {
        StudentDTO dto = studentVO.toDTO();
        return dto.toEntity();
    }
    
    /**
     * 将Teacher列表转换为TeacherVO列表
     */
    public static List<TeacherVO> toTeacherVOList(List<Teacher> teachers) {
        return teachers.stream()
                .map(TeacherVO::fromDO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将Clazz列表转换为ClazzVO列表
     */
    public static List<ClazzVO> toClazzVOList(List<Clazz> clazzes) {
        return clazzes.stream()
                .map(ClazzVO::fromDO)
                .collect(Collectors.toList());
    }
}