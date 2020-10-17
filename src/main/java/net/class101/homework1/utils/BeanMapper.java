package net.class101.homework1.utils;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class BeanMapper {

    /**
     *
     * @param rs result set
     * @param obj
     * @param <T> class type
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws NoSuchFieldException
     */
    public <T> List<T> beanMap(ResultSet rs, T obj) throws SQLException, IllegalAccessException,
            InvocationTargetException, InstantiationException, NoSuchFieldException {
        List<T> returnList = new ArrayList<>();
        Constructor[] ctors = obj.getClass().getConstructors();
        Constructor ctor = null;

        for (int i = 0; i < ctors.length; i++) {
            ctor = ctors[i];
            if (ctor.getGenericParameterTypes().length == 0)
                break;
        }

        List<String > methods = Arrays.stream(obj.getClass().getFields())
                .map(method -> method.getName().toLowerCase()).collect(Collectors.toList());

        while(rs.next()) {
            T newT = (T) ctor.newInstance();
            ctor.setAccessible(true);
            for(String method : methods) {
                newT.getClass().getDeclaredField(method).set(newT, rs.getObject(method));
            }
            returnList.add(newT);
        }

        return returnList;
    }
}
