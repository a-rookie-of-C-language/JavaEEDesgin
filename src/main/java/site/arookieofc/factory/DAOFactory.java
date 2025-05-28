package site.arookieofc.factory;

import site.arookieofc.processor.DAOProxy;

public class DAOFactory {
    
    @SuppressWarnings("unchecked")
    public static <T> T getDAO(Class<T> daoInterface) {
        return DAOProxy.createProxy(daoInterface);
    }
}