package net.ibogner.intellij.gamification.comparisonchain;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import net.ibogner.intellij.gamification.Notifier;
import net.ibogner.intellij.gamification.award.AwardIcon;

import java.util.List;

public class GenerateAction extends AnAction {
    public static final String COM_GOOGLE_COMMON_COLLECT_COMPARISON_CHAIN = "com.google.common.collect.ComparisonChain";

    public GenerateAction() {
        super();
    }

    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        GenerateDialog dlg = new GenerateDialog(psiClass);
        dlg.show();
        if (dlg.isOK()) {
            generateComparable(psiClass, dlg.getFields());
        }

        Notifier.getInstance().displayNotification(DataKeys.PROJECT.getData(e.getDataContext()), AwardIcon.GENERAL, "Congratulations!", "You won an award...");
    }

    public void generateComparable(final PsiClass psiClass, final List<PsiField> fields) {
        new WriteCommandAction.Simple(psiClass.getProject(), psiClass.getContainingFile()) {

            @Override
            protected void run() throws Throwable {
                generateCompareTo(psiClass, fields);
                generateImplementsComparable(psiClass);
            }

        }.execute();
    }

    private void generateImplementsComparable(PsiClass psiClass) {
        PsiClassType[] implementsListTypes = psiClass.getImplementsListTypes();
        for (PsiClassType implementsListType : implementsListTypes) {
            PsiClass resolved = implementsListType.resolve();
            if (resolved != null && "java.lang.Comparable".equals(resolved.getQualifiedName())) {
                return;
            }
        }

        String implementsType = "Comparable<" + psiClass.getName() + ">";
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiJavaCodeReferenceElement referenceElement = elementFactory.createReferenceFromText(implementsType, psiClass);
        psiClass.getImplementsList().add(referenceElement);
    }

    private void generateCompareTo(PsiClass psiClass, List<PsiField> fields) {
        StringBuilder builder = new StringBuilder("public int compareTo(");
        builder.append(psiClass.getName()).append(" that) {\n");
        builder.append("return " + COM_GOOGLE_COMMON_COLLECT_COMPARISON_CHAIN + ".start()");
        for (PsiField field : fields) {
            builder.append(".compare(this.").append(field.getName()).append(", that.");
            builder.append(field.getName()).append(")");
        }
        builder.append(".result();\n}");
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiMethod compareTo = elementFactory.createMethodFromText(builder.toString(), psiClass);
        PsiElement method = psiClass.add(compareTo);
        JavaCodeStyleManager.getInstance(psiClass.getProject()).shortenClassReferences(method);

    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private PsiClass getPsiClassFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        PsiClass psiClass = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
        return psiClass;
    }
}