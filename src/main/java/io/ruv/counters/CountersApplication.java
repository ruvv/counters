package io.ruv.counters;

import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.DispatcherServlet;

@SpringBootApplication
public class CountersApplication implements ApplicationContextAware {

    public static void main(String[] args) {

        SpringApplication.run(CountersApplication.class, args);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {

        val dispatcherServlet = (DispatcherServlet) applicationContext.getBean("dispatcherServlet");

        // throw exception instead of running default 404 handler
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
    }
}
