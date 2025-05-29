package site.arookieofc.controller;

import site.arookieofc.annotation.web.*;
import site.arookieofc.entity.Clazz;
import site.arookieofc.service.ClazzService;
import site.arookieofc.service.impl.ClazzServiceImpl;
import site.arookieofc.pojo.dto.Result;

import java.util.List;

@Controller("/class")
public class ClazzController {
    
    private final ClazzService clazzService = new ClazzServiceImpl();
    
    @GetMapping("/list")
    public Result getAllClasses() {
        try {
            List<Clazz> clazz = clazzService.getAllClasses();
            return Result.success("获取班级列表成功", clazz);
        } catch (Exception e) {
            return Result.error("获取班级列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/add")
    public Result addClass(@RequestBody Clazz clazz) {
        try {
            clazzService.addClass(clazz);
            return Result.success("添加班级成功");
        } catch (Exception e) {
            return Result.error("添加班级失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/update/{id}")
    public Result updateClass(@PathVariable String id, @RequestBody Clazz clazz) {
        try {
            clazz.setId(id);
            clazzService.updateClass(clazz);
            return Result.success("更新班级成功");
        } catch (Exception e) {
            return Result.error("更新班级失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")
    public Result deleteClass(@PathVariable String id) {
        try {
            clazzService.deleteClass(id);
            return Result.success("删除班级成功");
        } catch (Exception e) {
            return Result.error("删除班级失败: " + e.getMessage());
        }
    }
}