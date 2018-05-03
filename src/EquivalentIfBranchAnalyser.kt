import com.github.javaparser.ast.stmt.IfStmt
import com.github.javaparser.ast.stmt.Statement

class EquivalentIfBranchAnalyser: Analyser {
    fun visitIfStmt(stmt: IfStmt) {
        if (stmt.elseStmt.isPresent) {
            if (stmt.thenStmt == stmt.elseStmt.get()) {
                log.tell(stmt, "Branches of 'if' statement are equivalent")
            }
        }
    }

    override fun visitStatement(stmt: Statement) {
        when (stmt) {
            is IfStmt -> visitIfStmt(stmt)
        }
        stmt.childNodes.filterIsInstance<Statement>().forEach(::visitStatement)
    }
}