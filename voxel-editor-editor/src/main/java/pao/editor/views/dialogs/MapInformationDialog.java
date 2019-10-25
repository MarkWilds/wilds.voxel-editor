package pao.editor.views.dialogs;

import pao.core.models.Map;
import pao.editor.models.MapInformation;
import pao.editor.views.tablelayout.swing.Table;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.spinner.WebSpinner;
import com.alee.laf.text.WebTextField;
import com.alee.laf.window.WebDialog;
import com.alee.managers.notification.NotificationListener;
import com.alee.managers.notification.NotificationOption;
import com.alee.managers.notification.WebNotification;

import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * @author Mark "Wilds" van der Wal
 * @since 19-2-2018
 */
public class MapInformationDialog extends WebDialog {
    private Consumer<MapInformation> onActionButtonPressed;

    private WebButton actionButton;
    private WebTextField nameTextField;
    private WebSpinner widthSpinner;
    private WebSpinner heightSpinner;

    private SpinnerNumberModel widthSpinnerModel;
    private SpinnerNumberModel heightSpinnerModel;

    private WebNotification notification;

    public MapInformationDialog(JFrame parent) {
        this(parent, null);
    }

    public MapInformationDialog(JFrame parent, MapInformation mapInformation) {
        super(parent);
        initialize(mapInformation);
    }

    private void initialize(MapInformation mapInformation) {
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

        heightSpinnerModel = new SpinnerNumberModel(Map.MAX_MAP_DIMENSION,
                Map.MAP_STEP, Map.MAX_MAP_DIMENSION, Map.MAP_STEP);

        widthSpinner = new WebSpinner(widthSpinnerModel);
        contentTable.addCell(widthSpinner).expandX().fillX();

        heightSpinner = new WebSpinner(heightSpinnerModel);
        contentTable.addCell(heightSpinner).expandX().fillX();

        // set ok button and set initial map information
        contentTable.row();

        actionButton = new WebButton("Ok");
        actionButton.addActionListener(this::setOnActionButtonPressed);
        contentTable.addCell(actionButton).padTop(6).colspan(2).expandX().fillX();

        if (mapInformation != null) {
            nameTextField.setText(mapInformation.getMapName());
            widthSpinnerModel.setValue(mapInformation.getMapWidth());
            heightSpinnerModel.setValue(mapInformation.getMapHeight());
        }

        contentPanel.add(contentTable);
        add(contentPanel);
    }

    private void setOnActionButtonPressed(ActionEvent event) {
        if (isNameValid()) {
            setVisible(false);
            dispose();
            notifyObserver();
        } else if (notification == null) {

            notification = new WebNotification();
            notification.setContent("Name must be valid!");
            notification.setClickToClose(false);
            notification.setDisplayTime(750);

            notification.addNotificationListener(new NotificationListener() {
                @Override
                public void optionSelected(NotificationOption notificationOption) {

                }

                @Override
                public void accepted() {

                }

                @Override
                public void closed() {
                    notification = null;
                }
            });

            notification.showPopup(nameTextField, 0, -nameTextField.getHeight());
        }
    }

    private boolean isNameValid() {
        String name = nameTextField.getText();
        return name != null && !name.isEmpty() && !name.trim().isEmpty();
    }

    private void notifyObserver() {
        if (onActionButtonPressed != null) {
            MapInformation mapInformation = new MapInformation();
            mapInformation.setMapName(nameTextField.getText());
            mapInformation.setMapWidth((int) widthSpinnerModel.getValue());
            mapInformation.setMapHeight((int) heightSpinnerModel.getValue());
            onActionButtonPressed.accept(mapInformation);
        }
    }

    public void setActionButtonText(String text) {
        actionButton.setText(text);
    }

    public void setOnActionHandler(Consumer<MapInformation> action) {
        onActionButtonPressed = action;
    }
}
