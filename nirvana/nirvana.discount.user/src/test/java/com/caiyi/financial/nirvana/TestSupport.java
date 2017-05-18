package com.caiyi.financial.nirvana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * TestSupport : Spring测试支持,用于测试由Spring 管理的bean,编写测试类时,继承该类
 *
 * @author StarZou
 * @since 2014年5月18日 下午2:28:58
 */
@ContextConfiguration(locations = {"classpath*:spring-context.xml"})
public abstract class TestSupport extends AbstractTransactionalJUnit4SpringContextTests {

    public Logger logger = LoggerFactory.getLogger(getClass());


    protected long startTime;
    protected long endTime;

    /**
     * 记录 开始运行时间
     *
     * @return
     */
    protected long start() {
        this.startTime = System.currentTimeMillis();
        return startTime;
    }

    /**
     * 记录 结束运行时间
     *
     * @return
     */
    protected long end() {
        this.endTime = System.currentTimeMillis();
        this.log();
        return endTime;
    }

    /**
     * 输出记录
     */
    protected void log() {
        String text = "\n开始时间 : " + this.startTime + "\n结束时间 : " + this.endTime + "\n执行时间 : " + (this.endTime - this.startTime);
        logger.info(text);
    }
}