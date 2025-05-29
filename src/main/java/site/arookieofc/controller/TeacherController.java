package site.arookieofc.controller;

import site.arookieofc.annotation.web.*;
import site.arookieofc.entity.Teacher;
import site.arookieofc.service.TeacherService;
import site.arookieofc.service.impl.TeacherServiceImpl;
import site.arookieofc.pojo.dto.Result;

import java.util.List;

@Controller("/teacher")
public class TeacherController {
    
    private final TeacherService teacherService = new TeacherServiceImpl();
    
    @GetMapping("/list")
    public Result getAllTeachers() {
        try {
            List<Teacher> teachers = teacherService.getAllTeachers();
            return Result.success("获取教师列表成功", teachers);
        } catch (Exception e) {
            return Result.error("获取教师列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/add")
    public Result addTeacher(@RequestBody Teacher teacher) {
        try {
            teacherService.addTeacher(teacher);
            return Result.success("添加教师成功");
        } catch (Exception e) {
            return Result.error("添加教师失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/update/{id}")
    public Result updateTeacher(@PathVariable String id, @RequestBody Teacher teacher) {
        try {
            teacher.setId(id);
            teacherService.updateTeacher(teacher);
            return Result.success("更新教师成功");
        } catch (Exception e) {
            return Result.error("更新教师失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")
    public Result deleteTeacher(@PathVariable String id) {
        try {
            teacherService.deleteTeacher(id);
            return Result.success("删除教师成功");
        } catch (Exception e) {
            return Result.error("删除教师失败: " + e.getMessage());
        }
    }
}