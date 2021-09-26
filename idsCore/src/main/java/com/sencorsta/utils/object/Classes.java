package com.sencorsta.utils.object;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.processor.MessageProcessor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 类反射调用工具类
 *
 * @author ICe
 */
public class Classes {

    public static Object[] getFields(Class<Object> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        // TODO ice Autowired判断加上
        return Arrays.stream(declaredFields).map(e -> {
            String s = MessageProcessor.getSERVICE_MAP().get(e.getType().getName());
            if (ObjectUtil.isNotNull(s)) {
                Class<Object> objectClass = cn.hutool.core.util.ClassUtil.loadClass(s);
                Object[] fields = getFields(objectClass);
                return Singleton.get(objectClass, fields);
            }
            return null;
        }).filter(Objects::nonNull).toArray();
    }


    public static Field getDeclaredField(Object object, String fieldName) {
        Field field = null;
        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
                return field;
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * 获取对象的属性
     */
    public static Object getProperty(Object owner, String fieldName) throws Exception {
        Field field = getDeclaredField(owner, fieldName);
        field.setAccessible(true);
        Object property = field.get(owner);
        return property;
    }

    /**
     * 赋值给对象的私有属性
     */
    public static void setProperty(Object owner, String fieldName, Object value) throws Exception {
        Field field = getDeclaredField(owner, fieldName);
        field.setAccessible(true);
        field.set(owner, value);
    }

    /**
     * 获取类的静态属性
     */
    public static Object getStaticProperty(Class<?> clz, String fieldName) throws Exception {
        Field field = clz.getField(fieldName);
        Object property = field.get(clz);
        return property;
    }

    /**
     * 执行对象的方法
     */
    public static Object invokeMethod(Object owner, String methodName, Object... args)
            throws Exception {
        Class<?> ownerClass = owner.getClass();
        Class<?>[] argsClass = new Class[args.length];
        for (int i = 0, j = args.length; i < j; i++) {
            argsClass[i] = args[i].getClass();
        }
        Method method = ownerClass.getMethod(methodName, argsClass);
        return method.invoke(owner, args);
    }

    /**
     * 执行类的静态方法
     */
    public static Object invokeStaticMethod(String className, String methodName, Object... args)
            throws Exception {
        Class<?> ownerClass = Class.forName(className);
        Class<?>[] argsClass = new Class[args.length];
        for (int i = 0, j = args.length; i < j; i++) {
            argsClass[i] = args[i].getClass();
        }
        Method method = ownerClass.getMethod(methodName, argsClass);
        return method.invoke(null, args);
    }

//	/**
//	 * 新建实例
//	 */
//	public static Object newInstance(String className, Object... args) throws Exception {
//		Class<?> newoneClass = Class.forName(className);
//		Class<?>[] argsClass = new Class[args.length];
//		for (int i = 0, j = args.length; i < j; i++) {
//			argsClass[i] = args[i].getClass();
//		}
//		Constructor<?> cons = newoneClass.getConstructor(argsClass);
//		return cons.newInstance(args);
//	}

    public static Object newInstance(String className) {
        Class<Object> clazz = ClassUtil.loadClass(className);
        Object[] objects = Classes.getFields(clazz);
        return Singleton.get(clazz, objects);
    }

    /**
     * @param pageName 包名
     * @return List<Class < ?>> 包下所有类
     * @Description: 根据包名获得该包以及子包下的所有类不查找jar包中的
     */
    public static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace(".", "/");
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        for (File directory : dirs) {
            classes.addAll(findClass(directory, packageName));
        }
        return classes;
    }

    private static List<Class<?>> findClass(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClass(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(
                        Class.forName(packageName + "." + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    public static String getClassName(String name) {
        if (name.indexOf(File.separator) >= 0) {
            name = name.substring(name.lastIndexOf(File.separator) + 1);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(Character.toUpperCase(name.charAt(0)));
        for (int i = 1; i < name.length(); ++i) {
            if (name.charAt(i) == '.') break;
            if (name.charAt(i) == '-') {
                builder.append("_");
                continue;
            }
            if (name.charAt(i) == '_') {
                if (name.charAt(i + 1) != '.') {
                    builder.append(Character.toUpperCase(name.charAt(i + 1)));
                    ++i;
                }
            } else {
                builder.append(name.charAt(i));
            }
        }
        return builder.toString();
    }

    public static String getFieldName(String name) {
        if (name.length() < 1) return null;
        if ("p_id".equals(name)) return "pid";
        name = name.trim();
        StringBuilder builder = new StringBuilder();
        int i = name.charAt(1) == '_' ? 2 : 0;
        builder.append(Character.toLowerCase(name.charAt(i)));
        for (i++; i < name.length(); ++i) {
            if (name.charAt(i) == ' ') {
                continue;
            }
            if (name.charAt(i) == '-') {
                continue;
            }
            if (name.charAt(i) == '_') {
                if (name.length() > i + 1) {
                    builder.append(Character.toUpperCase(name.charAt(++i)));
                }
            } else {
                builder.append(name.charAt(i));
            }
        }
        String field = builder.toString();
        if ("long".equals(field)) {
            field = "long_v";
        } else if ("goto".equals(field)) {
            field = "goto_v";
        } else if ("default".equals(field)) {
            field = "default_v";
        } else if ("new".equals(field)) {
            field = "new_v";
        } else if ("try".equals(field)) {
            field = "try_v";
        } else if ("final".equals(field)) {
            field = "final_v";
        }
        return field;
    }

}
