// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.PyNumericLiteralExpression
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyStringLiteralExpression

class PySuspiciousEqualsInspection : PyInspection() {
  override fun getDisplayName(): String {
    return "Expression is always True"
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return Visitor(holder, session)
  }

  companion object {
    private class Visitor(holder: ProblemsHolder, session: LocalInspectionToolSession) : PyInspectionVisitor(holder, session) {
      override fun visitPyBinaryExpression(node: PyBinaryExpression) {
        val lhs = node.leftExpression ?: return
        val rhs = node.rightExpression ?: return
        if (node.operator != PyTokenTypes.EQEQ) return
        if (lhs is PyReferenceExpression && rhs is PyReferenceExpression) {
          if (lhs.name == rhs.name) {
            registerProblem(node, "Comparison with self")
            return
          }
        }
        if (lhs is PyStringLiteralExpression && rhs is PyStringLiteralExpression) {
          registerProblem(node, "Expression is always ${if (lhs.stringValue == rhs.stringValue) "True" else "False"}")
          return
        }
        if (lhs is PyNumericLiteralExpression && rhs is PyNumericLiteralExpression) {
          registerProblem(node, "Expression is always ${if (lhs.bigDecimalValue == rhs.bigDecimalValue) "True" else "False"}")
          return
        }
      }
    }
  }
}