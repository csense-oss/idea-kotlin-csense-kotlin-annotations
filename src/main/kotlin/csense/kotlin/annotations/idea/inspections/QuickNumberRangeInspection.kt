package csense.kotlin.annotations.idea.inspections

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.ProblemsHolder
import csense.kotlin.annotations.idea.Constants
import csense.kotlin.annotations.idea.bll.RangeParser
import csense.kotlin.annotations.idea.psi.resolveAllParameterAnnotations
import csense.kotlin.extensions.collections.getSafe
import org.jetbrains.kotlin.idea.debugger.sequence.psi.resolveType
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.types.typeUtil.isPrimitiveNumberType
import org.jetbrains.uast.UAnnotation


class QuickNumberRangeInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "NumberRangeInspector"
    }

    override fun getStaticDescription(): String? {
        return """
            
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String? {
        return "more desc ? "
    }

    override fun getShortName(): String {
        return "NumberRangeInspection"
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

    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean): KtVisitorVoid {
        return callExpressionVisitor { ourCall ->
            //we look at value arguments
            val haveAnyNumbers = ourCall.anyDescendantOfType<KtConstantExpression> { it.isNumberType() }
            if (!haveAnyNumbers) {
                return@callExpressionVisitor //we do not track fully.that would not be fast.
            }


            val resolvedFunction = ourCall.resolvePsi() ?: return@callExpressionVisitor
            val annotations: List<List<UAnnotation?>> = resolvedFunction.resolveAllParameterAnnotations()
            if (annotations.isEmpty()) {
                return@callExpressionVisitor
            }
            ourCall.analyzeUannotationValueArguments(annotations, holder)
        }
    }


    fun KtCallExpression.analyzeUannotationValueArguments(
            resolvedAnnotations: List<List<UAnnotation?>>,
            holder: ProblemsHolder
    ) {
        valueArguments.forEachIndexed { index: Int, ktValueArgument: KtValueArgument? ->
            val argAnnotations = resolvedAnnotations.getSafe(index)
            if (argAnnotations != null && argAnnotations.isNotEmpty() && ktValueArgument != null) {
                val typeRange: RangeParser<*>? = RangeParser.parse(argAnnotations)
                if (typeRange != null && !typeRange.validate(argAnnotations, ktValueArgument)) {
                    holder.registerProblem(
                            ktValueArgument,
                            typeRange.computeErrorMessage(argAnnotations, ktValueArgument)
                    )
                }
            }
        }
    }
}


fun KtConstantExpression.isNumberType(): Boolean {
    return this.resolveType().isPrimitiveNumberType()
}