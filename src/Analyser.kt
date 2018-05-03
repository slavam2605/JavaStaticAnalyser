import com.github.javaparser.ast.stmt.Statement

interface Analyser {
    fun visitStatement(stmt: Statement)
}