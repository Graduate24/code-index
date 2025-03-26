# Soot代码分析工具

这是一个基于Soot的Java代码分析工具，可以生成调用图、Jimple IR、代码索引和控制流图。

## 功能特性

- 生成调用图（Call Graph）
- 生成Jimple IR
- 创建代码索引（字段、方法的定义和引用）
- 生成控制流图（Control Flow Graph）
- 执行指针分析（Points-to Analysis）

## 依赖要求

- Java 8+
- Maven 3.6+

## 构建

```bash
mvn clean package
```

## 使用方法

### 基本用法

```bash
java -jar target/soot-analyzer-1.0-SNAPSHOT.jar [选项]
```

### 命令行选项

| 选项 | 长选项 | 描述 | 是否必需 |
|------|--------|------|----------|
| -t | --target | 目标Java项目路径（编译后的class文件目录） | 是 |
| -o | --output | 输出目录 | 是 |
| -c | --callgraph | 生成调用图 | 否 |
| -j | --jimple | 生成Jimple IR | 否 |
| -i | --index | 生成代码索引 | 否 |
| -p | --points-to | 执行指针分析 | 否 |
| -cfg | --control-flow-graph | 生成控制流图 | 否 |
| -class | --target-class | 目标类名（用于控制流图生成） | 否 |
| -method | --target-method | 目标方法名（用于控制流图生成） | 否 |
| -h | --help | 显示帮助信息 | 否 |

### 示例

1. 生成调用图：
```bash
java -jar target/soot-analyzer-1.0-SNAPSHOT.jar -t target/classes -o output -c
```

2. 生成Jimple IR：
```bash
java -jar target/soot-analyzer-1.0-SNAPSHOT.jar -t target/classes -o output -j
```

3. 生成代码索引：
```bash
java -jar target/soot-analyzer-1.0-SNAPSHOT.jar -t target/classes -o output -i
```

4. 执行指针分析：
```bash
java -jar target/soot-analyzer-1.0-SNAPSHOT.jar -t target/classes -o output -p
```

5. 生成控制流图：
```bash
java -jar target/soot-analyzer-1.0-SNAPSHOT.jar -t target/classes -o output -cfg -class edu.thu.example.MyClass -method myMethod
```

6. 组合使用多个功能：
```bash
java -jar target/soot-analyzer-1.0-SNAPSHOT.jar -t target/classes -o output -c -j -i -p -cfg -class edu.thu.example.MyClass -method myMethod
```

### 输出说明

- 调用图：保存在 `output/call_graph.json`
- Jimple IR：保存在 `output/jimple/` 目录下
- 代码索引：保存在 `output/index/` 目录下
  - `method_definitions.json`：方法定义索引
  - `method_invocations.json`：方法调用索引
  - `field_definitions.json`：字段定义索引
  - `field_references.json`：字段引用索引
- 指针分析结果：保存在 `output/points_to_analysis.json`
- 控制流图：保存在 `output/cfg/` 目录下，格式为DOT文件

## 注意事项

1. 确保目标路径包含编译后的class文件
2. 生成控制流图时必须指定目标类名和方法名
3. 指针分析需要较长时间，请耐心等待
4. 建议使用足够大的内存运行，可以通过 `-Xmx` 参数设置

## 许可证

MIT License 
