package edu.thu.soot;

import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

class SpoonMethodSourceExtractor {
    private final Launcher launcher;
    private final Factory factory;

    public SpoonMethodSourceExtractor(String sourcePath) {
        launcher = new Launcher();
        // 配置Spoon
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setLevel("OFF");
        launcher.getEnvironment().setComplianceLevel(11);

        // 添加源代码路径
        launcher.addInputResource(sourcePath);

        // 构建模型
        launcher.buildModel();
        factory = launcher.getFactory();

        // 打印加载的类信息
        List<CtMethod<?>> methods = factory.getModel().getRootPackage().getElements(new TypeFilter<>(CtMethod.class));
        System.out.println("Loaded methods:");
        for (CtMethod<?> method : methods) {
            System.out.println(method.getDeclaringType().getQualifiedName() + ": " +
                             method.getType().getSimpleName() + " " +
                             method.getSimpleName() + "(" +
                             getParameterTypes(method) + ")");
        }
    }

    private String getParameterTypes(CtMethod<?> method) {
        return method.getParameters().stream()
                .map(param -> param.getType().getSimpleName())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    public String getMethodSource(String methodSignature) {
        CtMethod<?> method = getMethod(methodSignature);
        return method.toString();
    }

    /**
     * 将方法源码写入指定文件
     *
     * @param methodSignature 方法签名
     * @param outputFilePath 输出文件路径
     * @return 是否成功写入
     * @throws IOException 如果写入文件时发生IO错误
     */
    public boolean writeMethodSourceToFile(String methodSignature, String outputFilePath) throws IOException {
        CtMethod<?> method = getMethod(methodSignature);
        if (method == null) {
            return false;
        }

        // 获取方法源码
        String methodSource = method.toString();

        // 创建父目录（如果不存在）
        Path filePath = Paths.get(outputFilePath);
        Files.createDirectories(filePath.getParent());

        // 写入文件
        Files.writeString(filePath, methodSource);
        System.out.println("方法源码已写入文件: " + outputFilePath);

        return true;
    }

    /**
     * 将类中所有方法的源码写入指定目录
     *
     * @param className 类名
     * @param outputDirPath 输出目录路径
     * @return 写入的方法数量
     * @throws IOException 如果写入文件时发生IO错误
     */
    public int writeAllMethodSourcesToDirectory(String className, String outputDirPath) throws IOException {
        // 获取类
        var type = factory.Type().get(className);
        if (type == null) {
            throw new IllegalArgumentException("Class not found: " + className);
        }

        // 创建输出目录
        Path dirPath = Paths.get(outputDirPath);
        Files.createDirectories(dirPath);

        // 获取所有方法并写入文件
        Set<CtMethod<?>> methods = type.getMethods();
        int count = 0;

        for (CtMethod<?> method : methods) {
            String methodName = method.getSimpleName();
            String fileName = className + "_" + methodName + ".java";
            Path filePath = dirPath.resolve(fileName);

            // 写入方法源码
            Files.writeString(filePath, method.toString());
            System.out.println("方法 " + methodName + " 源码已写入文件: " + filePath);

            count++;
        }

        return count;
    }

    public CtMethod<?> getMethod(String methodSignature) {
        // 支持Soot风格的签名：<className: returnType methodName(paramType1, paramType2, ...)>
        if (methodSignature.startsWith("<") && methodSignature.endsWith(">")) {
            methodSignature = methodSignature.substring(1, methodSignature.length() - 1);
        }

        // 解析方法签名
        // 格式: className: returnType methodName(paramType1, paramType2, ...)
        String[] parts = methodSignature.split(": ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid method signature format. Expected: className: returnType methodName(paramType1, paramType2, ...)");
        }

        String className = parts[0];
        String methodPart = parts[1];

        // 解析返回类型和方法名
        String[] methodParts = methodPart.split(" ", 2);
        if (methodParts.length != 2) {
            throw new IllegalArgumentException("Invalid method part format");
        }

        String returnType = methodParts[0];
        String methodName = methodParts[1].split("\\(")[0];

        // 获取简单类名（不含包名）
        String simpleReturnType = returnType;
        if (returnType.contains(".")) {
            simpleReturnType = returnType.substring(returnType.lastIndexOf('.') + 1);
        }

        // 获取类
        var type = factory.Type().get(className);
        if (type == null) {
            throw new IllegalArgumentException("Class not found: " + className);
        }

        // 获取方法声明
        CtMethod<?> method = null;
        for (CtMethod<?> m : type.getMethods()) {
            if (m.getSimpleName().equals(methodName) &&
                (m.getType().getSimpleName().equals(simpleReturnType) ||
                 m.getType().getQualifiedName().equals(returnType))) {
                method = m;
                break;
            }
        }

        if (method == null) {
            // 打印更多调试信息
            System.out.println("Available methods in class " + className + ":");
            Set<CtMethod<?>> methods = type.getMethods();
            for (CtMethod<?> m : methods) {
                System.out.println("- " + m.getType().getSimpleName() + " " +
                                 m.getSimpleName() + "(" +
                                 getParameterTypes(m) + ")");
                System.out.println("  Return type qualified: " + m.getType().getQualifiedName());
            }
            throw new IllegalArgumentException("Method not found: " + methodSignature);
        }

        return method;
    }
}
