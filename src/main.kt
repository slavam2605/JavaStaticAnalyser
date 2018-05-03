import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter

val log = PrintWriter(FileOutputStream("warnings.log"), true)
var fileName = ""

fun PrintWriter.tell(node: Node, message: String) {
    println("File: $fileName")
    val begin = node.begin.orElse(null)?.toString() ?: "???"
    val end = node.end.orElse(null)?.toString() ?: "???"
    println("At [$begin-$end]: $message")
    println()
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: java -jar [this].jar [project-root]")
        println("Example: java -jar analyser.jar C:\\kotlin")
        return
    }
    println("Running...")

    val startTime = System.currentTimeMillis()

    val analysers = listOf(
            TrueExpressionsAnalyser(),
            SuspiciousShiftPriorityAnalyser(),
            EquivalentIfBranchAnalyser()
    )

    File(args[0]).walkTopDown().filter { file ->
        file.name.endsWith(".java")
    }.forEach { file ->
        fileName = file.path
        val compilationUnit = try {
            JavaParser.parse(file)
        } catch (e: Exception) {
            println("Failed to parse: ${file.path}")
            return@forEach
        }
        compilationUnit.types
                .filterIsInstance<ClassOrInterfaceDeclaration>()
                .forEach { classDecl ->
                    classDecl.methods.forEach { method ->
                        for (analyser in analysers) {
                            method.body.ifPresent(analyser::visitStatement)
                        }
                    }
                }
    }
    log.close()
    println("Done in ${System.currentTimeMillis() - startTime} ms")
    println("Results are stored in warnings.log")
}