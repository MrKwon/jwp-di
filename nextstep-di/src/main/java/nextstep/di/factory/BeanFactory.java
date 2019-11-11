package nextstep.di.factory;

import com.google.common.collect.Maps;
import nextstep.di.scanner.BeanScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BeanFactory {
    private static final Logger logger = LoggerFactory.getLogger(BeanFactory.class);

    private Map<Class<?>, Object> beans = Maps.newHashMap();
    private Map<Object, Method> configBeans = Maps.newHashMap();
    private Set<Class<?>> preInstanticateClazz;

    public BeanFactory(Object... basePackage) {
        BeanScanner beanScanner = new BeanScanner(basePackage);
        preInstanticateClazz = beanScanner.getBeans();
        initialize();
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        return (T) beans.get(requiredType);
    }

    public void initialize() {
        for (Class<?> preInstanticateBean : preInstanticateClazz) {
            scanBean(preInstanticateBean);
        }
    }

    private Object scanBean(Class<?> preInstanticateBean) {
        if (beans.containsKey(preInstanticateBean)) {
            return beans.get(preInstanticateBean);
        }

        if (!configBeans.containsKey(preInstanticateBean)) {
            Constructor<?> injectedConstructor = BeanFactoryUtils.getInjectedConstructor(preInstanticateBean);

            if (Objects.isNull(injectedConstructor)) {
                Object instance = BeanUtils.instantiateClass(preInstanticateBean);
                beans.put(preInstanticateBean, instance);
                logger.debug("bean name : {}, instance : {}", preInstanticateBean, instance);
                return instance;
            }

            return putParameterizedObject(preInstanticateBean, injectedConstructor);
        } else {
            Method method = configBeans.get(preInstanticateBean);

            if (method.getParameterCount() == 0) {
                Object instance = method.invoke(null);
                beans.put(preInstanticateBean, instance);
                logger.debug("bean name : {}, instance : {}", preInstanticateBean, instance);
                return instance;
            }

            return putParameterizedMethodObject(preInstanticateBean, method);
        }
    }

    private Object putParameterizedObject(Class<?> preInstanticateBean, Constructor<?> constructor) {
        Object[] params = getConstructorParams(constructor);
        Object instance = BeanUtils.instantiateClass(constructor, params);
        beans.put(preInstanticateBean, instance);
        logger.debug("bean name : {}, instance : {}", preInstanticateBean, instance);
        return instance;
    }

    private Object[] getConstructorParams(Constructor<?> constructor) {
        Object[] params = new Object[constructor.getParameterCount()];
        for (int i = 0; i < params.length; i++) {
            Class<?> parameterType = constructor.getParameterTypes()[i];
            Class<?> concreteClass = BeanFactoryUtils.findConcreteClass(parameterType, preInstanticateClazz);
            params[i] = scanBean(concreteClass);
        }
        return params;
    }

    private Object putParameterizedMethodObject(Class<?> preInstanticateBean, Method method) {
        try {
            Object[] params = getMethodParams(method);
            Object instance = method.invoke(null, params);
            beans.put(preInstanticateBean, instance);
            logger.debug("bean name : {}, instance : {}", preInstanticateBean, instance);
            return instance;
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException("실행 불가능한 어플리케이션");
        }
    }

    private Object[] getMethodParams(Method method) {
        Object[] params = new Object[method.getParameterCount()];
        for (int i = 0; i < params.length; i++) {
            Class<?> parameterType = method.getParameterTypes()[i];
            params[i] = scanBean(parameterType);
        }
        return params;
    }

    public Map<Class<?>, Object> getAnnotatedWith(Class<? extends Annotation> annotation) {
        Map<Class<?>, Object> annotatedClass = Maps.newHashMap();
        for (Class<?> clazz : beans.keySet()) {
            if (clazz.isAnnotationPresent(annotation)) {
                annotatedClass.put(clazz, beans.get(clazz));
            }
        }
        return annotatedClass;
    }

    public void registerBean(Class<?> clazz) throws IllegalAccessException, InstantiationException { // clazz = ExampleConfig
        Map<Class<?>, Method> configBeans = BeanFactoryUtils.findConfigurationBean(clazz);
        Set<Class<?>> configurationClass = configBeans.keySet();
        preInstanticateClazz.addAll(configurationClass);
        for (Class<?> configBean : configurationClass) {
            this.configBeans.put(clazz.newInstance(), configBeans.get(configBean));
        }
        logger.debug("debug point");
    }
}

