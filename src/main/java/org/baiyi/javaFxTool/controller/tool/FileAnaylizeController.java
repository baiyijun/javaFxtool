package org.baiyi.javaFxTool.controller.tool;


import cn.hutool.core.thread.ThreadUtil;
import com.xwintop.xcore.util.javafx.FileChooserUtil;
import de.felixroske.jfxsupport.FXMLController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.baiyi.javaFxTool.services.tool.FileAnaylizeService;
import org.baiyi.javaFxTool.view.tool.FileAnaylizeView;
import org.springframework.context.annotation.Lazy;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @ClassName: PathWatchToolController
 * @Description: 文件夹监控工具
 * @author:
 * @date: 2019/10/27 1027 1:06
 */

@Getter
@Setter
@Slf4j
@Lazy
@FXMLController
public class FileAnaylizeController extends FileAnaylizeView {
    private FileAnaylizeService pathWatchToolService = new FileAnaylizeService(this);
    private String[] ourcode = new String[]{
            "T02", "T10", "T12", "T13", "T14"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initView();
        initEvent();
        initService();
    }

    private void initView() {
        this.ourcodeChoiceBox.getItems().addAll(this.ourcode);
        this.ourcodeChoiceBox.setValue(ourcode[0]);
    }

    private void initEvent() {
        FileChooserUtil.setOnDrag(watchPathTextField, FileChooserUtil.FileType.FOLDER);
    }

    private void initService() {
    }


    @FXML
    private void watchPathAction(ActionEvent event) {
        if (!watchPathTextField.getText().isEmpty()) {
            watchPathTextField.setText("");
        }
        File file = FileChooserUtil.chooseFile();
        if (file != null) {
            watchPathTextField.setText(file.getPath());
        }
    }

    @FXML
    private void watchAction(ActionEvent event) throws Exception {
        try {
            if ("解析".equals(watchButton.getText())) {
                if(!watchLogTextArea.getText().isEmpty()) {
                    watchLogTextArea.clear();
                }
                ThreadUtil.sleep(1000);
                pathWatchToolService.watchAction();
                ThreadUtil.sleep(1000);
            } else {
                pathWatchToolService.stopWatchAction();
                watchButton.setText("解析");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}