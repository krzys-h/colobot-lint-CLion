package pl.krzysh.clion.colobotlint;

import com.intellij.psi.PsiFile;
import com.jetbrains.cidr.lang.OCLanguageKind;
import com.jetbrains.cidr.lang.psi.impl.OCFileImpl;

public class LanguageTypeUtil {
    public static boolean isCFamily(PsiFile file) {
        if (!(file instanceof OCFileImpl)) {
            return false;
        }

        OCLanguageKind ocLanguageKind = ((OCFileImpl) file).getKind();

        return ocLanguageKind.isCpp();
    }
}
