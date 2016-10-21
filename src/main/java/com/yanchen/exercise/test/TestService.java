package com.yanchen.exercise.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by yuyanchen on 16/10/20.
 */
@Service
public class TestService {
    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    @Resource
    private TestDao testDao;

    public int test() {
        logger.info("test");
        int i = testDao.selectActivityCount();
        return i;
    }
}
