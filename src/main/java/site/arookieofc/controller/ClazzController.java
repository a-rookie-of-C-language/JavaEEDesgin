package site.arookieofc.controller;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.web.*;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.pojo.DO.Clazz;
import site.arookieofc.pojo.dto.Result;
import site.arookieofc.pojo.vo.ClazzVO;
import site.arookieofc.service.ClazzService;
import site.arookieofc.utils.ConversionUtils;
import java.util.List;

@Slf4j
@Controller("/class")
@Component
public class ClazzController {

    @Autowired
    private ClazzService clazzService;

    @GetMapping("/list")
    public Result getAllClasses() {
        List<Clazz> clazzes = clazzService.getAllClasses();
        List<ClazzVO> clazzVOs = ConversionUtils.toClazzVOList(clazzes);
        return Result.success("获取班级列表成功", clazzVOs);
    }

    @PostMapping("/add")
    public Result addClass(@RequestBody ClazzVO clazzVO) {
        Clazz clazz = clazzVO.toDO();
        clazzService.addClass(clazz);
        return Result.success("添加班级成功");
    }

    @PutMapping("/update/{id}")
    public Result updateClass(@PathVariable String id, @RequestBody ClazzVO clazzVO) {
        Clazz clazz = clazzVO.toDO();
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