package info.kgeorgiy.ja.ilyk.implementor;
import info.kgeorgiy.java.advanced.implementor.*;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Class that generates an interface implementation.
 *
 * @see JarImpler
 */
public class Implementor implements JarImpler{

    /**
     *  Separator between lines in generated implementation.
     */
    private static final String NEW_LINE = System.lineSeparator();

    /**
     *  Suffix in name of generated implementation.
     */
    private static final String SUFF = "Impl";
    /**
     * Tabulation in generated implementation.
     */
    private static final String TAB = "    ";
    /**
     * Space separator in generated implementation.
     */
    private static final String SPACE = " ";

    /**
     * Generates and writes code of class implementing a given interface {@code token}
     * through the specified {@code BufferedWriter}.
     * <p>
     * The generated code consists of two parts: {@link #generateClassHeader(Class, BufferedWriter) header} and
     * {@link #generateClassBody(Class, BufferedWriter) body}.
     *
     * @param token {@link Class} specifies the interface to be implemented
     * @param writer {@link BufferedWriter} to which the generated class is written
     * @throws IOException if an error occurs during writing
     */

    private void generateClass(Class<?> token, BufferedWriter writer) throws IOException {
        generateClassHeader(token, writer);
        generateClassBody(token, writer);
    }

    /**
     * Generates and writes header of class-implementation through the specified {@code BufferedWriter}.
     * <p>
     * The header contains information about package of generated class.
     * Package name is same as the package name of {@code token}. If it is unnamed,
     * the package record is not generated.
     *
     * @param token {@link Class} specifies the interface to be implemented
     * @param writer {@link BufferedWriter} to which the generated class is written
     * @throws IOException if an error occurs during writing
     */

    private void generateClassHeader(Class<?> token, BufferedWriter writer) throws IOException {
        String packageName = token.getPackageName();
        if (!packageName.isEmpty()) {
            writer.write("package" + SPACE + packageName + ";" + NEW_LINE.repeat(2));
        }
    }

    /**
     * Generates and writes the code of class-implementation itself with all methods
     * through the specified {@code BufferedWriter}.
     * <p>
     * Generated code contains a record that the class is public
     * and implements the interface specified by the {@code token}. Next, all the methods declared in the interface
     * generated by the {@link #generateMethod(Method, BufferedWriter)} recorded.
     *
     * @param token {@link Class} specifies the interface to be implemented
     * @param writer {@link BufferedWriter} to which the generated class is written
     * @throws IOException if an error occurs during writing
     */

    private void generateClassBody(Class<?> token, BufferedWriter writer) throws IOException {
        writer.write("public class" + SPACE + token.getSimpleName() + SUFF + SPACE + "implements"
                + SPACE + token.getCanonicalName() + SPACE + "{" + System.lineSeparator());
        for (Method m : token.getMethods()) {
            generateMethod(m, writer);
        }
        writer.write("}");
    }

    /**
     * Generates and writes code of a method through the specified {@code BufferedWriter}.
     * <p>
     * The generated method is non-abstract. Its parameters, return type and thrown exceptions are the same as the
     * {@code method} and defined by {@link #getParameters(Parameter[])}, {@link #getExceptions(Class[])} and
     * {@link}.
     *
     * @param method whose implementation is generated
     * @param writer {@link BufferedWriter} to which the generated class is written
     * @throws IOException if an error occurs during writing
     */

    private void generateMethod(Method method, BufferedWriter writer) throws IOException {
        writer.write(TAB + "public" + SPACE + method.getReturnType().getCanonicalName()
                + SPACE + method.getName() + SPACE + "(");
        String parameters = getParameters(method.getParameters());
        String exceptions = getExceptions(method.getExceptionTypes());
        writer.write(parameters + ")" + SPACE
                + exceptions +  "{" + NEW_LINE + TAB.repeat(2)
                + "return " + getDefaultValue(method.getReturnType()) + ";" + NEW_LINE + TAB + "}" + NEW_LINE);
    }

    /**
     * Generates a part of the method code describing the exceptions it throws as string.
     * <p>
     * Return empty string if {@code exceptions} is empty. Otherwise, it returns a string containing
     * the {@code throws} keyword and a list of thrown exceptions separated by a comma and a space.
     *
     * @param exceptions list of {@code Class} specifying thrown exceptions
     * @return {@code String} describing the thrown exceptions
     */
    private String getExceptions(Class<?>[] exceptions) {
        if (exceptions.length == 0) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        s.append(SPACE);
        s.append("throws");
        s.append(SPACE);
        for (Class<?> e : exceptions) {
            s.append(e.getCanonicalName());
            s.append(",");
            s.append(SPACE);
        }
        s.delete(s.length() - 2, s.length());
        return String.valueOf(s);
    }

    /**
     * Generates a string containing a list of parameter names.
     * <p>
     * Return empty string if {@code parameters} is empty.
     * Otherwise, return the parameters are separated by commas and space.
     *
     * @param parameters list of {@code Parametrs} specifying
     * @return string describing the parameters
     */

