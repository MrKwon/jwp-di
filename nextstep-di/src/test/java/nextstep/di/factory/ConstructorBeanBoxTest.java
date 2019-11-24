package nextstep.di.factory;

import nextstep.di.factory.example.JdbcQuestionRepository;
import nextstep.di.factory.example.MyQnaService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class ConstructorBeanBoxTest {

    private static final Logger log = LoggerFactory.getLogger(ConstructorBeanBoxTest.class);

    @Test
    void hasConstructorAndParams() {
        ConstructorBeanBox constructorBeanBox = new ConstructorBeanBox(MyQnaService.class);
        assertTrue(constructorBeanBox.hasParams());
    }

    @Test
    void hasNoConstructor() {
        ConstructorBeanBox constructorBeanBox = new ConstructorBeanBox(JdbcQuestionRepository.class);
        assertFalse(constructorBeanBox.hasParams());
    }

    @Test
    void getInvokerHasConstructorAndParams() {
        ConstructorBeanBox constructorBeanBox = new ConstructorBeanBox(MyQnaService.class);
        log.debug("Constructor : {}", constructorBeanBox.getInvoker());
        assertNotNull(constructorBeanBox.getInvoker());
    }

    @Test
    void getInvokerHasNoConstructor() {
        ConstructorBeanBox constructorBeanBox = new ConstructorBeanBox(JdbcQuestionRepository.class);
        log.debug("Constructor : {}", constructorBeanBox.getInvoker());
        assertNull(constructorBeanBox.getInvoker());
    }

    @Test
    void getParameterCountHasConstructorAndParams() {
        ConstructorBeanBox constructorBeanBox = new ConstructorBeanBox(MyQnaService.class);
        assertEquals(constructorBeanBox.getParameterCount(), 2);
    }
}