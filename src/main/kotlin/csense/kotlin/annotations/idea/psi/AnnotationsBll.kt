package csense.kotlin.annotations.idea.psi

import com.intellij.codeInsight.ExternalAnnotationsManager
import com.intellij.psi.*
import csense.kotlin.annotations.idea.bll.*
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.psi.*
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.toUElementOfType

fun PsiElement.resolveAllMethodAnnotations(externalAnnotationsManager: ExternalAnnotationsManager? = null): List<UAnnotation> {
    val extManager = externalAnnotationsManager ?: ExternalAnnotationsManager.getInstance(project)
    val ownAnnotations = when (this) {
        is KtLightMethod -> annotations.mapNotNull { it.toUElementOfType<UAnnotation>() }
        is KtFunction -> annotationEntries.mapNotNull { it.toUElementOfType<UAnnotation>() }
        is PsiMethod -> annotations.mapNotNull { it.toUElementOfType<UAnnotation>() }
        else -> emptyList()
    }
    val external = if (this is PsiModifierListOwner) {
        extManager.findExternalAnnotations(this)?.mapNotNull {
            it.toUElementOfType<UAnnotation>()
        } ?: emptyList()
    } else {
        emptyList()
    }
    return ownAnnotations + external
}

fun PsiElement.resolveAllClassAnnotations(externalAnnotationsManager: ExternalAnnotationsManager? = null): List<UAnnotation> {
    val extManager = externalAnnotationsManager ?: ExternalAnnotationsManager.getInstance(project)
    val internal = when (this) {
        is KtLightClass -> annotations.mapNotNull { it.toUElementOfType<UAnnotation>() }
        is KtClass -> annotationEntries.mapNotNull { it.toUElementOfType<UAnnotation>() }
        is KtClassOrObject -> annotationEntries.mapNotNull { it.toUElementOfType<UAnnotation>() }
        else -> emptyList()
    }
    val external = if (this is PsiModifierListOwner) {
        extManager.findExternalAnnotations(this)?.mapNotNull {
            it.toUElementOfType<UAnnotation>()
        } ?: emptyList()
    } else {
        emptyList()
    }
    return internal + external
}


fun PsiElement.resolveAllClassMppAnnotation(externalAnnotationsManager: ExternalAnnotationsManager? = null): List<MppAnnotation> {
    val extManager = externalAnnotationsManager ?: ExternalAnnotationsManager.getInstance(project)
    val internal = when (this) {
        is KtLightClass -> annotations.mapNotNull { it.toMppAnnotation() }
        is KtClass -> annotationEntries.mapNotNull { it.toMppAnnotation() }
        is KtClassOrObject -> annotationEntries.mapNotNull { it.toMppAnnotation() }
        else -> emptyList()
    }
    val external = if (this is PsiModifierListOwner) {
        extManager.findExternalAnnotations(this)?.mapNotNull {
            it.toMppAnnotation()
        } ?: emptyList()
    } else {
        emptyList()
    }
    return internal + external
}

fun PsiElement.resolveAllMethodAnnotationMppAnnotation(externalAnnotationsManager: ExternalAnnotationsManager? = null): List<MppAnnotation> {
    val extManager = externalAnnotationsManager ?: ExternalAnnotationsManager.getInstance(project)
    val ownAnnotations = this.getItemMppAnnotations()
    val external = (this as? PsiModifierListOwner)?.let {
        extManager.findExternalAnnotations(it)?.toMppAnnotations()
    } ?: emptyList()
    return ownAnnotations + external
}

fun PsiElement.resolveAllParameterAnnotations(externalAnnotationsManager: ExternalAnnotationsManager? = null): List<List<UAnnotation>> {
    val extManager = externalAnnotationsManager ?: ExternalAnnotationsManager.getInstance(project)
    return when (this) {
        is KtLightMethod -> parameterList.getAllAnnotations(extManager)
        is KtFunction -> valueParameters.getAllAnnotations(extManager)
        is PsiMethod -> parameterList.getAllAnnotations(extManager)
        else -> emptyList()
    }
}

fun List<KtParameter>.getAllAnnotations(
        extManager: ExternalAnnotationsManager
): List<List<UAnnotation>> = map {
    it.annotationEntries.toUAnnotation(extManager)
}

fun List<KtAnnotationEntry>.toUAnnotation(extManager: ExternalAnnotationsManager) = mapNotNull { it.toUElementOfType<UAnnotation>() }

fun PsiParameterList.getAllAnnotations(extManager: ExternalAnnotationsManager): List<List<UAnnotation>> {
    val internal = parameters.map {
        it.annotations.mapNotNull { it.toUElementOfType<UAnnotation>() }
    }
    val external = parameters.map {
        extManager.findExternalAnnotations(it)?.mapNotNull { it.toUElementOfType<UAnnotation>() }
                ?: emptyList()
    }
    return internal + external
}