    private String getParameters(Parameter[] parameters) {
        StringBuilder s = new StringBuilder();
        for (Parameter p: parameters) {
            s.append(p.getType().getCanonicalName());
            s.append(SPACE);
            s.append(p.getName());
            s.append(",");
            s.append(SPACE);
        }
        if (!s.isEmpty()) {
            s.delete(s.length() - 2, s.length());
        }
        return String.valueOf(s);
    }

    /**
     * Generates default return value for {@code token} as string.
     * <p>
     * Return empty string if {@code token} is {@code void}, {@code false} for {@code boolean},
     * {@code 0} for other primitive types and {@code null} otherwise.
     *
     * @param token {@link Class} for which the default value is returned
     * @return string describing the default return value
     */

    private String getDefaultValue(Class<?> token) {
        if (token.equals(void.class)) {
            return "";
        }
        if (token == boolean.class) {
            return "false";
        }
        if (token.isPrimitive()) {
            return "0";
        }
        return "null";
    }

    /**
     * Returns the full path to implementation of {@code token} with the extension added.
     *
     * @param token {@link Class} specifies the interface to be implemented
     * @param extension string of extension for generate path
     * @return {@code Path} to implementation
     */
    private static Path getFullPathImplementation(Class<?> token, String extension) {
        return Path.of(token.getPackageName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + SUFF + extension);
    }


    /**
     * Compiles java class generated by {@link #implement(Class, Path)}.
     *
     * @param token {@link Class} specifies the interface to be implemented
     * @param root {@link Path} to directory of implementation
     * @throws ImplerException if compilation fails
     */
    private void compile(Class<?> token, Path root) throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Could not find java compiler.");
        }
        String classpath;
        try {
            classpath = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (URISyntaxException e) {
            throw new ImplerException("URISyntaxException", e);
        }
        if (compiler.run(null, null, null,
                root.resolve(getFullPathImplementation(token, ".java")).toString() , "-cp",
                root + File.pathSeparator + classpath, "-encoding", StandardCharsets.UTF_8.name()) != 0) {
            throw new ImplerException("Failed to compile generated class.");
        }
    }

    /**
     * Creates a jar-file from the compiled class.
     *
     * @param directory {@link Path} to directory of the compiled class
     * @param classFile full path to compiled class
     * @param jarFile {@link Path} to target jar file
     * @throws ImplerException if jar-file generation fails
     */

    private void generateJar(Path directory, Path classFile, Path jarFile) throws ImplerException {
        Manifest manifest = new Manifest();
        Attributes attr = manifest.getMainAttributes();
        attr.put(Attributes.Name.MANIFEST_VERSION, "1.0");

        try (JarOutputStream jarWriter = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            jarWriter.putNextEntry(new ZipEntry(classFile.toString().replace(File.separatorChar, '/')));
            Files.copy(directory.resolve(classFile), jarWriter);
        } catch (IOException e) {
            throw new ImplerException("Failed to write to jar file", e);
        }
    }


    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!token.isInterface() || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Incorrect class token.");
        }
        Path path = root.resolve(getFullPathImplementation(token, ".java"));
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            System.err.println("Got wrong path, " + e.getMessage());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            generateClass(token, writer);
        } catch (IOException e) {
            throw new ImplerException("Failed to writing output file", e);
        }
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path directory = jarFile;
        try {
            try {
                directory = Files.createTempDirectory(jarFile.getParent(), "temp_");
            } catch (IOException e) {
                System.err.println("Got wrong path, " + e.getMessage());
            }
            implement(token, directory);
            compile(token, directory);
            generateJar(directory, getFullPathImplementation(token, ".class"), jarFile);
        } finally {
            try {
                BaseImplementorTest.clean(directory);
            } catch (IOException e) {
                System.err.println("Failed to delete temporary directory" + e.getMessage());
            }
        }
    }

    /**
     * Provides console interface for {@link Implementor}. Calls {@link #implement(Class, Path)} or
     * {@link #implementJar(Class, Path)} based on number of arguments passed.
     * <p>
     * If the first argument is {@code "-jar"}, calls {@link #implementJar(Class, Path)},
     * else calls {@link #implement(Class, Path)}.
     * The next two arguments define the class whose implementation is generated
     * and the directory where it should be placed.
     * <p>
     * If the passed arguments o not match the description above, prints an error message.
     *
     * @param args {@link String[]} array of arguments.
     */

    public static void main(String[] args) {
        if (args == null || args[0] == null || args[1] == null || args.length != (args[0].equals("-jar") ? 3 : 2)) {
            System.err.println("Incorrect arguments: [-jar] <class name> <root> expected.");
            return;
        }
        JarImpler implementor = new Implementor();
        try {
            if (args[0].equals("-jar")) {
                implementor.implementJar(Class.forName(args[1]), Path.of(args[2]));
            } else {
                implementor.implement(Class.forName(args[0]), Path.of(args[1]));
            }
        } catch (ImplerException e) {
            System.err.println("Implementation error " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class doesn't exist " + e.getMessage());
        }
    }

}