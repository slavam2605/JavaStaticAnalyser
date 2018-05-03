import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.IfStmt
import com.github.javaparser.ast.stmt.Statement

class SuspiciousShiftPriorityAnalyser: Analyser {
    fun suspiciousExpr(expr: Expression): Boolean {
        return expr !is LiteralExpr &&
               expr !is NameExpr &&
               expr !is EnclosedExpr &&
               expr !is UnaryExpr &&
               expr !is MethodCallExpr
    }

    fun analyseExpression(expr: Expression) {
        if (expr is BinaryExpr) {
            if (expr.operator == BinaryExpr.Operator.LEFT_SHIFT ||
                    expr.operator == BinaryExpr.Operator.SIGNED_RIGHT_SHIFT ||
                    expr.operator == BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT) {
                if (suspiciousExpr(expr.left) || suspiciousExpr(expr.right)) {
                    log.tell(expr, "possible missed parenthesis around ${expr.operator.asString()}")
                }
            }
            analyseExpression(expr.left)
            analyseExpression(expr.right)
        } else {
            expr.childNodes.filterIsInstance<Expression>().forEach(::analyseExpression)
        }
    }

    fun visitIfStmt(stmt: IfStmt) {
        analyseExpression(stmt.condition)
    }

    override fun visitStatement(stmt: Statement) {
        when (stmt) {
            is IfStmt -> visitIfStmt(stmt)
        }
        stmt.childNodes.filterIsInstance<Statement>().forEach(::visitStatement)
    }
}