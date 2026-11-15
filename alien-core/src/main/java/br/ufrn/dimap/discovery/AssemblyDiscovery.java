package br.ufrn.dimap.discovery;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class AssemblyDiscovery {
    private static final Set<String> basePackages = ConcurrentHashMap.newKeySet();

    private AssemblyDiscovery() {}

    public static void registerPackage(String basePackage) {
        if (basePackage != null && !basePackage.isBlank()) {
            basePackages.add(basePackage.trim());
        }
    }

    public static void registerPackage(Class<?> anchorClass) {
        if (anchorClass != null) {
            registerPackage(anchorClass.getPackageName());
        }
    }

    public static void clearPackages() {
        basePackages.clear();
    }

    public static List<Class<?>> getClasses() {
        Set<Class<?>> classes = new LinkedHashSet<>();
        for (String pkg : basePackages) {
            classes.addAll(getClasses(pkg));
        }
        return List.copyOf(classes);
    }

    public static List<Class<?>> getClasses(String basePackage) {
        List<Class<?>> classes = new ArrayList<>();
        String path = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    String decodedPath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
                    scanDirectory(new File(decodedPath), basePackage, classes, classLoader);
                } else if ("jar".equals(protocol)) {
                    scanJar(resource, path, classes, classLoader);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan package: " + basePackage, e);
        }

        return classes;
    }

    private static void scanDirectory(File dir, String pkg, List<Class<?>> classes, ClassLoader cl) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, pkg + "." + file.getName(), classes, cl);
            } else if (file.getName().endsWith(".class")) {
                String name = file.getName();
                String className = pkg + "." + name.substring(0, name.length() - 6);
                loadClass(className, cl, classes);
            }
        }
    }

    private static void scanJar(URL resource, String path, List<Class<?>> classes, ClassLoader cl) throws IOException {
        String prefix = path.endsWith("/") ? path : path + "/";
        JarURLConnection conn = (JarURLConnection) resource.openConnection();
        try (JarFile jar = conn.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(prefix) && name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    loadClass(className, cl, classes);
                }
            }
        }
    }

    private static void loadClass(String className, ClassLoader cl, List<Class<?>> classes) {
        try {
            classes.add(Class.forName(className, false, cl));
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
        }
    }
}
