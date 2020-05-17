package org.baiyi.javaFxTool.fxmlView.tool;

import de.felixroske.jfxsupport.AbstractFxmlView;
import de.felixroske.jfxsupport.FXMLView;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Scope("prototype")
@Lazy
@FXMLView(value = "/org/baiyi/javaFxTool/fxmlView/tool/PathWatchTool.fxml")
public class PathWatchToolView extends AbstractFxmlView {
}
