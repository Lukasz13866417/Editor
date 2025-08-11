package org.example.editor.layout_elements;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.example.editor.config.TabNameGenerator;
import org.example.editor.config.TerminalConfig;
import org.example.editor.layout_api.Component;

import java.nio.file.Path;

public class TerminalTabPaneComponent extends Component {

    private final TabPane tabPane;
    private final TerminalConfig terminalConfig;
    private final TabNameGenerator tabNameGenerator;
    private final Path terminalPath;
    private boolean designMode = false;


    public TerminalTabPaneComponent(TerminalConfig terminalConfig, TabNameGenerator tabNameGenerator, Path terminalPath) {
        super(new TabPane(), "terminal-" + System.currentTimeMillis());

        this.tabPane = (TabPane) getRegion();
        this.terminalConfig = terminalConfig;
        this.tabNameGenerator = tabNameGenerator;
        this.terminalPath = terminalPath;

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        addNewTerminalTab();
        addPlusTab();
    }

    private void addPlusTab() {
        Tab plusTab = new Tab("+");
        plusTab.setClosable(false);

        plusTab.setOnSelectionChanged(event -> {
            if (plusTab.isSelected()) {
                if (!designMode) {
                    addNewTerminalTab();
                } else {
                    tabPane.getSelectionModel().select(0); // wróć na pierwszą zakładkę
                }
            }
        });


        tabPane.getTabs().add(plusTab);
    }

    private void addNewTerminalTab() {
        TerminalTab terminalTab = new TerminalTab(terminalConfig, tabNameGenerator, terminalPath);

        int insertIndex = Math.max(tabPane.getTabs().size() - 1, 0);
        tabPane.getTabs().add(insertIndex, terminalTab);
        tabPane.getSelectionModel().select(terminalTab);
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public void setDesignMode(boolean isDesignMode) {
        this.designMode = isDesignMode;

        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof TerminalTab terminalTab) {
                terminalTab.getTerminal().setInputDisabled(isDesignMode);
                terminalTab.setAllowNewTerminal(!isDesignMode);
                terminalTab.setAllowClose(!isDesignMode);
            }
        }

        tabPane.setTabClosingPolicy(
                isDesignMode ? TabPane.TabClosingPolicy.UNAVAILABLE : TabPane.TabClosingPolicy.ALL_TABS
        );
    }



}
