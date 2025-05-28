package site.arookieofc.controller;

import site.arookieofc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller("/student")
public class StudentController {
    
    @GetMapping("/list")
    public String getStudentList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return "Student List";
    }
    
    @GetMapping("/info")
    public String getStudentInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return "Student Info";
    }
    
    @PostMapping("/add")
    public String addStudent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return "Student Added";
    }
    
    @PutMapping("/update")
    public String updateStudent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return "Student Updated";
    }
    
    @DeleteMapping("/delete")
    public String deleteStudent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return "Student Deleted";
    }
}
