package site.arookieofc.controller;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.web.*;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.entity.Clazz;
import site.arookieofc.service.ClazzService;
import site.arookieofc.pojo.dto.Result;

import java.util.List;

@Slf4j
@Controller("/class")
@Component
public class ClazzController {

    @Autowired
    private ClazzService clazzService;

    @GetMapping("/list")
    public Result getAllClasses() {
        List<Clazz> clazz = clazzService.getAllClasses();
        return Result.success("获取班级列表成功", clazz);
    }

    @PostMapping("/add")
    public Result addClass(@RequestBody Clazz clazz) {
        clazzService.addClass(clazz);
        return Result.success("添加班级成功");
    }

    @PutMapping("/update/{id}")
    public Result updateClass(@PathVariable String id, @RequestBody Clazz clazz) {
        clazz.setId(id);
        clazzService.updateClass(clazz);
        return Result.success("更新班级成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteClass(@PathVariable String id) {
        clazzService.deleteClass(id);
        return Result.success("删除班级成功");
    }
}