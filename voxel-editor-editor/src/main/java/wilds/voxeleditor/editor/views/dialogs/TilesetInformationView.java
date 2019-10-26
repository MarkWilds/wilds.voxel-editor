package wilds.voxeleditor.editor.views.dialogs;

import wilds.voxeleditor.core.models.Map;
import wilds.voxeleditor.editor.models.MapInformation;
import wilds.voxeleditor.editor.views.tablelayout.swing.Table;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.spinner.WebSpinner;
import com.alee.laf.text.WebTextField;
import com.alee.laf.window.WebDialog;

import javax.swing.SpinnerNumberModel;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * @author Mark van der Wal
 * @since 14/04/18
 */
public class TilesetInformationView extends WebDialog {

    private Consumer<MapInformation> onActionButtonPressed;

    private WebButton actionButton;
    private WebTextField nameTextField;
    private WebSpinner widthSpinner;

    private SpinnerNumberModel widthSpinnerModel;

    public TilesetInformationView() {
        initialize();
    }

    private void initialize() {
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(WebDialog.DISPOSE_ON_CLOSE);
        setSize(new Dimension(256, 176));
        setLocationRelativeTo(null);

        WebPanel contentPanel = new WebPanel();
        contentPanel.setPadding(12);
        Table contentTable = new Table();

        // create name
        contentTable.addCell(new WebLabel("Name:")).colspan(2).expandX().left();
        contentTable.row();

        nameTextField = new WebTextField();
        nameTextField.setText(Map.DEFAULT_MAP_NAME);
        contentTable.addCell(nameTextField).padBottom(4).colspan(2).expandX().fillX();

        // create width and height components
        contentTable.row();

        contentTable.addCell(new WebLabel("Width:")).expandX().left();
        contentTable.addCell(new WebLabel("Height:")).expandX().left();

        // create spinners
        contentTable.row();

        widthSpinnerModel = new SpinnerNumberModel(Map.MAX_MAP_DIMENSION,
                Map.MAP_STEP, Map.MAX_MAP_DIMENSION, Map.MAP_STEP);

        widthSpinner = new WebSpinner(widthSpinnerModel);
        contentTable.addCell(widthSpinner).expandX().fillX();

        // set ok button and set initial map information
        contentTable.row();

        actionButton = new WebButton("Ok");
        actionButton.addActionListener(this::setOnActionButtonPressed);
        contentTable.addCell(actionButton).padTop(6).colspan(2).expandX().fillX();

        contentPanel.add(contentTable);
        add(contentPanel);
    }

    private void setOnActionButtonPressed(ActionEvent event) {
        if (isNameValid()) {
            setVisible(false);
            dispose();
//            notifyObserver();
        }
    }

    private boolean isNameValid() {
        String name = nameTextField.getText();
        return name != null && !name.isEmpty() && !name.trim().isEmpty();
    }

    public void setOnActionHandler(Consumer<MapInformation> action) {
        onActionButtonPressed = action;
    }
}
