import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class Test2 {
    public static void main(String[] args) {
        // 示例Java代码
        String sourceCode =
                "class HelloWorld {" +
                        "    public static void main(String[] args) {" +
                        "        System.out.println(\"Hello World!\");" +
                        "    }" +
                        "}";

        try {
            // 解析代码生成AST
            CompilationUnit cu = StaticJavaParser.parse(sourceCode);

            // 创建访问者来遍历AST
            VoidVisitorAdapter<Void> visitor = new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodDeclaration md, Void arg) {
                    super.visit(md, arg);
                    System.out.println("方法名: " + md.getName());
                    System.out.println("返回类型: " + md.getType());
                    System.out.println("参数: " + md.getParameters());
                }
            };

            // 访问AST
            visitor.visit(cu, null);

            // 打印完整的AST结构
            System.out.println("\nAST结构:");
            System.out.println(cu.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
