package nextstep.di.factory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BeanFactory {
    private static final Logger logger = LoggerFactory.getLogger(BeanFactory.class);

    private Map<Class<?>, Object> beans;
    private Set<Class<?>> preInstanticateClazz;

    public BeanFactory() {
        beans = Maps.newHashMap();
        preInstanticateClazz = Sets.newHashSet();
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

        Constructor<?> injectedConstructor = BeanFactoryUtils.getInjectedConstructor(preInstanticateBean);

        if (Objects.isNull(injectedConstructor)) {
            Object instance = BeanUtils.instantiateClass(preInstanticateBean);
            beans.put(preInstanticateBean, instance);
            logger.debug("bean name : {}, instance : {}", preInstanticateBean, instance);
            return instance;
        }

        return putParameterizedObject(preInstanticateBean, injectedConstructor);
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

    public Map<Class<?>, Object> getAnnotatedWith(Class<? extends Annotation> annotation) {
        Map<Class<?>, Object> annotatedClass = Maps.newHashMap();
        for (Class<?> clazz : beans.keySet()) {
            if (clazz.isAnnotationPresent(annotation)) {
                annotatedClass.put(clazz, beans.get(clazz));
            }
        }
        return annotatedClass;
    }

    public void addPreInstanticateClazz(Set<Class<?>> beans) {
        preInstanticateClazz.addAll(beans);
    }
}

