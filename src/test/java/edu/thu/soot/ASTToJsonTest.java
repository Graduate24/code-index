package edu.thu.soot;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ASTToJsonTest {
    public static void main(String[] args) {
        // 示例Java代码片段
        String codeSnippet =
                "for (String key : sortedKeys) {\n" +
                        "                List<String> pointsToInfo = itemsMap.get(key);\n" +
                        "                System.out.println(\"\\n\\t\" + key + \" 指向: \");\n" +
                        "                for (String info : pointsToInfo) {\n" +
                        "                    System.out.println(\"\\t\\t- \" + info);\n" +
                        "                }\n" +
                        "            }";

        // 将代码片段包装在完整的类和方法中
        String sourceCode =
            "class TestClass {\n" +
            "    public void testMethod() {\n" +
            codeSnippet +
            "    }\n" +
            "}";

        try {
            // 解析代码生成AST
            CompilationUnit cu = StaticJavaParser.parse(sourceCode);

            // 创建AST访问者
            SimpleASTVisitor visitor = new SimpleASTVisitor();
            List<Map<String, Object>> astJson = visitor.visit(cu);

            // 使用Gson美化输出JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(astJson);

            System.out.println("AST的JSON表示:");
            System.out.println(jsonString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class SimpleASTVisitor {
    public List<Map<String, Object>> visit(CompilationUnit cu) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Node node : cu.getChildNodes()) {
            if (node instanceof ClassOrInterfaceDeclaration) {
                result.add(visitClass((ClassOrInterfaceDeclaration) node));
            }
        }
        return result;
    }

    private Map<String, Object> visitClass(ClassOrInterfaceDeclaration classDecl) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "class_stmt");
        result.put("name", classDecl.getNameAsString());

        List<Map<String, Object>> methods = new ArrayList<>();
        for (Node node : classDecl.getMembers()) {
            if (node instanceof MethodDeclaration) {
                methods.add(visitMethod((MethodDeclaration) node));
            }
        }
        result.put("methods", methods);

        return result;
    }

    private Map<String, Object> visitMethod(MethodDeclaration methodDecl) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "func_stmt");
        result.put("name", methodDecl.getNameAsString());

        Map<String, Object> function = new HashMap<>();
        function.put("type", "function");

        // 处理参数
        List<String> parameters = new ArrayList<>();
        methodDecl.getParameters().forEach(param ->
            parameters.add(param.getNameAsString()));
        function.put("parameters", parameters);

        // 处理方法体
        if (methodDecl.getBody().isPresent()) {
            function.put("body", visitBlock(methodDecl.getBody().get()));
        }

        result.put("function", function);
        return result;
    }

    private Map<String, Object> visitBlock(BlockStmt block) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "block_stmt");

        List<Map<String, Object>> statements = new ArrayList<>();
        for (Statement stmt : block.getStatements()) {
            Map<String, Object> stmtJson = visitStatement(stmt);
            if (!stmtJson.isEmpty()) {
                statements.add(stmtJson);
            }
        }
        result.put("statements", statements);

        return result;
    }

    private Map<String, Object> visitStatement(Statement node) {
        Map<String, Object> result = new HashMap<>();

        if (node instanceof ExpressionStmt) {
            ExpressionStmt exprStmt = (ExpressionStmt) node;
            return visitExpression(exprStmt.getExpression());
        } else if (node instanceof ForEachStmt) {
            ForEachStmt forEach = (ForEachStmt) node;
            result.put("type", "for_each");
            result.put("variable", visitExpression(forEach.getVariable()));
            result.put("iterable", visitExpression(forEach.getIterable()));
            if (forEach.getBody() instanceof BlockStmt) {
                result.put("body", visitBlock((BlockStmt) forEach.getBody()));
            }
        }

        return result;
    }

    private Map<String, Object> visitExpression(Node node) {
        Map<String, Object> result = new HashMap<>();

        if (node instanceof NameExpr) {
            NameExpr nameExpr = (NameExpr) node;
            result.put("type", "variable");
            result.put("name", nameExpr.getNameAsString());
        } else if (node instanceof StringLiteralExpr) {
            StringLiteralExpr stringExpr = (StringLiteralExpr) node;
            result.put("type", "string");
            result.put("value", stringExpr.getValue());
        } else if (node instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr) node;
            result.put("type", "binary");
            result.put("operator", binaryExpr.getOperator().asString());
            result.put("left", visitExpression(binaryExpr.getLeft()));
            result.put("right", visitExpression(binaryExpr.getRight()));
        } else if (node instanceof FieldAccessExpr) {
            FieldAccessExpr fieldAccess = (FieldAccessExpr) node;
            result.put("type", "field_access");
            result.put("scope", visitExpression(fieldAccess.getScope()));
            result.put("name", fieldAccess.getNameAsString());
        } else if (node instanceof MethodCallExpr) {
            MethodCallExpr methodCall = (MethodCallExpr) node;
            result.put("type", "method_call");
            result.put("name", methodCall.getNameAsString());

            if (methodCall.getScope().isPresent()) {
                result.put("scope", visitExpression(methodCall.getScope().get()));
            }

            List<Map<String, Object>> arguments = new ArrayList<>();
            methodCall.getArguments().forEach(arg ->
                arguments.add(visitExpression(arg)));
            result.put("arguments", arguments);
        } else if (node instanceof VariableDeclarationExpr) {
            VariableDeclarationExpr varDecl = (VariableDeclarationExpr) node;
            result.put("type", "variable_declaration");
            result.put("variables", varDecl.getVariables().stream()
                .map(v -> {
                    Map<String, Object> var = new HashMap<>();
                    var.put("name", v.getNameAsString());
                    var.put("type", v.getTypeAsString());
                    return var;
                }).collect(Collectors.toList()));
        }

        return result;
    }
}
