package site.arookieofc.controller;

import site.arookieofc.annotation.web.*;
import site.arookieofc.utils.Result;
import site.arookieofc.pojo.dto.StudentDTO;

@Controller("/student")
public class StudentController {
    
    @GetMapping("/list")
    public Result getStudentList(@RequestParam("page") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size) {
        // 模拟学生列表数据
        java.util.List<StudentDTO> students = new java.util.ArrayList<>();
        for (int i = 1; i <= size; i++) {
            StudentDTO student = new StudentDTO();
            student.setId(i);
            student.setName("Student " + i);
            student.setAge(20 + i % 5);
            student.setTeacherId("T00" + (i % 3 + 1));
            student.setClazz("Class " + (i % 3 + 1));
            students.add(student);
        }
        return Result.success("获取学生列表成功", students);
    }
    
    @GetMapping("/info/{id}")
    public Result getStudentInfo(@PathVariable("id") int id) {
        // 模拟学生信息
        StudentDTO student = new StudentDTO();
        student.setId(id);
        student.setName("Student " + id);
        student.setAge(20);
        student.setTeacherId("T001");
        student.setClazz("Class A");
        return Result.success("获取学生信息成功", student);
    }
    
    @PostMapping("/add")
    public Result addStudent(StudentDTO student) {
        // 模拟添加学生逻辑
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            return Result.error("学生姓名不能为空");
        }
        return Result.success("学生 " + student.getName() + " 添加成功");
    }
    
    @PutMapping("/update/{id}")
    public Result updateStudent(@PathVariable("id") int id, StudentDTO student) {
        // 模拟更新学生逻辑
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            return Result.error("学生姓名不能为空");
        }
        student.setId(id);
        return Result.success("学生 " + id + " 信息更新成功", student);
    }
    
    @DeleteMapping("/delete/{id}")
    public Result deleteStudent(@PathVariable("id") int id) {
        // 模拟删除学生逻辑
        if (id <= 0) {
            return Result.error("无效的学生ID");
        }
        return Result.success("学生 " + id + " 删除成功");
    }
}
