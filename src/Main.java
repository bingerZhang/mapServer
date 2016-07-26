import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Security;
import java.util.*;

public class Main {

    static {
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Invalid arguments, exit!");
            System.exit(1);
        }
        String name = args[0];
        args = shift(args);
        com.launch.Main main = getMain(name);
        if (main == null) {
            System.out.println("Invalid name: " + name);
            System.exit(1);
        }
        try {
            main.main(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static com.launch.Main getMain(String name) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            return getService(loader, com.launch.Main.class, name);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static <T> T getService(ClassLoader loader, Class<T> service, String name) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        Properties services = getServices(loader, service);
        String className = services.getProperty(name);
        if (className == null) {
            return null;
        }
        return service.cast(Class.forName(className, true, loader).newInstance());
    }

    private static <T> Properties getServices(ClassLoader loader, Class<T> service) {
        String fullName = "META-INF/services/" + service.getName() + ".properties";
        Enumeration<URL> configs = null;
        try {
            if (loader == null) {
                configs = ClassLoader.getSystemResources(fullName);
            } else {
                configs = loader.getResources(fullName);
            }
		} catch (IOException e) {
            throw new ServiceConfigurationError(service.getName() + ": Error locating configuration files", e);
		}
        Properties properties = new Properties();
		while (configs.hasMoreElements()) {
            URL url = configs.nextElement();
            InputStream in = null;
            try {
                in = url.openStream();
                properties.load(in);
            } catch (IOException e) {
                throw new ServiceConfigurationError(service.getName() + ": Error reading configuration files", e);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    throw new ServiceConfigurationError(service.getName() + ": Error closing configuration files", e);
                }
            }
		}
        return properties;
    }

    private static <T> T[] shift(T[] values) {
        T[] newValues = (T[]) java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), values.length - 1);
        System.arraycopy(values, 1, newValues, 0, newValues.length);
        return newValues;
    }
}
