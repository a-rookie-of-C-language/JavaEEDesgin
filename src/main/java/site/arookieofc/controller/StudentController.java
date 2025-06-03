package site.arookieofc.controller;

import site.arookieofc.annotation.web.*;
import site.arookieofc.annotation.ioc.Autowired;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.entity.Teacher;
import site.arookieofc.pojo.dto.PageResult;
import site.arookieofc.service.TeacherService;
import site.arookieofc.pojo.dto.Result;
import site.arookieofc.pojo.dto.StudentDTO;
import site.arookieofc.entity.Student;
import site.arookieofc.service.StudentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller("/student")
@Component
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/page")
    public Result getStudentList(@RequestParam("page") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResult<Student> pageResult = studentService.getStudentsByPage(page, size);
        List<StudentDTO> studentDTOs = pageResult.getData().stream()
                .map(student -> {
                    StudentDTO dto = student.toDTO();
                    if (student.getTeacherId() != null) {
                        Optional<Teacher> teacher = teacherService.getTeacherById(student.getTeacherId());
                        teacher.ifPresent(t -> dto.setTeacherName(t.getName()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        PageResult<StudentDTO> dtoPageResult = new PageResult<>(
                studentDTOs,
                pageResult.getTotal(),
                pageResult.getPage(),
                pageResult.getSize()
        );

        return Result.success("获取学生列表成功", dtoPageResult);
    }

    @GetMapping("/info/{id}")
    public Result getStudentInfo(@PathVariable("id") String id) {
        try {
            Optional<Student> studentOpt = studentService.getStudentById(id);
            if (studentOpt.isPresent()) {
                StudentDTO studentDTO = studentOpt.get().toDTO();
                return Result.success("获取学生信息成功", studentDTO);
            } else {
                return Result.error("学生不存在");
            }
        } catch (Exception e) {
            return Result.error("获取学生信息失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result addStudent(@RequestBody StudentDTO studentDTO) {
        try {
            // 验证数据
            if (studentDTO.isValid()) {
                return Result.error(studentDTO.getValidationError());
            }

            Student student = studentDTO.toEntity();
            studentService.addStudent(student);
            return Result.success("学生 " + student.getName() + " 添加成功");
        } catch (Exception e) {
            return Result.error("添加学生失败: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public Result updateStudent(@PathVariable("id") String id, @RequestBody StudentDTO studentDTO) {
        try {
            // 验证数据
            if (studentDTO.isValid()) {
                return Result.error(studentDTO.getValidationError());
            }

            Student student = studentDTO.toEntity();
            student.setId(id);
            studentService.updateStudent(student);
            return Result.success("学生 " + id + " 信息更新成功", student.toDTO());
        } catch (Exception e) {
            return Result.error("更新学生失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteStudent(@PathVariable("id") String id) {
        try {
            studentService.deleteStudent(id);
            return Result.success("学生 " + id + " 删除成功");
        } catch (Exception e) {
            return Result.error("删除学生失败: " + e.getMessage());
        }
    }

    @GetMapping("/class/{clazz}")
    public Result getStudentsByClass(@PathVariable("clazz") String clazz) {
        try {
            List<Student> students = studentService.getStudentsByClass(clazz);
            List<StudentDTO> studentDTOs = students.stream()
                    .map(Student::toDTO)
                    .collect(Collectors.toList());
            return Result.success("获取班级学生成功", studentDTOs);
        } catch (Exception e) {
            return Result.error("获取班级学生失败: " + e.getMessage());
        }
    }

    @GetMapping("/teacher/{teacherId}")
    public Result getStudentsByTeacher(@PathVariable("teacherId") String teacherId) {
        try {
            List<Student> students = studentService.getStudentsByTeacherId(teacherId);
            List<StudentDTO> studentDTOs = students.stream()
                    .map(Student::toDTO)
                    .collect(Collectors.toList());
            return Result.success("获取教师学生成功", studentDTOs);
        } catch (Exception e) {
            return Result.error("获取教师学生失败: " + e.getMessage());
        }
    }

    @RequestMapping("/list")
    public Result getAllStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            List<StudentDTO> studentDTOs = new ArrayList<>();
            for (Student student : students) {
                StudentDTO dto = StudentDTO.fromEntity(student);
                if (student.getTeacherId() != null) {
                    Optional<Teacher> teacher = teacherService.getTeacherById(student.getTeacherId());
                    teacher.ifPresent(value -> dto.setTeacherName(value.getName()));
                }
                studentDTOs.add(dto);
            }
            return Result.success(studentDTOs);
        } catch (Exception e) {
            return Result.error();
        }
    }

    @GetMapping("/teachers")
    public Result getAllTeachers() {
        try {
            List<Teacher> teachers = teacherService.getAllTeachers();
            return Result.success("获取教师列表成功", teachers);
        } catch (Exception e) {
            return Result.error("获取教师列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/classes")
    public Result getAllClasses() {
        try {
            List<String> classes = teacherService.getAllClassNames();
            return Result.success("获取班级列表成功", classes);
        } catch (Exception e) {
            return Result.error("获取班级列表失败: " + e.getMessage());
        }
    }
}
