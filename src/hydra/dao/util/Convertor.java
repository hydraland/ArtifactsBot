package hydra.dao.util;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface Convertor {
	static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T convert(Class<T> aClass, Object obj) {
		try {
			if (obj == null) {
				return null;
			}
			T instance = aClass.getConstructor().newInstance();
			Method[] methods = aClass.getMethods();
			for (Method method : methods) {
				if (method.getName().startsWith("set")) {
					Class<?> parameter = method.getParameterTypes()[0];
					if (parameter.getTypeName().equals("java.lang.String") || parameter.getTypeName().equals("int")
							|| parameter.getTypeName().equals("boolean")) {
						Method getMethod = obj.getClass().getMethod(method.getName().replaceFirst("set", "get"));
						Object invokeValue = getMethod.invoke(obj);
						if (invokeValue instanceof Enum) {
							invokeValue = invokeValue.toString();
						}
						method.invoke(instance, invokeValue);
					} else if (parameter.getTypeName().equals("java.util.List")) {
						Type[] genericParameterTypes = method.getGenericParameterTypes();
						ParameterizedType parameterizedType = (ParameterizedType) genericParameterTypes[0];
						Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
						String typeGenericList = actualTypeArguments[0].getTypeName();
						List genericList = new ArrayList();
						Method getMethod = obj.getClass().getMethod(method.getName().replaceFirst("set", "get"));
						List aList = (List) getMethod.invoke(obj);
						for (Object value : aList) {
							if (typeGenericList.equals("java.lang.String")) {
								genericList.add(value);
							} else {
								// Object
								genericList.add(convert(Class.forName(typeGenericList), value));
							}
						}
						method.invoke(instance, genericList);

					} else if (parameter.getEnumConstants() != null) {
						Method getMethod = obj.getClass().getMethod(method.getName().replaceFirst("set", "get"));
						Object invokeValue = getMethod.invoke(obj);
						String value;
						if (invokeValue != null) {
							if (invokeValue instanceof String str) {
								value = str;
							} else {
								value = invokeValue.toString();
							}
							Optional<?> searchedEnum = Arrays.stream(parameter.getEnumConstants())
									.filter(aEnum -> aEnum.toString().equalsIgnoreCase(value)).findFirst();
							if (searchedEnum.isPresent()) {// si attribut n'est pas une chaine vide
								method.invoke(instance, searchedEnum.get());
							}
						}
					} else {
						Method getMethod = obj.getClass().getMethod(method.getName().replaceFirst("set", "get"));
						method.invoke(instance, convert(parameter, getMethod.invoke(obj)));
					}
				}
			}
			return instance;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Convertor error", e);
			return null;
		}
	}

}
