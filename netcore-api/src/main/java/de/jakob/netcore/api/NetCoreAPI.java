package de.jakob.netcore.api;

public class NetCoreAPI {

    private static NetCoreAPI instance;

    protected NetCoreAPI() {}

    public static void setInstance(NetCoreAPI apiImplementation) {
        if (instance != null) {
            throw new UnsupportedOperationException("NetCoreAPI cant be initialized twice!");
        }
        instance = apiImplementation;
    }

    public static NetCoreAPI get() {
        if (instance == null) {
            throw new IllegalStateException("NetCoreAPI has not been initialized!");
        }
        return instance;

    }

}