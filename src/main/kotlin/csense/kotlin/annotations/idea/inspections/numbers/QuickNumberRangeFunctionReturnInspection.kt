package csense.kotlin.annotations.idea.inspections.numbers

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.idea.base.bll.kotlin.*
import csense.kotlin.annotations.idea.*
import csense.kotlin.annotations.idea.inspections.numbers.bll.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*


class QuickNumberRangeFunctionReturnInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "NumberRangeFunctionReturnInspection"
    }

    override fun getStaticDescription(): String {
        return """
            Validates that the (constant) returns of a given function obeys any limits / ranges
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String {
        return "Validates that the (constant) returns of a given function obeys any limits / ranges"
    }

    override fun getShortName(): String {
        return "NumberRangeFunctionReturnInspection"
    }

    override fun getGroupDisplayName(): String {
        return Constants.InspectionGroupName
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.ERROR
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): KtVisitorVoid = namedFunctionVisitor { function ->
        val hasAnnotation = RangeParser.parseKt(function.annotationEntries) != null
        if (!hasAnnotation) {
            return@namedFunctionVisitor
        }
        val returnExpressions = function.findAllReturnExpressions()

        val mayReturnNull = function.typeReference?.isNullableType() == true

        returnExpressions.validateConstantsFor(
            annotations = function.annotationEntries,
            mayResultBeNull = mayReturnNull,
            holder = holder
        )
    }
}

fun KtNamedFunction.findAllReturnExpressions(): List<KtExpression> {
    return findAllReturnExpressionsForBodyExpression() +
            findAllReturnExpressionsForBodyBlockExpression()
}

fun KtNamedFunction.findAllReturnExpressionsForBodyBlockExpression(): List<KtExpression> {
    return bodyBlockExpression?.findAllReturnExpressionsFor(this.name) ?: emptyList()
}

fun KtNamedFunction.findAllReturnExpressionsForBodyExpression(): List<KtExpression> {
    val bodyExpression = bodyExpression ?: return emptyList()
    val innerReturns = bodyExpression.findAllReturnExpressionsFor(this.name)
    return listOf(bodyExpression) + innerReturns
}

fun KtElement.findAllReturnExpressionsFor(
    functionName: String?
): List<KtExpression> =
    findAllReturnsFor(functionName)
        .mapNotNull { it.returnedExpression }

fun KtElement.findAllReturnsFor(
    functionName: String?
): List<KtReturnExpression> = collectDescendantsOfType<KtReturnExpression> {
    val namedLabel = it.getLabelName() ?: return@collectDescendantsOfType true
    namedLabel == functionName
}


