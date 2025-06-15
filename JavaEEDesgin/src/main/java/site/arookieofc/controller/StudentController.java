package site.arookieofc.controller;

import site.arookieofc.annotation.web.*;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.pojo.DO.Student;
import site.arookieofc.pojo.DO.Teacher;
import site.arookieofc.pojo.dto.PageResult;
import site.arookieofc.pojo.dto.Result;
import site.arookieofc.pojo.vo.StudentVO;
import site.arookieofc.pojo.vo.TeacherVO;
import site.arookieofc.service.ClazzService;
import site.arookieofc.service.StudentService;
import site.arookieofc.service.TeacherService;
import site.arookieofc.utils.ConversionUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Controller("/student")
@Component
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private ClazzService clazzService;

    @GetMapping("/page")
    public Result getStudentList(@RequestParam("page") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResult<Student> pageResult = studentService.getStudentsByPage(page, size);
        PageResult<StudentVO> voPageResult = ConversionUtils
                .toStudentVOPageResult(pageResult, teacherService, clazzService);
        return Result.success("获取学生列表成功", voPageResult);
    }

    @GetMapping("/info/{id}")
    public Result getStudentInfo(@PathVariable("id") String id) {
        Student student = studentService.getStudentById(id);
        StudentVO studentVO = ConversionUtils
                .toStudentVO(student, teacherService, clazzService);
        return Result.success("获取学生信息成功", studentVO);
    }

    @PostMapping("/add")
    public Result addStudent(@RequestBody StudentVO studentVO) {
        Student student = ConversionUtils.toStudentEntity(studentVO);
        studentService.addStudent(student);
        return Result.success("学生 " + student.getName() + " 添加成功");
    }

    @PutMapping("/update")
    public Result updateStudent(@RequestBody StudentVO studentVO) {
        Student student = ConversionUtils.toStudentEntity(studentVO);
        System.out.println(studentVO);
        System.out.println(student);
        studentService.updateStudent(student);
        StudentVO updatedVO = ConversionUtils
                .toStudentVO(student, teacherService, clazzService);
        return Result.success("学生信息更新成功", updatedVO);
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteStudent(@PathVariable("id") String id) {
        studentService.deleteStudent(id);
        return Result.success("学生 " + id + " 删除成功");
    }

    @GetMapping("/class/{clazz}")
    public Result getStudentsByClass(@PathVariable("clazz") String clazz) {
        clazz = URLDecoder.decode(clazz, StandardCharsets.UTF_8);
        List<Student> students = studentService.getStudentsByClass(clazz);
        List<StudentVO> studentVOs = ConversionUtils
                .toStudentVOList(students, teacherService, clazzService);
        return Result.success("获取班级学生成功", studentVOs);
    }

    @GetMapping("/teacher/{teacherId}")
    public Result getStudentsByTeacher(@PathVariable("teacherId") String teacherId) {
        List<Student> students = studentService.getStudentsByTeacherId(teacherId);
        List<StudentVO> studentVOs = ConversionUtils
                .toStudentVOList(students, teacherService, clazzService);
        return Result.success("获取教师学生成功", studentVOs);
    }

    @RequestMapping("/list")
    public Result getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        List<StudentVO> studentVOs = ConversionUtils
                .toStudentVOList(students, teacherService, clazzService);
        return Result.success(studentVOs);
    }

    @GetMapping("/teachers")
    public Result getAllTeachers() {
        List<Teacher> teachers = teacherService.getAllTeachers();
        List<TeacherVO> teacherVOs = ConversionUtils.toTeacherVOList(teachers);
        return Result.success("获取教师列表成功", teacherVOs);
    }

    @GetMapping("/classes")
    public Result getAllClasses() {
        List<String> classes = teacherService.getAllClassNames();
        return Result.success("获取班级列表成功", classes);
    }
}
