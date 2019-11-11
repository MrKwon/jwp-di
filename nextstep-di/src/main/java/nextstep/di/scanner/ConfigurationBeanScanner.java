package nextstep.di.scanner;

import nextstep.di.factory.BeanFactory;

public class ConfigurationBeanScanner {
    private final BeanFactory beanFactory;

    public ConfigurationBeanScanner(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void register(Class<?> clazz) {
        beanFactory.registerBean(clazz);
    }
}
