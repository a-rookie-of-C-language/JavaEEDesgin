package site.arookieofc.controller;

import site.arookieofc.annotation.web.*;
import site.arookieofc.entity.Class;
import site.arookieofc.service.ClassService;
import site.arookieofc.service.impl.ClassServiceImpl;
import site.arookieofc.pojo.dto.Result;

import java.util.List;

@Controller("/class")
public class ClassController {
    
    private final ClassService classService = new ClassServiceImpl();
    
    @GetMapping("/list")
    public Result getAllClasses() {
        try {
            List<Class> classes = classService.getAllClasses();
            return Result.success("获取班级列表成功", classes);
        } catch (Exception e) {
            return Result.error("获取班级列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/add")
    public Result addClass(@RequestBody Class clazz) {
        try {
            classService.addClass(clazz);
            return Result.success("添加班级成功");
        } catch (Exception e) {
            return Result.error("添加班级失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/update/{id}")
    public Result updateClass(@PathVariable String id, @RequestBody Class clazz) {
        try {
            clazz.setId(id);
            classService.updateClass(clazz);
            return Result.success("更新班级成功");
        } catch (Exception e) {
            return Result.error("更新班级失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")
    public Result deleteClass(@PathVariable String id) {
        try {
            classService.deleteClass(id);
            return Result.success("删除班级成功");
        } catch (Exception e) {
            return Result.error("删除班级失败: " + e.getMessage());
        }
    }
}