package site.arookieofc.service.impl;

import site.arookieofc.dao.StudentDAO;
import site.arookieofc.entity.Student;
import site.arookieofc.factory.DAOFactory;
import site.arookieofc.service.StudentService;

import java.util.List;
import java.util.Optional;

public class StudentServiceImpl implements StudentService {
    
    private final StudentDAO studentDAO = DAOFactory.getDAO(StudentDAO.class);
    
    public Optional<Student> getStudentById(int id) {
        return studentDAO.getStudentById(id);
    }
    
    public void addStudent(Student student) {
        studentDAO.addStudent(student.getName(), student.getAge(), 
                            student.getTeacherId(), student.getClazz());
    }
    
    public void updateStudent(Student student) {
        studentDAO.updateStudent(student.getName(), student.getAge(), 
                               student.getTeacherId(), student.getClazz(), student.getId());
    }
    
    public void deleteStudent(int id) {
        studentDAO.deleteStudent(id);
    }
    
    public Optional<List<Student>> getAllStudents() {
        return studentDAO.getAllStudents();
    }
}
