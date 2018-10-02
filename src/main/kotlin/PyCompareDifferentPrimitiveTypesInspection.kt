// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.impl.PyBuiltinCache

class PyCompareDifferentPrimitiveTypesInspection : PyInspection() {
  override fun getDisplayName(): String {
    return "Incompatible primitive types"
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return Visitor(holder, session)
  }

  companion object {
    private class Visitor(holder: ProblemsHolder, session: LocalInspectionToolSession) : PyInspectionVisitor(holder, session) {
      override fun visitPyBinaryExpression(node: PyBinaryExpression) {
        val lhs = node.leftExpression ?: return
        val rhs = node.rightExpression ?: return
        if (node.operator !in comparisonOperators) return
        if (!(lhs.isPrimitiveType() && rhs.isPrimitiveType())) return
        if (myTypeEvalContext.getType(lhs) != myTypeEvalContext.getType(rhs)) {
          registerProblem(node, "Incompatible primitive types")
        }
      }

      private fun PyExpression.isPrimitiveType(): Boolean {
        val type = myTypeEvalContext.getType(this) ?: return false
        val cache = PyBuiltinCache.getInstance(this)
        return cache.boolType == type || cache.intType == type || cache.floatType == type || cache.strType == type
      }
    }

    private val comparisonOperators = listOf(
      PyTokenTypes.LT, PyTokenTypes.GT, PyTokenTypes.LE, PyTokenTypes.GE,
      PyTokenTypes.EQEQ, PyTokenTypes.NE, PyTokenTypes.NE_OLD
    )
  }
}