# 代码索引和源码提取工具

基于 Soot 和 Spoon 的代码分析与源码提取工具，用于分析 Java 项目并提取特定方法的源代码。

## 功能

1. **生成调用图**：创建项目中各方法之间的调用关系图。
2. **生成 Jimple IR**：生成 Soot 的中间表示代码。
3. **创建代码索引**：索引项目中的方法和字段定义与引用。
4. **提取方法源码**：根据方法签名提取对应的源代码。

## 安装

```bash
mvn clean package
```

打包后会在 `target` 目录生成 `code-index-1.0-SNAPSHOT.jar` 文件。

## 使用方法

### 提取方法源码

从源代码中提取指定方法的源代码：

```bash
java -jar target/code-index-1.0-SNAPSHOT.jar \
  -extract \
  -o output目录 \
  -s 源代码路径 \
  -m "<完全限定类名: 返回类型 方法名(参数类型)>"
```

例如：

```bash
java -jar target/code-index-1.0-SNAPSHOT.jar \
  -extract \
  -o ./output \
  -s /path/to/source/code \
  -m "<edu.thu.benchmark.annotated.controller.XmlController: java.util.Map processXml(java.lang.String)>"
```

提取的源码将保存在 `output目录/method_source/` 下。

### 代码分析

执行完整的代码分析（需要字节码）：

```bash
java -jar target/code-index-1.0-SNAPSHOT.jar \
  -t 字节码路径 \
  -o 输出目录 \
  [其他选项]
```

可用选项：
- `-c, --callgraph`：生成调用图
- `-j, --jimple`：生成 Jimple IR
- `-i, --index`：生成代码索引
- `-p, --points-to`：执行指针分析
- `-cfg, --control-flow-graph`：生成控制流图
- `-class, --target-class <arg>`：目标类名
- `-method, --target-method <arg>`：目标方法名

## 方法签名格式

工具支持多种方法签名格式：

1. **标准格式**：`类名: 返回类型 方法名(参数类型列表)`
   例如：`com.example.MyClass: void myMethod(java.lang.String, int)`

2. **Soot 格式**：`<类名: 返回类型 方法名(参数类型列表)>`
   例如：`<com.example.MyClass: void myMethod(java.lang.String, int)>`

工具会自动检测并支持这两种格式。

## 注意事项

- 提取源码时，只需要指定源码路径（`-s`），不需要字节码路径（`-t`）。
- 进行 Soot 分析时需要同时提供字节码路径（`-t`）和输出目录（`-o`）。
- 方法签名区分大小写，需要完全匹配。

## 许可证

MIT License 
