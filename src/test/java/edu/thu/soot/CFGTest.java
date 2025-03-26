package edu.thu.soot;

import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.dot.DotGraph;
import soot.options.Options;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class CFGTest {
    public static void main(String[] args) {
        // 设置Soot选项
        Options.v().set_process_dir(new ArrayList<String>() {{
            add("/home/ran/Documents/work/graduate/annotated-benchmark/target/classes");  // 编译后的class文件目录
        }});
        Options.v().set_src_prec(Options.src_prec_class);  // 输入为class文件
        Options.v().set_output_format(Options.output_format_none);  // 不需要输出文件
        Options.v().set_whole_program(true);  // 全程序分析
        Options.v().set_allow_phantom_refs(true);  // 允许phantom引用

        // 添加要分析的类
        SootClass mainClass = Scene.v().loadClassAndSupport("edu.thu.benchmark.annotated.service.PathTraversalService");
        mainClass.setApplicationClass();  // 标记为应用类

        // 加载所有依赖的类
        Scene.v().loadNecessaryClasses();

        // 获取main方法
        SootMethod mainMethod = mainClass.getMethodByName("readFileUnsafe");

        // 创建控制流图
        Body body = mainMethod.retrieveActiveBody();
        UnitGraph cfg = new BriefUnitGraph(body);

        // 创建DOT图
        DotGraph dotGraph = new DotGraph("CFG");

        // 添加节点
        for (Unit unit : cfg) {
            String nodeName = unit.toString();
            dotGraph.drawNode(nodeName);

            // 添加边
            List<Unit> succs = cfg.getSuccsOf(unit);
            for (Unit succ : succs) {
                dotGraph.drawEdge(nodeName, succ.toString());
            }
        }

        // 保存DOT文件
        String outputPath = "cfg.dot";
        dotGraph.plot(outputPath);

        System.out.println("控制流图已生成到: " + outputPath);

        // 打印一些基本信息
        System.out.println("\n方法信息:");
        System.out.println("方法名: " + mainMethod.getName());
        System.out.println("参数数量: " + mainMethod.getParameterCount());
        System.out.println("返回类型: " + mainMethod.getReturnType());

        System.out.println("\n控制流图信息:");
        System.out.println("节点数量: " + cfg.size());
        System.out.println("边数量: " + countEdges(cfg));
    }

    private static int countEdges(UnitGraph cfg) {
        int edgeCount = 0;
        for (Unit unit : cfg) {
            edgeCount += cfg.getSuccsOf(unit).size();
        }
        return edgeCount;
    }
}
