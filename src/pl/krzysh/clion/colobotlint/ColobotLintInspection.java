package pl.krzysh.clion.colobotlint;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColobotLintInspection extends LocalInspectionTool {
    private static final Set<String> stableRules = new HashSet<String>();
    private static final Set<String> stableNoCBotRules = new HashSet<String>();
    static {
        stableRules.add("class naming");
        stableRules.add("code block placement");
        stableRules.add("compile error");
        stableRules.add("header file not self-contained");
        stableRules.add("license header");
        stableRules.add("old-style null pointer");
        stableRules.add("undefined function");
        stableRules.add("whitespace");

        stableNoCBotRules.add("class naming");
        stableNoCBotRules.add("code block placement");
        stableNoCBotRules.add("compile error");
        stableNoCBotRules.add("compile warning");
        stableNoCBotRules.add("header file not self-contained");
        stableNoCBotRules.add("include style");
        stableNoCBotRules.add("inconsistent declaration parameter name");
        stableNoCBotRules.add("license header");
        stableNoCBotRules.add("naked delete");
        stableNoCBotRules.add("naked new");
        stableNoCBotRules.add("old-style null pointer");
        stableNoCBotRules.add("undefined function");
        stableNoCBotRules.add("uninitialized field");
        stableNoCBotRules.add("unused forward declaration");
        stableNoCBotRules.add("whitespace");
    }

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if (!LanguageTypeUtil.isCFamily(file)) {
            return null;
        }

        List<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();

        Project project = file.getProject();
        String projectPath = project.getBasePath();
        String buildPath = Settings.get(Option.OPTION_KEY_BUILD_PATH);

        String filePath = file.getVirtualFile().getCanonicalPath();
        if (filePath == null) return null;
        String cppFilePath;
        String headerFilePath;
        if (filePath.endsWith(".cpp")) {
            cppFilePath = filePath;
            headerFilePath = buildPath + "/fake_header_sources" + filePath.replace(projectPath, "");
        } else if (filePath.endsWith(".h")) {
            cppFilePath = filePath.replace(".h", ".cpp");
            headerFilePath = buildPath + "/fake_header_sources" + filePath.replace(projectPath, "").replace(".h", ".cpp");
        } else {
            return null;
        }

        String result = "";
        try {
            result = ColobotLintCommand.execute(project,
                    "-verbose",
                    "-p", buildPath,
                    "-project-local-include-path", projectPath + "/src",
                    "-project-local-include-path", buildPath + "/src",
                    "-license-template-file", projectPath + "/LICENSE-HEADER.txt",
                    cppFilePath, headerFilePath
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Notifications.Bus.notify(new Notification("pl.krzysh.clion.colobotlint", "lint", result, NotificationType.INFORMATION));

        Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
        if (document == null) return null;

        Pattern pattern = Pattern.compile("\\[(.+)\\] \\[(.+)\\] (.+):([0-9]+) (.+)");
        for (String line : result.split("\n")) {
            Matcher match = pattern.matcher(line);
            if (match.find()) {
                String type = match.group(1);
                String rule = match.group(2);
                String filename = match.group(3);
                int linenum = Integer.valueOf(match.group(4));
                String text = match.group(5);

                if (!filename.equals(filePath)) continue;
                if (rule.equals("TODO comment")) continue;

                ProblemHighlightType highlightType = ProblemHighlightType.WEAK_WARNING;
                if (filename.contains("CBot")) {
                    if (stableRules.contains(rule)) highlightType = ProblemHighlightType.ERROR;
                } else {
                    if (stableNoCBotRules.contains(rule)) highlightType = ProblemHighlightType.ERROR;
                }
                if (type.equals("information")) highlightType = ProblemHighlightType.INFORMATION;

                ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
                        file,
                        TextRange.create(document.getLineStartOffset(linenum - 1), document.getLineEndOffset(linenum - 1)),
                        "[" + rule + "] " + text,
                        highlightType,
                        true);
                descriptors.add(problemDescriptor);
            }
        }

        return descriptors.toArray(new ProblemDescriptor[descriptors.size()]);
    }
}
