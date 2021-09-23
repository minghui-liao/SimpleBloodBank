package logic;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class LogicFactory {

    private static final String PACKAGE = "logic.";
    private static final String SUFFIX = "Logic";

    private LogicFactory() {
    }

  
    public static < T> T getFor( String entityName ) {
        T newInstance = null;
        try{
           newInstance = getFor((Class<T>) Class.forName(PACKAGE + entityName + SUFFIX));
        }catch(ClassNotFoundException e){          
        
        }
        return newInstance;
    }
    
    public static <T> T getFor(Class<T> type){
        T newInstance = null;
        try{
            Constructor<T> declaredConstructor =type.getDeclaredConstructor();
            newInstance = declaredConstructor.newInstance();
        
        }catch(InstantiationException | IllegalAccessException | IllegalArgumentException |
        InvocationTargetException | NoSuchMethodException | SecurityException e){
            
        } 
        return newInstance;
    }

}
