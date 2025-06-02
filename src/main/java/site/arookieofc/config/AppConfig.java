package site.arookieofc.config;

import site.arookieofc.annotation.ioc.Bean;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.processor.transaction.TransactionInterceptor;
import site.arookieofc.service.*;
import site.arookieofc.service.impl.*;

@Component
public class AppConfig {
    
    @Bean
    public StudentService studentService() {
        return TransactionInterceptor.createProxy(new StudentServiceImpl());
    }
    
    @Bean
    public TeacherService teacherService() {
        return TransactionInterceptor.createProxy(new TeacherServiceImpl());
    }
    
    @Bean
    public ClazzService clazzService() {
        return TransactionInterceptor.createProxy(new ClazzServiceImpl());
    }
    
    @Bean
    public AiService aiService() {
        return new AiServiceImpl();
    }
}