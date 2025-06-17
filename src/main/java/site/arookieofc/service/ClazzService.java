package site.arookieofc.service;

import site.arookieofc.pojo.DO.Clazz;
import java.util.List;

public interface ClazzService {

    List<Clazz> getAllClasses();

    Clazz getClassById(String id);

    Integer addClass(Clazz clazz);

    Boolean updateClass(Clazz clazz);

    Boolean deleteClass(String id);

    List<Clazz> getClassesByTeacher(String teacherId);

    void updateStudentCount(String classId, int increment);

    int getStudentCount(String classId);

    String getClassIdByName(String clazz);
}