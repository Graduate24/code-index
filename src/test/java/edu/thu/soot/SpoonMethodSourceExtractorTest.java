package edu.thu.soot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class SpoonMethodSourceExtractorTest {
    private SpoonMethodSourceExtractor extractor;
    private String testSourcePath;

    @TempDir
    Path tempOutputDir;

    @BeforeEach
    void setUp() throws IOException {
        // 创建临时测试目录
        testSourcePath = Files.createTempDirectory("spoon-test").toString();

        // 创建测试类文件
        createTestClass();

        // 初始化提取器
        extractor = new SpoonMethodSourceExtractor(testSourcePath);
    }

    private void createTestClass() throws IOException {
        // 创建包目录
        Path packagePath = Paths.get(testSourcePath, "com", "example");
        Files.createDirectories(packagePath);

        // 创建测试类文件
        String sourceCode = "package com.example;\n\n" +
                "import java.util.Map;\n\n" +
                "public class TestClass {\n" +
                "    /**\n" +
                "     * 测试方法\n" +
                "     * @param name 参数名\n" +
                "     * @return 返回结果\n" +
                "     */\n" +
                "    public Map<String, String> testMethod(String name) {\n" +
                "        return Map.of(\"result\", \"Hello, \" + name);\n" +
                "    }\n\n" +
                "    public int add(int a, int b) {\n" +
                "        return a + b;\n" +
                "    }\n" +
                "}";

        Files.write(Paths.get(packagePath.toString(), "TestClass.java"), sourceCode.getBytes());
    }

    @Test
    void testGetMethodSource() {
        // 测试获取testMethod方法的源码
        String methodSource = extractor.getMethodSource("com.example.TestClass: Map testMethod(java.lang.String)");
        assertNotNull(methodSource);

        // 打印方法源码以便调试
        System.out.println("Method source:");
        System.out.println(methodSource);

        // 检查方法签名
        assertTrue(methodSource.contains("testMethod"));
        assertTrue(methodSource.contains("String name"));
        assertTrue(methodSource.contains("return Map.of"));

        // 测试获取add方法的源码
        methodSource = extractor.getMethodSource("com.example.TestClass: int add(int, int)");
        assertNotNull(methodSource);

        // 打印方法源码以便调试
        System.out.println("Add method source:");
        System.out.println(methodSource);

        // 检查方法签名
        assertTrue(methodSource.contains("add"));
        assertTrue(methodSource.contains("int a"));
        assertTrue(methodSource.contains("int b"));
        assertTrue(methodSource.contains("return a + b"));
    }

    @Test
    void testInvalidMethodSignature() {
        // 测试无效的方法签名
        assertThrows(IllegalArgumentException.class, () -> {
            extractor.getMethodSource("invalid-signature");
        });

        // 测试不存在的方法
        assertThrows(IllegalArgumentException.class, () -> {
            extractor.getMethodSource("com.example.TestClass: void nonExistentMethod()");
        });
    }

    @Test
    void testSootStyleSignature() {
        // 测试Soot风格的方法签名
        String methodSource = extractor.getMethodSource("<com.example.TestClass: java.util.Map testMethod(java.lang.String)>");
        assertNotNull(methodSource);
        assertTrue(methodSource.contains("testMethod"));
        assertTrue(methodSource.contains("String name"));
    }

    @Test
    void testWriteMethodSourceToFile() throws IOException {
        // 创建输出文件路径
        Path outputFile = tempOutputDir.resolve("testMethod.java");

        // 将方法源码写入文件
        extractor.writeMethodSourceToFile("<com.example.TestClass: Map testMethod(java.lang.String)>", outputFile.toString());

        // 检查文件是否存在
        assertTrue(Files.exists(outputFile));

        // 读取文件内容
        String fileContent = Files.readString(outputFile);

        // 检查文件内容是否包含方法源码
        assertTrue(fileContent.contains("testMethod"));
        assertTrue(fileContent.contains("String name"));
        assertTrue(fileContent.contains("return Map.of"));
    }

    @Test
    void testWriteAllMethodSourcesToDirectory() throws IOException {
        // 将所有方法源码写入目录
        int count = extractor.writeAllMethodSourcesToDirectory("com.example.TestClass", tempOutputDir.toString());

        // 应该有两个方法被写入
        assertEquals(2, count);

        // 检查文件是否存在
        Path testMethodFile = tempOutputDir.resolve("com.example.TestClass_testMethod.java");
        Path addMethodFile = tempOutputDir.resolve("com.example.TestClass_add.java");

        assertTrue(Files.exists(testMethodFile));
        assertTrue(Files.exists(addMethodFile));

        // 检查文件内容
        String testMethodContent = Files.readString(testMethodFile);
        String addMethodContent = Files.readString(addMethodFile);

        assertTrue(testMethodContent.contains("testMethod"));
        assertTrue(addMethodContent.contains("add(int a, int b)"));
    }
}


