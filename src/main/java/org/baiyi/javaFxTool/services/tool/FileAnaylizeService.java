package org.baiyi.javaFxTool.services.tool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.xwintop.xcore.util.javafx.TooltipUtil;
import javafx.geometry.Pos;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.baiyi.javaFxTool.controller.tool.FileAnaylizeController;
import org.baiyi.javaFxTool.services.business.AnaylizeFileRecv;
import org.baiyi.javaFxTool.services.business.AnaylizeFileSend;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName: PathWatchToolService
 * @Description: 文件夹监控工具
 * @author: xufeng
 * @date: 2019/4/27 0027 1:06
 */

@Getter
@Setter
@Slf4j
public class FileAnaylizeService {
    private FileAnaylizeController fileAnaylizeController;

    private AnaylizeFileSend anaylizeFileSend;

    private AnaylizeFileRecv anaylizeFileRecv;

    Thread thread = null;
    HashMap<String, String> map = new HashMap<>();

    public FileAnaylizeService(FileAnaylizeController fileAnaylizeController) {
        this.fileAnaylizeController = fileAnaylizeController;
//        this.anaylizeFileSend = ApplicationContextHolder.getApplicationContext().getBean(AnaylizeFileSend.class);
//        this.anaylizeFileRecv = ApplicationContextHolder.getApplicationContext().getBean(AnaylizeFileRecv.class);
    }

    public void watchAction() throws Exception {
        String watchPath = fileAnaylizeController.getWatchPathTextField().getText();
        Path path = Paths.get(watchPath);
        if (!Files.exists(path)) {
            TooltipUtil.showToast("文件不存在！");
            return;
        } else if (Files.isDirectory(path)) {
            TooltipUtil.showToast("只能解析文件！");
            return;
        }
        if (thread != null) {
            thread.stop();
        }
        //boolean fileNameSRegex = pathWatchToolController.getFileNameSupportRegexCheckBox().isSelected();
        String ourcode = (String) fileAnaylizeController.getOurcodeChoiceBox().getValue();

        if (ourcode.isEmpty() || ourcode == null) {
            TooltipUtil.showToast("参数不能为空!!!", Pos.CENTER);
            return;
        }
        map.put("ourcode", ourcode);

        if (!watchPath.endsWith(".zip")) {
            TooltipUtil.showToast("文件类型错误,请重试!!!", Pos.CENTER);
        }

        ThreadPoolExecutor threadPoolExecutor = org.baiyi.javaFxTool.utils.ThreadUtil.getThreadPoolExecutor();
        StringBuffer stringBuffer = new StringBuffer();
        thread = new Thread(() -> {
            ThreadUtil.sleep(300);
            try {
                if (FileUtil.getName(watchPath).startsWith(ourcode)) {
                    new AnaylizeFileSend().makeWatch(watchPath + "",this);
                } else {
                    new AnaylizeFileRecv().makeWatch(watchPath + "",this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void stopWatchAction() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }

    public void appendText(String text) {
        fileAnaylizeController.getWatchLogTextArea().appendText(text);
    }
}