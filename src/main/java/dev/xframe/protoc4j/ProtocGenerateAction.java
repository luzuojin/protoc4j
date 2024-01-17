package dev.xframe.protoc4j;

import com.intellij.history.core.Paths;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProtocGenerateAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(isProtoFile(PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext())));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataCtx = e.getDataContext();
        VirtualFile file = PlatformDataKeys.VIRTUAL_FILE.getData(dataCtx);
        if(isProtoFile(file)) {//current selected .proto file
            try {
                Module module = PlatformDataKeys.MODULE.getData(dataCtx);
                ProtocConfigState config = ProtocConfigState.getInstance();//get from data
                VirtualFile outDir = getOutputDir(module, config);
                List<VirtualFile> inDirs = getInputDirs(module, file);
                ProtocExecutor.ExecResp resp = ProtocExecutor.exec(config, inDirs.stream().map(VirtualFile::getPath).collect(Collectors.toList()), outDir.getPath(), file.getPath());
                if(resp.ok)
                    VfsUtil.markDirtyAndRefresh(true, true,true, outDir);
                Messages.showMessageDialog(e.getProject(), resp.msg, e.getPresentation().getText(), e.getPresentation().getIcon());
            } catch (Throwable ex) {
                Messages.showMessageDialog(e.getProject(), throwableToString(ex), e.getPresentation().getText(), e.getPresentation().getIcon());
            }
        }
    }

    private List<VirtualFile> getInputDirs(Module module, VirtualFile protoFile) {
        VirtualFile protoDir = protoFile.getParent();
        VirtualFile protoRoot = ProjectFileIndex.getInstance(module.getProject()).getContentRootForFile(protoFile);
        String protoRelative = Paths.relativeIfUnder(protoDir.getPath(), protoRoot.getPath());
        ModuleRootManager moduleRoot = ModuleRootManager.getInstance(module);
        List<VirtualFile> conentRoots = new ArrayList<>();
        Arrays.stream(moduleRoot.getContentRoots()).forEach(conentRoots::add);
        Arrays.stream(moduleRoot.getDependencies()).map(ModuleRootManager::getInstance).forEach(dependence -> {
            Arrays.stream(dependence.getContentRoots()).forEach(conentRoots::add);
        });
        return conentRoots.stream().map(f->f.findFileByRelativePath(protoRelative)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private VirtualFile getOutputDir(Module module, ProtocConfigState config) {
        return config.getPossibleOutDirs().stream()
                .map(path->this.getOutputDir(module, path))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(()->new IllegalStateException(String.format("Protoc out dir not found in module[%s]", module.getName())));
    }
    private VirtualFile getOutputDir(Module module, String possiblePath) {
        for (VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
            VirtualFile out = contentRoot.findFileByRelativePath(possiblePath);
            if(out != null) {
                return out;
            }
        }
        return LocalFileSystem.getInstance().findFileByPath(possiblePath);
    }

    private boolean isProtoFile(VirtualFile file) {
        return file != null && "proto".equals(file.getExtension());
    }

    private String throwableToString(Throwable e) {
        StringWriter w = new StringWriter();
        e.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

}
