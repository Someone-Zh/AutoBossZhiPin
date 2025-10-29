package com.someone.auto.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.someone.auto.Model.BeanTransform;

public class BeanHelper {

    public static Object setBean(Object bean, Map<String, Object> values) throws IllegalArgumentException, Exception {
        if (bean == null || values == null) {
            return bean;
        }
        Class<?> beanClass = bean.getClass();
        Field[] declaredFields = beanClass.getDeclaredFields();
        for (Field field : declaredFields) {
            String fieldName = field.getName();
            if (values.containsKey(fieldName)) {
                field.setAccessible(true);
                try {
                    field.set(bean, convertValue(values.get(fieldName), field));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to set field value:" + fieldName, e);
                }
            }
        }
        return bean;
    }

    /**
     * 转换字段值，特别处理枚举类型
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object convertValue(Object value, Field field) throws Exception {
        Class<?> fieldType = field.getType();
        
        // 如果是枚举类型，进行特殊处理
        if (fieldType.isEnum()) {
            return convertToEnum(value, (Class<? extends Enum>) fieldType, field);
        }
         // 处理List集合类型
        if (isListType(fieldType)) {
            return convertList(value, field);
        }
       
        return convertType(value, fieldType);
    }
    /**
     * 枚举转换逻辑
     */
    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E convertToEnum(Object value, Class<E> enumType, Field field) {
        if (value == null) {
            return null;
        }
        
        
        // 如果值本身就是枚举类型，直接返回
        if (enumType.isInstance(value)) {
            return (E) value;
        }
        Method[] methods = enumType.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(BeanTransform.class)) {
                // 检查方法是否是静态的且只有一个参数
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers()) && 
                    method.getParameterCount() == 1) {
                    // 获取参数类型
                    Class<?> paramType = method.getParameterTypes()[0];
                    // 尝试将值转换为方法参数类型
                    Object convertedValue = convertValue(value, paramType);
                    if (convertedValue != null) {
                        try {
                            E result = (E) method.invoke(null, convertedValue);
                            if (result != null) {
                                return result;
                            }
                        } catch (Exception e) {
                            // 如果当前方法失败，继续尝试其他方法
                            continue;
                        }
                    }
                }
            }
        }
        Field[] fields = enumType.getFields();
        for (Field eField : fields) {
            if (eField.isAnnotationPresent(BeanTransform.class)) {
                String matchField = eField.getName();
                // 按自定义属性匹配
                E result = findEnumByProperty(enumType, matchField, value);
                if (result != null) {
                    return result;
                }
            }
        }
        // 如果都匹配失败，抛出异常
        throw new IllegalArgumentException("无法将值 '" + value + "' 转换为枚举类型 " + enumType.getSimpleName());
    }
    /**
     * 判断是否为List类型
     */
    private static boolean isListType(Class<?> type) {
        return List.class.isAssignableFrom(type) || 
               Collection.class.isAssignableFrom(type);
    }
    
    /**
     * 转换List集合
     */
    private static Object convertList(Object value, Field field) throws Exception {
        if (value == null) {
            return null;
        }
        
        // 获取泛型类型
        Type genericType = field.getGenericType();
        Class<?> elementType = null;
        
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                elementType = (Class<?>) actualTypeArguments[0];
            }
        }
        
        if (elementType == null) {
            // 如果无法获取泛型类型，尝试从集合元素推断
            if (value instanceof Collection && !((Collection<?>) value).isEmpty()) {
                Object firstElement = ((Collection<?>) value).iterator().next();
                if (firstElement != null) {
                    elementType = firstElement.getClass();
                }
            }
            if (elementType == null) {
                return value; // 无法确定元素类型，返回原值
            }
        }
        
        // 如果值已经是List类型，转换其中的元素
        if (value instanceof Collection) {
            Collection<?> sourceCollection = (Collection<?>) value;
            List<Object> convertedList = new ArrayList<>();
            
            for (Object item : sourceCollection) {
                if (item == null) {
                    convertedList.add(null);
                } else if (elementType.isEnum()) {
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    Object convertedItem = convertToEnum(item, (Class<? extends Enum>) elementType, field);
                    convertedList.add(convertedItem);
                } else {
                    Object convertedItem = convertType(item, elementType);
                    convertedList.add(convertedItem);
                }
            }
            
            return convertedList;
        }
        
        // 如果值是数组，转换为List
        if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            List<Object> convertedList = new ArrayList<>();
            
            for (Object item : array) {
                if (item == null) {
                    convertedList.add(null);
                } else if (elementType.isEnum()) {
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    Object convertedItem = convertToEnum(item, (Class<? extends Enum>) elementType, field);
                    convertedList.add(convertedItem);
                } else {
                    Object convertedItem = convertType(item, elementType);
                    convertedList.add(convertedItem);
                }
            }
            
            return convertedList;
        }
        
        // 如果值是字符串，尝试解析为逗号分隔的列表
        if (value instanceof String) {
            String[] items = ((String) value).split(",");
            List<Object> convertedList = new ArrayList<>();
            
            for (String item : items) {
                String trimmedItem = item.trim();
                if (elementType.isEnum()) {
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    Object convertedItem = convertToEnum(trimmedItem, (Class<? extends Enum>) elementType, field);
                    convertedList.add(convertedItem);
                } else {
                    Object convertedItem = convertType(trimmedItem, elementType);
                    convertedList.add(convertedItem);
                }
            }
            
            return convertedList;
        }
        
        return value; // 无法处理的类型，返回原值
    }
    /**
     * 根据属性值查找枚举
     */
    private static <E extends Enum<E>> E findEnumByProperty(Class<E> enumType, String property, Object value) {
        try {
            E[] enumConstants = enumType.getEnumConstants();
            for (E enumConstant : enumConstants) {
                Field propertyField = enumType.getDeclaredField(property);
                propertyField.setAccessible(true);
                Object propertyValue = propertyField.get(enumConstant);
                
                if (Objects.equals(propertyValue.toString(), value.toString())) {
                    return enumConstant;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("查找枚举属性失败: " + e.getMessage(), e);
        }
        
        throw new IllegalArgumentException("未找到匹配属性值的枚举: " + value);
    }
    
    /**
     * 基础类型转换
     */
    private static Object convertType(Object value, Class<?> targetType) {
        if (value == null) return null;
        
        String valueStr = value.toString();
        
        if (targetType == String.class) {
            return valueStr;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(valueStr);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(valueStr);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(valueStr);
        } else if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(valueStr);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(valueStr);
        } else if (targetType == Short.class || targetType == short.class) {
            return Short.parseShort(valueStr);
        } else if (targetType == Byte.class || targetType == byte.class) {
            return Byte.parseByte(valueStr);
        }
        
        return value; // 无法转换时返回原值
    }

    /**
     * 值类型转换辅助方法
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        
        if (targetType.isInstance(value)) {
            return value;
        }
        
        String valueStr = value.toString();
        
        if (targetType == String.class) {
            return valueStr;
        } else if (targetType == Integer.class || targetType == int.class) {
            try {
                return Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (targetType == Long.class || targetType == long.class) {
            try {
                return Long.parseLong(valueStr);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (targetType == Double.class || targetType == double.class) {
            try {
                return Double.parseDouble(valueStr);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (targetType == Float.class || targetType == float.class) {
            try {
                return Float.parseFloat(valueStr);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(valueStr);
        }
        
        return null;
    }
}
