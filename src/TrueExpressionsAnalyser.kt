import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.EnclosedExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.UnaryExpr
import com.github.javaparser.ast.stmt.IfStmt
import com.github.javaparser.ast.stmt.Statement
import com.sun.org.apache.xpath.internal.operations.Bool
import kotlin.math.exp

class TrueExpressionsAnalyser: Analyser {
    val trueExpressions: MutableList<Expression> = arrayListOf()

    fun deducableFrom(expr: Expression, trueExpressions: List<Expression>): Boolean {
        return trueExpressions.any { trueExpr ->
            fun findEvidence(currentExpr: Expression): Boolean {
                if (currentExpr == expr)
                    return true
                if (currentExpr is EnclosedExpr) {
                    if (findEvidence(currentExpr.inner))
                        return true
                }
                if (currentExpr is BinaryExpr && currentExpr.operator == BinaryExpr.Operator.AND) {
                    if (findEvidence(currentExpr.left))
                        return true
                    if (findEvidence(currentExpr.right))
                        return true
                }
                return false
            }
            findEvidence(trueExpr)
        }
    }

    fun refutableFrom(expr: Expression, trueExpressions: List<Expression>): Boolean {
        fun pushComplement(expr: Expression): Expression {
            if (expr is BinaryExpr && expr.operator == BinaryExpr.Operator.AND) {
                return BinaryExpr(pushComplement(expr.left), pushComplement(expr.right), BinaryExpr.Operator.OR)
            }
            if (expr is BinaryExpr && expr.operator == BinaryExpr.Operator.OR) {
                return BinaryExpr(pushComplement(expr.left), pushComplement(expr.right), BinaryExpr.Operator.AND)
            }
            return UnaryExpr(expr, UnaryExpr.Operator.LOGICAL_COMPLEMENT)
        }

        fun findEvidence(currentExpr: Expression): Boolean {
            if (currentExpr is UnaryExpr && currentExpr.operator == UnaryExpr.Operator.LOGICAL_COMPLEMENT && currentExpr.expression == expr)
                return true
            if (currentExpr is EnclosedExpr) {
                if (findEvidence(currentExpr.inner))
                    return true
            }
            val effectiveCurrentExpr = if (currentExpr is UnaryExpr && currentExpr.operator == UnaryExpr.Operator.LOGICAL_COMPLEMENT)
                pushComplement(currentExpr.expression)
            else currentExpr
            if (effectiveCurrentExpr is BinaryExpr && effectiveCurrentExpr.operator == BinaryExpr.Operator.AND) {
                if (findEvidence(effectiveCurrentExpr.left))
                    return true
                if (findEvidence(effectiveCurrentExpr.right))
                    return true
            }
            return false
        }

        return trueExpressions.any { trueExpr ->
            findEvidence(trueExpr)
        }
    }

    fun analyseExpression(expr: Expression) {
        if (deducableFrom(expr, trueExpressions)) {
            log.tell(expr, "expression '$expr' is always true")
            return
        }
        if (refutableFrom(expr, trueExpressions)) {
            log.tell(expr, "expression '$expr' is always false")
            return
        }
        if (expr is BinaryExpr) {
            analyseExpression(expr.left)
            when (expr.operator) {
                BinaryExpr.Operator.AND -> {
                    trueExpressions.add(expr.left)
                    analyseExpression(expr.right)
                    trueExpressions.removeAt(trueExpressions.lastIndex)
                }
                BinaryExpr.Operator.OR -> {
                    val notExpr = UnaryExpr(expr.left, UnaryExpr.Operator.LOGICAL_COMPLEMENT)
                    trueExpressions.add(notExpr)
                    analyseExpression(expr.right)
                    trueExpressions.removeAt(trueExpressions.lastIndex)
                }
                else -> {
                    analyseExpression(expr.right)
                }
            }
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