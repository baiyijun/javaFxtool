package org.baiyi.javaFxTool.services.tool;

import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.core.thread.ThreadUtil;
import com.xwintop.xcore.util.javafx.TooltipUtil;
import javafx.geometry.Pos;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.baiyi.javaFxTool.controller.tool.FileAnaylizeController;
import org.baiyi.javaFxTool.services.business.AnaylizeFileSend;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    Thread thread = null;
    HashMap<String, String> map = new HashMap<>();

    public FileAnaylizeService(FileAnaylizeController fileAnaylizeController) {
        this.fileAnaylizeController = fileAnaylizeController;
        this.anaylizeFileSend = new AnaylizeFileSend();
    }

    public void watchAction() throws Exception {
        String watchPath = fileAnaylizeController.getWatchPathTextField().getText();
        if (StringUtils.isEmpty(watchPath)) {
            TooltipUtil.showToast("监控目录不能为空！");
            return;
        }
        Path path = Paths.get(watchPath);
        if (!Files.exists(path)) {
            TooltipUtil.showToast("监控目录不存在！");
            return;
        } else if (!Files.isDirectory(path)) {
            TooltipUtil.showToast("只能监控文件夹！");
            return;
        }
        if (thread != null) {
            thread.stop();
        }
        //boolean fileNameSRegex = pathWatchToolController.getFileNameSupportRegexCheckBox().isSelected();
        String ourcode = fileAnaylizeController.getOurcode().getText();

        if (ourcode.isEmpty()) {
            TooltipUtil.showToast("参数不能为空!!!", Pos.CENTER);
            return;
        }
        map.put("ourcode", ourcode);


        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 10, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>(1024));
        StringBuffer stringBuffer = new StringBuffer();
        File file = new File(watchPath);
        thread = new Thread(() -> {
            WatchMonitor watchMonitor = WatchMonitor.create(file, WatchMonitor.EVENTS_ALL);
            watchMonitor.setWatcher(new Watcher() {
                @Override
                public void onCreate(WatchEvent<?> event, Path currentPath) {
                    fileAnaylizeController.getWatchLogTextArea().appendText("监听到：" + currentPath + File.separator + event.context()+"\n");
                    ThreadUtil.sleep(300);
                    if (event.context().toString().endsWith(".zip")) {
                        threadPoolExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                anaylizeFileSend.makeWatch(currentPath + File.separator+event.context()+"");                            }
                        });
                    } else {
                        TooltipUtil.showToast("文件类型错误,请重试!!!", Pos.CENTER);
                    }
                }

                @Override
                public void onModify(WatchEvent<?> event, Path currentPath) {

                }

                @Override
                public void onDelete(WatchEvent<?> event, Path currentPath) {

                }

                @Override
                public void onOverflow(WatchEvent<?> event, Path currentPath) {

                }
            });
            //设置监听目录的最大深入，目录层级大于制定层级的变更将不被监听，默认只监听当前层级目录
            watchMonitor.setMaxDepth(3);
            //启动监听
            watchMonitor.start();

        });
        thread.start();
    }

    public void stopWatchAction() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }
}