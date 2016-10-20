package com.yanchen.exercise.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by yuyanchen on 16/10/20.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/applicationContext.xml")
public class TestServiceTest {

    @Resource
    private TestService testService;

    @Test
    public void testTest1() throws Exception {
        testService.test();
    }
}