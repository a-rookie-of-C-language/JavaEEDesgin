package site.arookieofc.controller;

import site.arookieofc.annotation.web.*;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.pojo.DO.Teacher;
import site.arookieofc.pojo.dto.Result;
import site.arookieofc.pojo.vo.TeacherVO;
import site.arookieofc.service.TeacherService;
import site.arookieofc.utils.ConversionUtils;
import java.util.List;

@Controller("/teacher")
@Component
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/list")
    public Result getAllTeachers() {
        List<Teacher> teachers = teacherService.getAllTeachers();
        List<TeacherVO> teacherVOs = ConversionUtils.toTeacherVOList(teachers);
        return Result.success("获取教师列表成功", teacherVOs);
    }

    @PostMapping("/add")
    public Result addTeacher(@RequestBody TeacherVO teacherVO) {
        Teacher teacher = teacherVO.toDO();
        teacherService.addTeacher(teacher);
        return Result.success("添加教师成功");
    }

    @PutMapping("/update/{id}")
    public Result updateTeacher(@PathVariable String id, @RequestBody TeacherVO teacherVO) {
        Teacher teacher = teacherVO.toDO();
        teacher.setId(id);
        teacherService.updateTeacher(teacher);
        return Result.success("更新教师成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteTeacher(@PathVariable String id) {
        teacherService.deleteTeacher(id);
        return Result.success("删除教师成功");
    }
}