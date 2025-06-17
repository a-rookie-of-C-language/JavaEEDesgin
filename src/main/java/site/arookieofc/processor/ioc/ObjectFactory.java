package site.arookieofc.processor.ioc;

@FunctionalInterface
interface ObjectFactory<T> {
    T getObject();
}
