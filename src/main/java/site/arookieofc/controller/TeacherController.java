package site.arookieofc.controller;

import site.arookieofc.annotation.web.*;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.entity.Teacher;
import site.arookieofc.service.TeacherService;
import site.arookieofc.pojo.dto.Result;

import java.util.List;

@Controller("/teacher")
@Component
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/list")
    public Result getAllTeachers() {
        List<Teacher> teachers = teacherService.getAllTeachers();
        return Result.success("获取教师列表成功", teachers);
    }

    @PostMapping("/add")
    public Result addTeacher(@RequestBody Teacher teacher) {
        teacherService.addTeacher(teacher);
        return Result.success("添加教师成功");
    }

    @PutMapping("/update/{id}")
    public Result updateTeacher(@PathVariable String id, @RequestBody Teacher teacher) {
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