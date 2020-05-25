package org.baiyi.javaFxTool.view.tool;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName: PathWatchToolView
 * @Description: 文件夹监控工具
 * @author: xufeng
 * @date: 2019/4/27 0027 1:06
 */

@Getter
@Setter
public abstract class FileAnaylizeView implements Initializable {
    @FXML
    protected TextField watchPathTextField;
    @FXML
    protected Button watchPathButton;
    @FXML
    protected CheckBox isShowNotificationCheckBox;
    @FXML
    protected Button watchButton;
    @FXML
    protected TextField ourcode;
    @FXML
    protected TextArea watchLogTextArea;

}